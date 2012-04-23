package net.sf.jtmt.ontology.relational.transactions;

import java.util.Date;

import net.sf.jtmt.ontology.relational.Entity;
import net.sf.jtmt.ontology.relational.Ontology;

import org.prevayler.TransactionWithQuery;

/**
 * Transaction Wrapper for Ontology.updateEntity(Entity)
 * @author Sujit Pal
 * @version $Revision$
 */
public class EntityUpdateTransaction implements TransactionWithQuery {

  private static final long serialVersionUID = 9045316383912698808L;

  private long entityId;
  private String entityName;
  
  public EntityUpdateTransaction() {
    super();
  }
  
  public EntityUpdateTransaction(long entityId, String entityName) {
    this();
    this.entityId = entityId;
    this.entityName = entityName;
  }
  
  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    Entity entity = ((Ontology) prevalentSystem).getEntityById(entityId);
    entity.setName(entityName);
    ((Ontology) prevalentSystem).updateEntity(entity);
    return entity;
  }

}
