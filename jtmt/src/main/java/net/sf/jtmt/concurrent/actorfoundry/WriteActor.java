package net.sf.jtmt.concurrent.actorfoundry;

import osl.manager.Actor;
import osl.manager.ActorName;
import osl.manager.RemoteCodeException;
import osl.manager.annotations.message;

/**
 * Write Actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class WriteActor extends Actor {

  private static final long serialVersionUID = -4203081425372996186L;

  private ActorName actorManager;

  public WriteActor(ActorName manager) {
    actorManager = manager;
  }

  @message
  public void write(String message) throws RemoteCodeException {
    String newMessage = message.replaceFirst("Indexed ", "Wrote ");
//    send(stdout, "println", newMessage);
  }
  
  @message public void stop() throws RemoteCodeException {
    ActorManager.decrementLatch();
  }
}
