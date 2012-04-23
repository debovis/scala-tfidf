package net.sf.jtmt.indexers.lucenepayload.converter;

import org.junit.Test;

/**
 * Runs the index conversion.
 * @author Sujit Pal
 * @version $Revision$
 */
public class IndexConverterTest {

  @Test
  public void testConvert() throws Exception {
    IndexConverter converter = new IndexConverter();
    converter.setConceptIndexPath("/prod/web/data/adamindex.20100101/cpindex");
    converter.setFulltextIndexPath("/prod/web/data/adamindex.20100101/ftindex");
    converter.setCombinedIndexPath("/prod/web/data.local/adamindex.20100101");
    converter.convert();
  }
}
