package net.sf.jtmt.ontology.relational.transactions;

/**
 * Enumeration of possible transactions available for the Ontology.
 * @author Sujit Pal
 * @version $Revision$
 */
public enum Transactions {
  addEntity(1),
  updEntity(2),
  delEntity(3),
  addAttr(4),
  updAttr(5),
  delAttr(6),
  addRel(7),
  addFact(8),
  delFact(9);
  
  private int transactionId;
  
  Transactions(int transactionId) {
    this.transactionId = transactionId;
  }
  
  public int id() {
    return transactionId;
  }
}
