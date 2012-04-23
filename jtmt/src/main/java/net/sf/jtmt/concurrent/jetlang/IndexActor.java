package net.sf.jtmt.concurrent.jetlang;

import org.jetlang.channels.Channel;
import org.jetlang.fibers.Fiber;

/**
 * Index Actor.
 * @author Sujit Pal
 * @version $Revision$
 */
public class IndexActor extends Actor {

  public IndexActor(Channel<String> inChannel, Channel<String> outChannel,
      Channel<Void> stopChannel, Channel<Void> nextStopChannel,
      Fiber fiber) {
    super(inChannel, outChannel, stopChannel, nextStopChannel, fiber);
  }

  @Override
  public String act(String payload) {
    return payload.replaceFirst("Downloaded ", "Indexed ");
  }
}
