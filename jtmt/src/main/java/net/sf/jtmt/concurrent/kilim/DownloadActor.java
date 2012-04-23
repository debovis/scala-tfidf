package net.sf.jtmt.concurrent.kilim;

import kilim.Mailbox;

/**
 * Download Actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class DownloadActor extends Actor {

  public DownloadActor(int numThreads, Mailbox<String> inbox, Mailbox<String> outbox) {
    super(numThreads, inbox, outbox);
  }

  @Override
  public String act(String request) {
    return request.replaceFirst("Requested ", "Downloaded ");
  }
}
