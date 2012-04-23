package net.sf.jtmt.concurrent.kilim;

import kilim.Mailbox;
import kilim.Task;
import kilim.pausable;

/**
 * Actor superclass.
 * @author Sujit Pal
 * @version $Revision$
 */
public abstract class Actor extends Task {

  private Mailbox<String> inbox;
  private Mailbox<String> outbox;
  
  public Actor(int numThreads, Mailbox<String> inbox, Mailbox<String> outbox) {
    this.inbox = inbox;
    this.outbox = outbox;
//    setScheduler(new Scheduler(numThreads));
  }

  @pausable
  public void execute() {
    for (;;) {
      String request = inbox.get();
      // this is custom poison pill handling code for our application
      if (request.equals(ActorManager.STOP)) {
        if (outbox != null) {
          outbox.put(request);
        }
        break;
      }
      // end of poison pill handling
      String response = act(request);
      ActorManager.log(response);
      if (outbox != null) {
        outbox.put(response);
      }
    }
  }
  
  public abstract String act(String request);
}
