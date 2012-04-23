package net.sf.jtmt.concurrent.actorsguild;

import org.actorsguildframework.Actor;
import org.actorsguildframework.AsyncResult;
import org.actorsguildframework.annotations.Initializer;
import org.actorsguildframework.annotations.Message;

/**
 * Index Actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class IndexActor extends Actor {
  public WriteActor writeActor;
  
  @Initializer
  public AsyncResult<IndexActor> init(WriteActor writeActor) {
    this.writeActor = writeActor;
    return result(this);
  }
  
  
  @Message
  public AsyncResult<Void> index(int id, String payload) {
    String newPayload = payload.replaceFirst("Downloaded ", "Indexed ");
    ActorManager.log(newPayload);
    return writeActor.write(id, newPayload);
  }
}
