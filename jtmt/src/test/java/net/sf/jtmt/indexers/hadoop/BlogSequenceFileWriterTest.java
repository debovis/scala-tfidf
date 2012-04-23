// $Id$
package net.sf.jtmt.indexers.hadoop;

import org.junit.Test;

/**
 * Runner for BlogSequenceFileWriter.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BlogSequenceFileWriterTest {

  @Test
  public void testGenerate() throws Exception {
    BlogSequenceFileWriter writer = new BlogSequenceFileWriter(
      "/home/sujit/src/gclient/src/test/resources/data/blog",
      "/home/sujit/src/jtmt/src/test/resources/hac/inputs");
    writer.generate();
  }
}
