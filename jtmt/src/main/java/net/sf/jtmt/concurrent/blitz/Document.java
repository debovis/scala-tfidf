package net.sf.jtmt.concurrent.blitz;

import net.jini.core.entry.Entry;

/**
 * Represents the Document that will be transformed as it goes
 * through different processors.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Document implements Entry {

  private static final long serialVersionUID = 54056132871976348L;

  public static enum Status {New, Downloaded, Indexed, Written};
  
  public Status status;
  public String url;
  public String contents;
  public String indexData;
  
  public Document() {
    super();
  }
  
  public String toString() {
    String statusString = "unknown";
    switch (status) {
      case New: { statusString = "new"; break; }
      case Downloaded: { statusString = "downloaded"; break; }
      case Indexed: { statusString = "indexed"; break; }
      case Written: { statusString = "written"; break; }
      default: {}
    }
    return url + " (" + statusString + ")";
  }
  
  /**
   * We can make our smart task, dumb worker model by implementing
   * this approach.
   */
  public void execute() {
    
  }
}
