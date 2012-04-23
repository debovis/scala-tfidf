package net.sf.jtmt.ontology.relational.transactions;

import java.util.Date;

import net.sf.jtmt.ontology.relational.Ontology;
import net.sf.jtmt.ontology.relational.Relation;

import org.prevayler.TransactionWithQuery;

/**
 * Transaction Wrapper for Ontology.addRelation(Relation)
 * @author Sujit Pal
 * @version $Revision$
 */
public class RelationAddTransaction implements TransactionWithQuery {

  private static final long serialVersionUID = -7166383188432254533L;

  private long relationId;
  private String relationName;
  
  public RelationAddTransaction() {
    super();
  }
  
  public RelationAddTransaction(long relationId, String relationName) {
    this();
    this.relationId = relationId;
    this.relationName = relationName;
  }
  
  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    Relation relation = new Relation();
    relation.setRelationId(relationId);
    relation.setRelationName(relationName);
    ((Ontology) prevalentSystem).addRelation(relation);
    return relation;
  }
}
