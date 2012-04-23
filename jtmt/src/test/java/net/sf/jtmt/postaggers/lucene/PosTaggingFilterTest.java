// $Id$
// $Source$
package net.sf.jtmt.postaggers.lucene;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * Test for the Lucene POS Tagging Filter.
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public class PosTaggingFilterTest {

  private String[] INPUT_TEXTS = {
    "The growing popularity of Linux in Asia, Europe, and the US is " +
    "a major concern for Microsoft.",
    "Jaguar will sell its new XJ-6 model in the US for a small fortune.",
    "The union is in a sad state.",
    "Please do not state the obvious.",
    "I am looking forward to the state of the union address.",
    "I have a bad cold today.",
    "The cold war was over long ago."
  };
  
  private Analyzer analyzer = new Analyzer() {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      return new StandardTokenizer(Version.LUCENE_30, reader);
    }
  };
  
  @Test
  public void testPosTagging() throws Exception {
    for (String inputText : INPUT_TEXTS) {
      System.out.println("Input: " + inputText);
      List<String> tags = new ArrayList<String>();
      TokenStream input = analyzer.tokenStream("f", new StringReader(inputText));
      TokenStream suffixStream = analyzer.tokenStream("f", new StringReader(inputText));
      input = new PosTaggingFilter(input, suffixStream,
        "/opt/wordnet-3.0/dict", "src/main/resources/pos_suffixes.txt",
        "src/main/resources/pos_trans_prob.txt");
      TermAttribute termAttribute = (TermAttribute) input.addAttribute(TermAttribute.class);
      PosAttribute posAttribute = (PosAttribute) input.addAttribute(PosAttribute.class);
      while (input.incrementToken()) {
        tags.add(termAttribute.term() + "/" + posAttribute.getPos());
      }
      input.end();
      input.close();
      StringBuilder tagBuf = new StringBuilder();
      tagBuf.append("Tagged: ").append(StringUtils.join(tags.iterator(), " "));
      System.out.println(tagBuf.toString());
    }
  }
}
