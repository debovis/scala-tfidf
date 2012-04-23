package net.sf.jtmt.ontology.relational.transactions;

import java.util.Date;

import net.sf.jtmt.ontology.relational.Entity;
import net.sf.jtmt.ontology.relational.Ontology;

import org.prevayler.TransactionWithQuery;

/**
 * Transaction Wrapper for Ontology.addEntity(Entity).
 * @author Sujit Pal
 * @version $Revision$
 */
public class EntityAddTransaction implements TransactionWithQuery {

  private static final long serialVersionUID = 4022640211143804194L;

  private long entityId;
  private String entityName;
  
  public EntityAddTransaction() {
    super();
  }
  
  public EntityAddTransaction(long entityId, String entityName) {
    this();
    this.entityId = entityId;
    this.entityName = entityName;
  }
  
  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    Entity entity = new Entity();
    entity.setId(entityId);
    entity.setName(entityName);
    ((Ontology) prevalentSystem).addEntity(entity);
    return entity;
  }
}
