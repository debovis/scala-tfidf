// $Id$
// $Source$
package net.sf.jtmt.concurrent.kabuki.actors;

import java.io.FileWriter;
import java.io.PrintWriter;

import net.sf.jtmt.concurrent.kabuki.core.Actor;

import org.apache.commons.lang.StringUtils;

/**
 * Demo write actor for testing.
 * @author Sujit Pal
 * @version $Revision$
 */
public class WriteActor extends Actor<String,String> {

  PrintWriter printWriter;
  
  @Override public Type type() {
    return Type.READ_ONLY;
  }

  @Override public void init() throws Exception {
    printWriter = new PrintWriter(new FileWriter("/tmp/demo.txt"), true);
  }

  @Override public void destroy() throws Exception {
    if (printWriter != null) {
      printWriter.flush();
      printWriter.close();
    }
  }

  @Override public String perform(String input) {
    String output = StringUtils.replace(input, "Index", "Write");
    logger.info(output);
    printWriter.println(output);
    return null;
  }
}
