package net.sf.jtmt.concurrent.actorfoundry;

import osl.manager.Actor;
import osl.manager.ActorName;
import osl.manager.RemoteCodeException;
import osl.manager.annotations.message;

/**
 * Download actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class DownloadActor extends Actor {

  private static final long serialVersionUID = -2311959419132224127L;

  private ActorName actorManager;
  private ActorName indexActor;
  
  public DownloadActor(ActorName manager) throws RemoteCodeException {
    actorManager = manager;
  }
  
  @message
  public void download(String message) throws RemoteCodeException {
    String newMessage = message.replaceFirst("Requested ", "Downloaded ");
    if (indexActor == null) {
      indexActor = create(IndexActor.class, actorManager);
    }
//    send(stdout, "println", newMessage);
    send(indexActor, "index", newMessage);
  }
  
  @message
  public void stop() throws RemoteCodeException {
    send(indexActor, "stop");
    ActorManager.decrementLatch();
  }
}
