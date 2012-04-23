package net.sf.jtmt.postaggers;

import org.junit.Test;

/**
 * Test harness for Brown's corpus data loader.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BrownCorpusReaderTest {
  
  @Test
  public void testLoadFile() throws Exception {
    BrownCorpusReader reader = new BrownCorpusReader();
//    reader.setDataFilesLocation("/opt/brown-2.0/cg50");
    reader.setDataFilesLocation("/opt/brown-2.0");
    reader.setWordDictionaryLocation("src/main/resources/brown_dict.txt");
    reader.read();
  }
}
