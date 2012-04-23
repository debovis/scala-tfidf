package net.sf.jtmt.ontology.relational;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * A collection of nodes that are connected to each other via relations.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Ontology implements Serializable {

  private static final long serialVersionUID = 8903265933795172508L;
  
  private final Log log = LogFactory.getLog(getClass());
  
  protected Map<Long,Entity> entityMap;
  protected Map<Long,Relation> relationMap;
  protected SimpleDirectedGraph<Entity,RelationEdge> ontology;
  
  public Ontology() {
    entityMap = new HashMap<Long,Entity>();
    relationMap = new HashMap<Long,Relation>();
    ontology = new SimpleDirectedGraph<Entity,RelationEdge>(
      new ClassBasedEdgeFactory<Entity,RelationEdge>(RelationEdge.class));
  }

  public Entity getEntityById(long entityId) {
    return entityMap.get(entityId);
  }

  public Relation getRelationById(long relationId) {
    return relationMap.get(relationId);
  }
  
  public Set<Long> getAvailableRelationIds(Entity entity) {
    Set<Long> relationIds = new HashSet<Long>();
    Set<RelationEdge> relationEdges = ontology.edgesOf(entity);
    for (RelationEdge relationEdge : relationEdges) {
      relationIds.add(relationEdge.getRelationId());
    }
    return relationIds;
  }
  
  public Set<Entity> getEntitiesRelatedById(Entity entity, long relationId) {
    Set<RelationEdge> relationEdges = ontology.outgoingEdgesOf(entity);
    Set<Entity> relatedEntities = new HashSet<Entity>();
    for (RelationEdge relationEdge : relationEdges) {
      if (relationEdge.getRelationId() == relationId) {
        Entity relatedEntity = ontology.getEdgeTarget(relationEdge);
        relatedEntities.add(relatedEntity);
      }
    }
    return relatedEntities;
  }
  
  public void addEntity(Entity entity) throws OntologyException {
    if (entityMap.get(entity.getId()) != null) {
      throw new OntologyException("Entity(id=" + entity.getId() + ") exists, cannot add");
    }
    entityMap.put(entity.getId(), entity);
    ontology.addVertex(entity);
  }
  
  public void updateEntity(Entity entity) throws OntologyException {
    Entity entityToUpdate = entityMap.get(entity.getId());
    if (entityToUpdate == null) {
      throw new OntologyException("Entity(id=" + entity.getId() + ") does not exist, cannot update");
    }
    entityMap.put(entity.getId(), entity);
  }
  
  public void removeEntity(Entity entity) throws OntologyException {
    Entity entityToDelete = entityMap.get(entity.getId());
    if (entityToDelete == null) {
      throw new OntologyException("Entity(id=" + entity.getId() + ") does not exist, cannot delete");
    }
    entityMap.remove(entity.getId());
    ontology.removeVertex(entity);
  }
  
  public void addAttribute(long entityId, Attribute attribute) throws OntologyException {
    Entity entityToAddTo = entityMap.get(entityId);
    if (entityToAddTo == null) {
      throw new OntologyException("Entity(id=" + entityId + ") does not exist, cannot add attribute");
    }
    if (attribute == null) {
      throw new OntologyException("Entity(id=" + entityId + ") cannot get a null attribute");
    }
    List<Attribute> newAttributes = new ArrayList<Attribute>();
    String attributeName = attribute.getName();
    boolean attributeExists = false;
    for (Attribute attr : entityToAddTo.getAttributes()) {
      if (attributeName.equals(attr.getName())) {
        String value = attr.getValue() + "|||" + attribute.getValue();
        attr.setValue(value);
        attributeExists = true;
      }
      newAttributes.add(attr);
    }
    if (! attributeExists) {
      newAttributes.add(attribute);
    }
    entityToAddTo.setAttributes(newAttributes);
    entityMap.put(entityId, entityToAddTo);
  }
  
  public void updateAttribute(long entityId, Attribute attribute) throws OntologyException {
    Entity entityToUpdate = entityMap.get(entityId);
    if (entityToUpdate == null) {
      throw new OntologyException("Entity(id=" + entityId + ") does not exist, cannot update attribute");
    }
    if (attribute == null) {
      throw new OntologyException("Entity(id=" + entityId + ") cannot get a null attribute");
    }
    String attributeName = attribute.getName();
    List<Attribute> updatedAttributes = new ArrayList<Attribute>();
    for (Attribute attr : entityToUpdate.getAttributes()) {
      if (attributeName.equals(attr.getName())) {
        attr.setValue(attribute.getValue());
      }
      updatedAttributes.add(attr);
    }
    entityToUpdate.setAttributes(updatedAttributes);
    entityMap.put(entityId, entityToUpdate);
  }
  
  public void removeAttribute(long entityId, Attribute attribute) throws OntologyException {
    Entity entityToUpdate = entityMap.get(entityId);
    if (entityToUpdate == null) {
      throw new OntologyException("Entity(id=" + entityId + ") does not exist, cannot remove attribute");
    }
    if (attribute == null) {
      throw new OntologyException("Entity(id=" + entityId + ") cannot remove null attribute");
    }
    String attributeName = attribute.getName();
    List<Attribute> updatedAttributes = new ArrayList<Attribute>();
    for (Attribute attr : entityToUpdate.getAttributes()) {
      if (attributeName.equals(attr.getName())) {
        // remove this from the updated list
        continue;
      }
      updatedAttributes.add(attr);
    }
    entityToUpdate.setAttributes(updatedAttributes);
    entityMap.put(entityId, entityToUpdate);
  }
  
  public void addRelation(Relation relation) throws OntologyException {
    if (relationMap.get(relation.getId()) != null) {
      throw new OntologyException("Relation(id=" + relation.getId() + ") already exists, cannot add");
    }
    relationMap.put(relation.getId(), relation);
  }
  
  public void addFact(Fact fact) throws OntologyException {
    Entity sourceEntity = getEntityById(fact.getSourceEntityId());
    if (sourceEntity == null) {
      throw new OntologyException("Source entity(id=" + fact.getSourceEntityId() + ") does not exist, cannot add fact");
    }
    Entity targetEntity = getEntityById(fact.getTargetEntityId());
    if (targetEntity == null) {
      throw new OntologyException("Target entity(id=" + fact.getTargetEntityId() + ") does not exist, cannot add fact");
    }
    long relationId = fact.getRelationId();
    Relation relation = getRelationById(relationId);
    if (relation == null) {
      throw new OntologyException("Relation(id=" + fact.getRelationId() + ") does not exist, cannot add fact");
    }
    Set<Long> relationIds = getAvailableRelationIds(sourceEntity);
    if (relationIds.contains(relationId)) {
      throw new OntologyException("Fact: " + relation.getName() + "(" + 
        sourceEntity.getName() + "," + targetEntity.getName() + 
        ") already added to ontology");
    }
    RelationEdge relationEdge = new RelationEdge();
    relationEdge.setRelationId(relationId);
    ontology.addEdge(sourceEntity, targetEntity, relationEdge);
    if (relationMap.get(-1L * relationId) != null) {
      RelationEdge reverseRelationEdge = new RelationEdge();
      reverseRelationEdge.setRelationId(-1L * relationId);
      ontology.addEdge(targetEntity, sourceEntity, reverseRelationEdge);
    }
  }
  
  public void removeFact(Fact fact) throws OntologyException {
    Entity sourceEntity = getEntityById(fact.getSourceEntityId());
    if (sourceEntity == null) {
      throw new OntologyException("Source entity(id=" + fact.getSourceEntityId() + ") not available");
    }
    Entity targetEntity = getEntityById(fact.getTargetEntityId());
    if (targetEntity == null) {
      throw new OntologyException("Target entity(id=" + fact.getTargetEntityId() + ") not available");
    }
    long relationId = fact.getRelationId();
    Relation relation = getRelationById(relationId);
    if (relation == null) {
      throw new OntologyException("Relation(id=" + relationId + ") not available");
    }
    boolean isReversibleRelation = (relationMap.get(-1L * relationId) != null); 
    Set<RelationEdge> edges = ontology.getAllEdges(sourceEntity, targetEntity);
    for (RelationEdge edge : edges) {
      if (edge.getRelationId() == relationId) {
        ontology.removeEdge(edge);
      }
      if (isReversibleRelation) {
        if (edge.getRelationId() == (-1L * relationId)) {
          ontology.removeEdge(edge);
        }
      }
    }
  }
}
