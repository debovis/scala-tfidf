package net.sf.jtmt.tokenizers.lucene;

import java.io.IOException;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

/**
 * Filters out numeric tokens from the TokenStream.
 * @author Sujit Pal
 * @version $Revision$
 */
public class NumericTokenFilter extends TokenFilter {

  private TermAttribute termAttribute;
  
  public NumericTokenFilter(TokenStream input) {
    super(input);
    this.termAttribute = (TermAttribute) addAttribute(TermAttribute.class);
  }
  
  @Override
  public boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      String term = termAttribute.term();
      term = term.replaceAll(",", "");
      if (! NumberUtils.isNumber(term)) {
        return true;
      }
    }
    return false;
  }
  
//  @Override
//  public Token next(Token token) throws IOException {
//    while ((token = input.next(token)) != null) {
//      String term = token.term();
//      term = term.replaceAll(",", "");
//      if (! NumberUtils.isNumber(term)) {
//        return token;
//      }
//    }
//    return null;
//  }
}
