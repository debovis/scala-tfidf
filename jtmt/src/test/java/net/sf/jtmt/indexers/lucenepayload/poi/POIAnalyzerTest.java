// $Id$
// $Source$
package net.sf.jtmt.indexers.lucenepayload.poi;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.junit.Test;

/**
 * Test for POI token filter
 * @author Sujit Pal
 * @version $Revision$
 */
public class POIAnalyzerTest {

  @Test
  public void testPoiTokenFilter() throws Exception {
    Analyzer analyzer = new POIAnalyzer();
    TokenStream tokenStream = analyzer.tokenStream("poi", new StringReader("t1 t2 t3"));
    while (tokenStream.incrementToken()) {
      TermAttribute termAttr = tokenStream.getAttribute(TermAttribute.class);
      OffsetAttribute offsetAttr = tokenStream.getAttribute(OffsetAttribute.class);
      PayloadAttribute payloadAttr = tokenStream.getAttribute(PayloadAttribute.class);
      System.out.println("term=" + termAttr.term() +
          ", payload=" + PayloadHelper.decodeFloat(payloadAttr.getPayload().getData()) + 
          ", offset=" + offsetAttr.startOffset());
    }
  }
}
