package net.sf.jtmt.ontology.relational.transactions;

import java.util.Date;

import net.sf.jtmt.ontology.relational.Fact;
import net.sf.jtmt.ontology.relational.Ontology;

import org.prevayler.TransactionWithQuery;

/**
 * Transaction Wrapper for Ontology.removeFact(Fact).
 * @author Sujit Pal
 * @version $Revision$
 */
public class FactDeleteTransaction implements TransactionWithQuery {

  private static final long serialVersionUID = 6310997512074702543L;

  private long sourceEntityId;
  private long targetEntityId;
  private long relationId;
  
  public FactDeleteTransaction() {
    super();
  }
  
  public FactDeleteTransaction(long sourceEntityId, long targetEntityId, long relationId) {
    this();
    this.sourceEntityId = sourceEntityId;
    this.targetEntityId = targetEntityId;
    this.relationId = relationId;
  }

  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    Fact fact = new Fact();
    fact.setSourceEntityId(sourceEntityId);
    fact.setTargetEntityId(targetEntityId);
    fact.setRelationId(relationId);
    ((Ontology) prevalentSystem).removeFact(fact);
    return fact;
  }
}
