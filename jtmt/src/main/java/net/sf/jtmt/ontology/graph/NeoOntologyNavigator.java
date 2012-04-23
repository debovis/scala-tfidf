// $Id$
package net.sf.jtmt.ontology.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.Traverser;
import org.neo4j.api.core.Traverser.Order;
import org.neo4j.util.index.IndexService;
import org.neo4j.util.index.LuceneIndexService;

/**
 * Provides methods to locate nodes and find neighbors in the Neo
 * graph database.
 * @author Sujit Pal
 * @version $Revision$
 */
public class NeoOntologyNavigator {

  public static final String FIELD_ENTITY_NAME = "name";
  public static final String FIELD_RELATIONSHIP_NAME = "name";
  public static final String FIELD_RELATIONSHIP_WEIGHT = "weight";

  private String neoDbPath;
  
  private NeoService neoService;
  private IndexService indexService;
  
  /**
   * Ctor for NeoOntologyNavigator
   * @param dbPath the path to the neo database.
   */
  public NeoOntologyNavigator(String dbPath) {
    super();
    this.neoDbPath = dbPath;
  }
  
  /**
   * The init() method should be called by client after instantiation.
   */
  public void init() {
    this.neoService = new EmbeddedNeo(neoDbPath);
    this.indexService = new LuceneIndexService(neoService);
  }
  
  /**
   * The destroy() method should be called by client on shutdown.
   */
  public void destroy() {
    indexService.shutdown();
    neoService.shutdown();
  }
  
  /**
   * Gets the reference to the named Node. Returns null if the node
   * is not found in the database.
   * @param nodeName the name of the node to lookup.
   * @return the reference to the Node, or null if not found.
   * @throws Exception if thrown.
   */
  public Node getByName(String nodeName) throws Exception {
    Transaction tx = neoService.beginTx();
    try {
      Node node = indexService.getSingleNode(FIELD_ENTITY_NAME, nodeName);
      tx.success();
      return node;
    } catch (Exception e) {
      tx.failure();
      throw(e);
    } finally {
      tx.finish();
    }
  }

  /**
   * Return a Map of relationship names to a List of nodes connected
   * by that relationship. The keys are sorted by name, and the list
   * of node values are sorted by the incoming relation weights.
   * @param node the root Node.
   * @return a Map of String to Node List of neighbors.
   */
  public Map<String,List<Node>> getAllNeighbors(Node node) throws Exception {
    BrowserReturnableEvaluator browserReturnableEvaluator = 
      new BrowserReturnableEvaluator(node);
    Transaction tx = neoService.beginTx();
    try {
      // set up the data structure for all outgoing relationships
      OntologyRelationshipType[] relTypes = OntologyRelationshipType.values();
      Object[] typeAndDirection = new Object[relTypes.length * 2];
      for (int i = 0; i < typeAndDirection.length; i++) {
        if (i % 2 == 0) {
          // relationship type slot
          typeAndDirection[i] = relTypes[i / 2];
        } else {
          // direction slot
          typeAndDirection[i] = Direction.OUTGOING;
        }
      }
      Traverser traverser = node.traverse(Order.BREADTH_FIRST, 
        StopEvaluator.DEPTH_ONE, 
        browserReturnableEvaluator, 
        typeAndDirection);
      for (Iterator<Node> it = traverser.iterator(); it.hasNext();) {
        // just eat up the nodes returned by the traverser, we are
        // really interested in the data structure.
        it.next();
      }
      // get at the accumulated data structure and return it
      tx.success();
      return browserReturnableEvaluator.getNeighbors();
    } catch (Exception e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
  }
  
  /**
   * Returns a List of neighbor nodes that is reachable from the specified
   * Node. No ordering is done (since the Traverser framework does not seem
   * to allow this type of traversal, and we want to use the Traverser here).
   * @param node reference to the base node.
   * @param type the relationship type.
   * @return a List of neighbor nodes.
   */
  public List<Node> getNeighborsRelatedBy(Node node, OntologyRelationshipType type) 
      throws Exception {
    List<Node> neighbors = new ArrayList<Node>();
    Transaction tx = neoService.beginTx();
    try {
      Traverser traverser = node.traverse(
        Order.BREADTH_FIRST, 
        StopEvaluator.DEPTH_ONE, 
        ReturnableEvaluator.ALL_BUT_START_NODE, 
        type, 
        Direction.OUTGOING);
      for (Iterator<Node> it = traverser.iterator(); it.hasNext();) {
        Node neighbor = it.next();
        neighbors.add(neighbor);
      }
      tx.success();
    } catch (Exception e) {
      tx.failure();
      throw(e);
    } finally {
      tx.success();
    }
    return neighbors;
  }
}
