package net.sf.jtmt.ontology.relational.transactions;

import java.util.Date;

import net.sf.jtmt.ontology.relational.Attribute;
import net.sf.jtmt.ontology.relational.Ontology;

import org.prevayler.TransactionWithQuery;

/**
 * Transaction Wrapper for Ontology.removeAttribute(entityId, Attribute).
 * @author Sujit Pal
 * @version $Revision$
 */
public class AttributeDeleteTransaction implements TransactionWithQuery {

  private static final long serialVersionUID = -5371569007324513472L;

  private long entityId;
  private String attributeName;
  private String attributeValue;
  
  public AttributeDeleteTransaction() {
    super();
  }
  
  public AttributeDeleteTransaction(long entityId, String attributeName, String attributeValue) {
    this();
    this.entityId = entityId;
    this.attributeName = attributeName;
    this.attributeValue = attributeValue;
  }
  
  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    Attribute attribute = new Attribute(attributeName, attributeValue);
    ((Ontology) prevalentSystem).removeAttribute(entityId, attribute);
    return attribute;
  }
}
