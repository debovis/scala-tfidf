// $Id$
// $Source$
package net.sf.jtmt.indexers.lucenepayload.poi;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;

/**
 * Analyzer to parse out a string into item and payload.
 * @author Sujit Pal
 * @version $Revision$
 */
public class POIAnalyzer extends Analyzer {

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    // since our input is likely to be small peices of text in
    // the "tour" field, we just convert Reader to String (we
    // need to compute the number of terms in this string for
    // scoring purposes), then pass the String back via a StringReader
    int numTokens = 0;
    int c = 0;
    StringBuilder buf = new StringBuilder();
    try {
      while ((c = reader.read()) != -1) {
        buf.append((char) c);
        if ((char) c == ' ') {
          numTokens++;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new POITokenFilter(
      new WhitespaceTokenizer(new StringReader(buf.toString())), numTokens + 1);
  }
}
