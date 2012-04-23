// $Id$
// $Source$
package net.sf.jtmt.concurrent.kabuki.actors;

import net.sf.jtmt.concurrent.kabuki.core.Actor;

/**
 * Demo download actor for testing.
 * @author Sujit Pal
 * @version $Revision$
 */
public class DownloadActor extends Actor<String,String> {

  @Override public Type type() {
    return Type.WRITE_ONLY;
  }

  @Override public void init() throws Exception {
    perform(null);
  }

  @Override public void destroy() throws Exception { /* NOOP */ }

  @Override public String perform(String input) throws Exception {
    for (int i = 0; i < 10; i++) {
      input = "Download Document-#:" + i;
      logger.info(input);
      send(input);
    }
    shutdown();
    return null;
  }
}
