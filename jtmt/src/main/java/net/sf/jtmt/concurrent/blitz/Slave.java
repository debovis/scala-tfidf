package net.sf.jtmt.concurrent.blitz;

import java.rmi.RMISecurityManager;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Models a slave process. There can be many slave processes in
 * the system, each attached to a single JavaSpace. In the current
 * setup, a slave consists of 1 instance each of the Download and
 * the Index thread, but optionally (not implemented yet) can 
 * consist of a set of m Downloaders and n Indexers where (m,n)
 * are controlled by configuration, and depend on the machine on
 * which it will run. The processing units are coalesced into threads
 * to make it easier to manage - there is no direct communication 
 * between the threads.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Slave {

  private final Log log = LogFactory.getLog(getClass());
  
  private static final long RETRY_INTERVAL_MILLIS = 5000L; // 5 seconds
  
  private boolean shouldTerminate = false;
  
  public void doWork() throws Exception {
    Thread downloadThread = new Thread(new Runnable() {
      public void run() {
        try {
          DownloadProcessor downloader = new DownloadProcessor();
          for (;;) {
            if (shouldTerminate) {
              break;
            }
            try {
              downloader.process();
            } catch (Exception e) {
              log.warn("Download Process failed, retrying...", e);
              pause(RETRY_INTERVAL_MILLIS);
              continue;
            }
          }
        } catch (Exception e) {
          log.error("Exception creating DownloadProcessor", e);
        }
      }
    });
    Thread indexThread = new Thread(new Runnable() {
      public void run() {
        try {
          IndexProcessor indexer = new IndexProcessor();
          for (;;) {
            if (shouldTerminate) {
              break;
            }
            try {
              indexer.process();
            } catch (Exception e) {
              log.warn("Index process failed, retrying...", e);
              pause(RETRY_INTERVAL_MILLIS);
              continue;
            }
          }
        } catch (Exception e) {
          log.error("Exception creating IndexProcessor", e);
        }
      }
    });
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        shouldTerminate = true;
      }
    }));
    downloadThread.start();
    indexThread.start();
    downloadThread.join();
    indexThread.join();
  }

  private void pause(long retryIntervalMills) {
    try { Thread.sleep(RETRY_INTERVAL_MILLIS); }
    catch (InterruptedException e) {
      log.info("Pause interrupted");
    }
  }

  /**
   * Models the Download Processing Unit.
   */
  private class DownloadProcessor extends AbstractProcessor {
    
    private JavaSpace space;
    private Document template;
    
    public DownloadProcessor() throws Exception {
      super();
      this.space = getSpace();
      this.template = getTemplate(Document.Status.New);
    }
    
    @Override
    public void process() throws Exception {
      Transaction tx = null;
      try {
        tx = createTransaction();
        Document doc = (Document) space.take(template, tx, Lease.FOREVER);
        // more processing here...
        log.info("Downloading " + doc);
        doc.status = Document.Status.Downloaded;
        space.write(doc, tx, DEFAULT_LEASE_TIMEOUT);
        tx.commit();
      } catch (Exception e) {
        if (tx != null) {
          tx.abort();
        }
        throw e;
      }
    }
  }
  
  /**
   * Models the Indexing Processing Unit.
   */
  private class IndexProcessor extends AbstractProcessor {

    private JavaSpace space;
    private Document template;
    
    public IndexProcessor() throws Exception {
      super();
      this.space = getSpace();
      this.template = getTemplate(Document.Status.Downloaded);
    }
    
    @Override
    public void process() throws Exception {
      Transaction tx = null;
      try {
        tx = createTransaction();
        Document doc = (Document) space.take(template, tx, Lease.FOREVER);
        log.info("Indexing " + doc);
        doc.status = Document.Status.Indexed;
        space.write(doc, tx, DEFAULT_LEASE_TIMEOUT);
        tx.commit();
      } catch (Exception e) {
        if (tx != null) {
          tx.abort();
        }
        throw e;
      }
    }
  }

  /**
   * This is how we are called.
   */
  public static void main(String[] args) {
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new RMISecurityManager());
    }
    try {
      Slave slave = new Slave();
      slave.doWork();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
