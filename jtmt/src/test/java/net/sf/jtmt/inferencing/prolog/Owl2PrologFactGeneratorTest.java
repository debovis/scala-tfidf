// $Id$
package net.sf.jtmt.inferencing.prolog;

import org.junit.Test;

/**
 * Test for Owl2PrologFactGenerator.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Owl2PrologFactGeneratorTest {

  @Test
  public void testGenerate() throws Exception {
    Owl2PrologFactGenerator generator = new Owl2PrologFactGenerator();
    generator.setInputOwlFilename(
      "file:///home/sujit/src/jtmt/src/main/resources/wine.rdf");
    generator.setOutputPrologFilename("/tmp/wine_facts.pro");
    generator.generate();
  }
}
