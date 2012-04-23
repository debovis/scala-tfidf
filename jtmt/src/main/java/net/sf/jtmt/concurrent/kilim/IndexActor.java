package net.sf.jtmt.concurrent.kilim;

import kilim.Mailbox;

/**
 * Index Actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class IndexActor extends Actor {

  public IndexActor(int numThreads, Mailbox<String> inbox, Mailbox<String> outbox) {
    super(numThreads, inbox, outbox);
  }

  @Override
  public String act(String request) {
    return request.replaceFirst("Downloaded ", "Indexed ");
  }
}
