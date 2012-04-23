// $Id$
// $Source$
package net.sf.jtmt.concurrent.kabuki.actors;

import net.sf.jtmt.concurrent.kabuki.core.Actor;

import org.apache.commons.lang.StringUtils;

/**
 * Demo index actor for testing.
 * @author Sujit Pal
 * @version $Revision$
 */
public class IndexActor extends Actor<String,String> {

  @Override public Type type() {
    return Type.READ_WRITE;
  }

  @Override public void init() throws Exception { /* NOOP */ }

  @Override public void destroy() throws Exception { /* NOOP */ }

  @Override public String perform(String input) {
    String output = StringUtils.replace(input, "Download", "Index");
    logger.info(output);
    return output;
  }
}
