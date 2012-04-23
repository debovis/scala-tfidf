// $Id$
package net.sf.jtmt.phrase;

import org.junit.Test;

/**
 * Test for Sentence Sequence File generator.
 * @author Sujit Pal
 * @version $Revision$
 */
public class SentenceSequenceFileGeneratorTest {

  @Test
  public void testGenerate() throws Exception {
    SentenceSequenceFileGenerator ssfg = new SentenceSequenceFileGenerator();
    ssfg.generate("src/test/resources/phraseextractor/inputs", 
      "src/test/resources/phraseextractor/holding1/books.seq");
  }
}
