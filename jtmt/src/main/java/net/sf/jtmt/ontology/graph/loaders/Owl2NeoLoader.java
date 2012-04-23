// $Id$
package net.sf.jtmt.ontology.graph.loaders;

import net.sf.jtmt.ontology.graph.OntologyRelationshipType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.NotFoundException;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.index.IndexService;
import org.neo4j.util.index.LuceneIndexService;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Parses an OWL RDF file and populates a graph database directly.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Owl2NeoLoader {

  private static final String FIELD_ENTITY_NAME = "name";
  private static final String FIELD_ENTITY_TYPE = "type";
  private static final String FIELD_RELATIONSHIP_NAME = "name";
  private static final String FIELD_RELATIONSHIP_WEIGHT = "weight";
  
  private final Log log = LogFactory.getLog(getClass());
  
  private String filePath;
  private String dbPath;
  private String ontologyName;
  private String refNodeName;
  
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
  
  public void setDbPath(String dbPath) {
    this.dbPath = dbPath;
  }
  
  public void setOntologyName(String ontologyName) {
    this.ontologyName = ontologyName;
  }
  
  public void setRefNodeName(String refNodeName) {
    this.refNodeName = refNodeName;
  }
  
  public void load() throws Exception {
    NeoService neoService = null;
    IndexService indexService = null;
    try {
      // set up an embedded instance of neo database
      neoService = new EmbeddedNeo(dbPath);
      // set up index service for looking up node by name
      indexService = new LuceneIndexService(neoService);
      // set up top-level pseudo nodes for navigation
      org.neo4j.api.core.Node refNode = getReferenceNode(neoService);
      org.neo4j.api.core.Node fileNode = getFileNode(neoService, refNode);
      // parse the owl rdf file
      Model model = ModelFactory.createDefaultModel();
      model.read("file://" + filePath);
      // iterate through all triples in the file, and set up corresponding
      // nodes in the neo database.
      StmtIterator it = model.listStatements();
      while (it.hasNext()) {
        Statement st = it.next();
        Triple triple = st.asTriple();
        insertIntoDb(neoService, indexService, fileNode, triple);
      }
    } finally {
      if (indexService != null) {
        indexService.shutdown();
      }
      if (neoService != null) {
        neoService.shutdown();
      }
    }
  }

  /**
   * Get the reference node if already available, otherwise create it.
   * @param neoService the reference to the Neo service.
   * @return a Neo4j Node object reference to the reference node.
   * @throws Exception if thrown.
   */
  private org.neo4j.api.core.Node getReferenceNode(NeoService neoService) 
      throws Exception { 
    org.neo4j.api.core.Node refNode = null;
    Transaction tx = neoService.beginTx(); 
    try {
      refNode = neoService.getReferenceNode();
      if (! refNode.hasProperty(FIELD_ENTITY_NAME)) {
        refNode.setProperty(FIELD_ENTITY_NAME, refNodeName);
        refNode.setProperty(FIELD_ENTITY_TYPE, "Thing");
      }
      tx.success();
    } catch (NotFoundException e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
    return refNode;
  }

  /**
   * Creates a single node for the file. This method is called once
   * per file, and the node should not exist in the Neo4j database.
   * So there is no need to check for existence of the node. Once
   * the node is created, it is connected to the reference node.
   * @param neoService the reference to the Neo service.
   * @param refNode the reference to the reference node.
   * @return the "file" node representing the entry-point into the
   * entities described by the current OWL file.
   * @throws Exception if thrown.
   */
  private org.neo4j.api.core.Node getFileNode(NeoService neoService,
      org.neo4j.api.core.Node refNode) throws Exception {
    org.neo4j.api.core.Node fileNode = null;
    Transaction tx = neoService.beginTx();
    try {
      fileNode = neoService.createNode();
      fileNode.setProperty(FIELD_ENTITY_NAME, ontologyName);
      fileNode.setProperty(FIELD_ENTITY_TYPE, "Class");
      Relationship rel = refNode.createRelationshipTo(
        fileNode, OntologyRelationshipType.CATEGORIZED_AS);
      logTriple(refNode, OntologyRelationshipType.CATEGORIZED_AS, fileNode);
      rel.setProperty(
        FIELD_RELATIONSHIP_NAME, OntologyRelationshipType.CATEGORIZED_AS.name());
      rel.setProperty(FIELD_RELATIONSHIP_WEIGHT, 0.0F);
      tx.success();
    } catch (Exception e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
    return fileNode;
  }

  /**
   * Inserts selected entities and relationships from Triples extracted
   * from the OWL document by the Jena parser. Only entities which have
   * a non-blank node for the subject and object are used. Further, only
   * relationship types listed in OntologyRelationshipTypes enum are 
   * considered. In addition, if the enum specifies that certain 
   * relationship types have an inverse, the inverse relation is also
   * created here.
   * @param neoService a reference to the Neo service.
   * @param indexService a reference to the Index service (for looking
   * up Nodes by name).
   * @param fileNode a reference to the Node that is an entry point into
   * this ontology. This node will connect to both the subject and object 
   * nodes of the selected triples via a CONTAINS relationship. 
   * @param triple a reference to the Triple extracted by the Jena parser.
   * @throws Exception if thrown.
   */
  private void insertIntoDb(NeoService neoService, IndexService indexService,
      org.neo4j.api.core.Node fileNode, Triple triple) throws Exception {
    Node subject = triple.getSubject();
    Node predicate = triple.getPredicate();
    Node object = triple.getObject();
    if ((subject instanceof Node_URI) &&
        (object instanceof Node_URI)) {
      // get or create the subject and object nodes
      org.neo4j.api.core.Node subjectNode = 
        getEntityNode(neoService, indexService, subject);
      org.neo4j.api.core.Node objectNode =
        getEntityNode(neoService, indexService, object);
      if (subjectNode == null || objectNode == null) {
        return;
      }
      Transaction tx = neoService.beginTx();
      try {
        // hook up both nodes to the fileNode
        if (! isConnected(neoService, fileNode, 
            OntologyRelationshipType.CONTAINS, Direction.OUTGOING, subjectNode)) {
          logTriple(fileNode, OntologyRelationshipType.CONTAINS, subjectNode);
          Relationship rel = fileNode.createRelationshipTo(
            subjectNode, OntologyRelationshipType.CONTAINS);
          rel.setProperty(FIELD_RELATIONSHIP_NAME, 
            OntologyRelationshipType.CONTAINS.name());
          rel.setProperty(FIELD_RELATIONSHIP_WEIGHT, 0.0F);
        }
        if (! isConnected(neoService, fileNode, 
            OntologyRelationshipType.CONTAINS, Direction.OUTGOING, objectNode)) {
          logTriple(fileNode, OntologyRelationshipType.CONTAINS, objectNode);
          Relationship rel = fileNode.createRelationshipTo(
            objectNode, OntologyRelationshipType.CONTAINS);
          rel.setProperty(
            FIELD_RELATIONSHIP_NAME, OntologyRelationshipType.CONTAINS.name());
          rel.setProperty(FIELD_RELATIONSHIP_WEIGHT, 0.0F);
        }
        // hook up subject and object via predicate
        OntologyRelationshipType type = 
          OntologyRelationshipType.fromName(predicate.getLocalName());
        if (type != null) {
          logTriple(subjectNode, type, objectNode);
          Relationship rel = subjectNode.createRelationshipTo(
              objectNode, type);
          rel.setProperty(FIELD_RELATIONSHIP_NAME, type.name());
          rel.setProperty(FIELD_RELATIONSHIP_WEIGHT, 1.0F);
        }
        // create reverse relationship
        OntologyRelationshipType inverseType = 
          OntologyRelationshipType.inverseOf(predicate.getLocalName());
        if (inverseType != null) {
          logTriple(objectNode, inverseType, subjectNode);
          Relationship inverseRel = objectNode.createRelationshipTo(
            subjectNode, inverseType);
          inverseRel.setProperty(FIELD_RELATIONSHIP_NAME, inverseType.name());
          inverseRel.setProperty(FIELD_RELATIONSHIP_WEIGHT, 1.0F);
        }
        tx.success();
      } catch (Exception e) {
        tx.failure();
        throw e;
      } finally {
        tx.finish();
      }
    } else {
      return;
    }
  }

  /**
   * Loops through the relationships and returns true if the source
   * and target nodes are connected using the specified relationship
   * type and direction.
   * @param neoService a reference to the NeoService.
   * @param sourceNode the source Node object.
   * @param relationshipType the type of relationship.
   * @param direction the direction of the relationship.
   * @param targetNode the target Node object.
   * @return true or false.
   * @throws Exception if thrown.
   */
  private boolean isConnected(NeoService neoService, 
      org.neo4j.api.core.Node sourceNode,
      OntologyRelationshipType relationshipType, Direction direction,
      org.neo4j.api.core.Node targetNode) throws Exception {
    boolean isConnected = false;
    Transaction tx = neoService.beginTx();
    try {
      for (Relationship rel : sourceNode.getRelationships(relationshipType, direction)) {
        org.neo4j.api.core.Node endNode = rel.getEndNode();
        if (endNode.getProperty(FIELD_ENTITY_NAME).equals(
            targetNode.getProperty(FIELD_ENTITY_NAME))) {
          isConnected = true;
          break;
        }
      }
      tx.success();
    } catch (Exception e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
    return isConnected;
  }

  private org.neo4j.api.core.Node getEntityNode(NeoService neoService,
      IndexService indexService, Node entity) throws Exception {
    String uri = ((Node_URI) entity).getURI();
    if (uri.indexOf('#') == -1) {
      return null;
    }
    String[] parts = StringUtils.split(uri, "#");
    String type = parts[0].substring(0, parts[0].lastIndexOf('/'));
    Transaction tx = neoService.beginTx();
    try {
      org.neo4j.api.core.Node entityNode = 
        indexService.getSingleNode(FIELD_ENTITY_NAME, parts[1]);
      if (entityNode == null) {
        entityNode = neoService.createNode();
        entityNode.setProperty(FIELD_ENTITY_NAME, parts[1]);
        entityNode.setProperty(FIELD_ENTITY_TYPE, type);
        indexService.index(entityNode, FIELD_ENTITY_NAME, parts[1]);
      }
      tx.success();
      return entityNode;
    } catch (Exception e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
  }
  
  /**
   * Convenience method to log the triple when it is inserted into the
   * database.
   * @param sourceNode the subject of the triple.
   * @param ontologyRelationshipType the predicate of the triple.
   * @param targetNode the object of the triple.
   */
  private void logTriple(org.neo4j.api.core.Node sourceNode, 
      OntologyRelationshipType ontologyRelationshipType, 
      org.neo4j.api.core.Node targetNode) {
    log.info("(" + sourceNode.getProperty(FIELD_ENTITY_NAME) +
      "," + ontologyRelationshipType.name() + 
      "," + targetNode.getProperty(FIELD_ENTITY_NAME) + ")");
  }
}
