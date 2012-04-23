package net.sf.jtmt.indexers.lucenepayload.converter;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.FloatEncoder;

/**
 * Analyzer to parse a string of pairs into a payload field.
 * @author Sujit Pal
 * @version $Revision$
 */
public class MyPayloadAnalyzer extends Analyzer {

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    return new DelimitedPayloadTokenFilter(
      new WhitespaceTokenizer(reader),
      '$', new FloatEncoder());
  }
}
