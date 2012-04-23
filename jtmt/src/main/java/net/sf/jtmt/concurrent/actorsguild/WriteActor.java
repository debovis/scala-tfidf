package net.sf.jtmt.concurrent.actorsguild;

import org.actorsguildframework.Actor;
import org.actorsguildframework.AsyncResult;
import org.actorsguildframework.annotations.Message;

/**
 * Write Actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class WriteActor extends Actor {
  @Message
  public AsyncResult<Void> write(int id, String payload) {
    String newPayload = payload.replaceFirst("Indexed ", "Wrote ");
    ActorManager.log(newPayload);
    return noResult();
  }
}
