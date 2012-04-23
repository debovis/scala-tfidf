package net.sf.jtmt.ontology.relational.transactions;

import java.util.Date;

import net.sf.jtmt.ontology.relational.Entity;
import net.sf.jtmt.ontology.relational.Ontology;

import org.prevayler.TransactionWithQuery;

/**
 * Transaction Wrapper for Ontology.deleteEntity(Entity)
 * @author Sujit Pal
 * @version $Revision$
 */
public class EntityDeleteTransaction implements TransactionWithQuery {

  private static final long serialVersionUID = 5700307389881612819L;

  private long entityId;
  private String entityName;
  
  public EntityDeleteTransaction() {
    super();
  }
  
  public EntityDeleteTransaction(long entityId, String entityName) {
    this();
    this.entityId = entityId;
    this.entityName = entityName;
  }
  
  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    Entity entity = ((Ontology) prevalentSystem).getEntityById(entityId);
    entity.setName(entityName);
    ((Ontology) prevalentSystem).removeEntity(entity);
    return entity;
  }
}
