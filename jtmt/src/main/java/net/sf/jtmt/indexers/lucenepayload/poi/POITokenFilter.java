package net.sf.jtmt.indexers.lucenepayload.poi;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Payload;

/**
 * Token filter to parse out the POI string into individual POI
 * each with custom scores which are put into the payload depending 
 * on position and density.
 * @author Sujit Pal
 * @version $Revision$
 */
public class POITokenFilter extends TokenFilter {

  private TermAttribute termAttr;
  private PayloadAttribute payloadAttr;
  private int termPosition = 0;
  private float density = 0;
  
  protected POITokenFilter(TokenStream input, int numTokens) {
    super(input);
    this.termAttr = addAttribute(TermAttribute.class);
    this.payloadAttr = addAttribute(PayloadAttribute.class);
    if (numTokens > 0) {
      this.density = 1.0F / numTokens;
    }
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      float score = (1.0F - 
        ((termPosition <= 9) ? (0.1F * (float) termPosition) : 0.1F)) * density;
//      System.out.println(termAttr.term() + "=>" + score);
      payloadAttr.setPayload(new Payload(PayloadHelper.encodeFloat(score)));
      termPosition++;
      return true;
    }
//    System.out.println("==");
    return false;
  }
}
