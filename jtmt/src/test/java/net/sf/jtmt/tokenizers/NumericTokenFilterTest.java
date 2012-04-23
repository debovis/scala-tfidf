package net.sf.jtmt.tokenizers;

import java.io.Reader;
import java.io.StringReader;

import net.sf.jtmt.tokenizers.lucene.NumericTokenFilter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * Test for NumericTokenFilter - to make sure that we did the upgrade
 * from next() to incrementToken() correctly.
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public class NumericTokenFilterTest {

  private Analyzer analyzer = new Analyzer() {
    public TokenStream tokenStream(String fieldName, Reader reader) {
      return new NumericTokenFilter(
        new StandardTokenizer(Version.LUCENE_30, reader));
    }
  };
  private String test = "The quick brown 2.0 fox made 144 leaps across 244 km";

  // we run the same test with the next() method first (to make sure existing
  // behavior) and then comment it out and test with the incrementToken()
  // method to make sure that the behavior is identical.
  @Test
  public void testOldOrNewMethod() throws Exception {
    TokenStream tokens = analyzer.tokenStream("test", new StringReader(test));
    while (tokens.incrementToken()) {
      TermAttribute termAttribute = 
        (TermAttribute) tokens.getAttribute(TermAttribute.class);
      System.out.println("term=" + termAttribute.term());
    }
  }
}
