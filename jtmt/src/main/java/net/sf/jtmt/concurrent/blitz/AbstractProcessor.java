package net.sf.jtmt.concurrent.blitz;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract superclass for the processing unit. The class contains
 * common application level and JavaSpace related functionality,
 * so the subclasses can focus on the business logic. 
 * @author Sujit Pal
 * @version $Revision$
 */
public abstract class AbstractProcessor {

  protected Log log = LogFactory.getLog(getClass());
  
  protected final static long DEFAULT_LEASE_TIMEOUT = 120000L; // 2 mins

  /**
   * Simple lookup, returns the default JavaSpace found. More 
   * complex lookups across multiple machines may be possible
   * using LookupLocators. This method could be modified in the
   * future to return an array of JavaSpaces.
   * @return the default JavaSpace.
   */
  protected JavaSpace getSpace() throws Exception {
    Lookup lookup = new Lookup(JavaSpace.class);
    JavaSpace space = (JavaSpace) lookup.getService();
    return space;
  }

  /**
   * Returns a Transaction object to the caller by looking it
   * up from the JavaSpaces server.
   * @return a Transaction object.
   * @throws Exception if one is thrown.
   */
  protected Transaction createTransaction() throws Exception {
    Lookup lookup = new Lookup(TransactionManager.class);
    TransactionManager txManager = (TransactionManager) lookup.getService();
    Transaction.Created trc = TransactionFactory.create(txManager, DEFAULT_LEASE_TIMEOUT);
    return trc.transaction;
  }
  
  /**
   * Convenience method to build a Document template based on the
   * desired status.
   * @param status the DocumentStatus of the template.
   * @return the Document template for matching.
   */
  public Document getTemplate(Document.Status status) {
    Document doc = new Document();
    if (status != null) {
      doc.status = status;
    }
    return doc;
  }

  /**
   * This is the method that subclasses will override, and will contain
   * the business logic that needs to be applied to the entry.
   * @throws Exception if one is thrown.
   */
  public abstract void process() throws Exception;
}
