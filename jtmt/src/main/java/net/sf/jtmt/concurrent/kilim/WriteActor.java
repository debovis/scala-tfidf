package net.sf.jtmt.concurrent.kilim;

import kilim.Mailbox;

/**
 * Write Actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class WriteActor extends Actor {

  public WriteActor(int numThreads, Mailbox<String> inbox, Mailbox<String> outbox) {
    super(numThreads, inbox, outbox);
  }

  @Override
  public String act(String request) {
    return request.replaceFirst("Indexed ", "Wrote ");
  }
}
