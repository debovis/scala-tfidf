package net.sf.jtmt.concurrent.jetlang;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;

/**
 * @author Sujit Pal
 * @version $Revision$
 */
public class Channels {

  public static final Channel<String> downloadChannel = new MemoryChannel<String>();
  public static final Channel<Void> downloadStopChannel = new MemoryChannel<Void>();
  public static final Channel<String> indexChannel = new MemoryChannel<String>();
  public static final Channel<Void> indexStopChannel = new MemoryChannel<Void>();
  public static final Channel<String> writeChannel = new MemoryChannel<String>();
  public static final Channel<Void> writeStopChannel = new MemoryChannel<Void>();
}
