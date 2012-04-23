package net.sf.jtmt.concurrent.jetlang;

import org.jetlang.channels.Channel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

/**
 * Actor superclass.
 * @author Sujit Pal
 * @version $Revision$
 */
public abstract class Actor {

  private Channel<String> inChannel;
  private Channel<String> outChannel;
  private Channel<Void> stopChannel;
  private Channel<Void> nextStopChannel;
  private Fiber fiber;
  
  public Actor(Channel<String> inChannel, Channel<String> outChannel,
      Channel<Void> stopChannel, Channel<Void> nextStopChannel,
      Fiber fiber) {
    this.inChannel = inChannel;
    this.outChannel = outChannel;
    this.stopChannel = stopChannel;
    this.nextStopChannel = nextStopChannel;
    this.fiber = fiber;
  }
  
  public void start() {
    // set up subscription listener
    Callback<String> onRecieve = new Callback<String>() {
      public void onMessage(String message) {
        String response = act(message);
        ActorManager.log(response);
        if (outChannel != null) {
          outChannel.publish(response);
        }
      }
    };
    // subscribe to incoming channel
    inChannel.subscribe(fiber, onRecieve);
    // set up stop message listener
    Callback<Void> onStop = new Callback<Void>() {
      public void onMessage(Void message) {
        if (nextStopChannel != null) {
          nextStopChannel.publish(null);
        }
        fiber.dispose();
      }
    };
    // subscribe to stop channel
    stopChannel.subscribe(fiber, onStop);
    // start the fiber
    fiber.start();
  }
  
  public abstract String act(String message);
}
