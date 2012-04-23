package net.sf.jtmt.summarizers;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.jtmt.tokenizers.lucene.NumericTokenFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * Special purpose analyzer that uses a chain of PorterStemFilter, StopFilter,
 * LowercaseFilter and StandardFilter to wrap a StandardTokenizer. The StopFilter
 * uses a custom stop word set.
 * @author Sujit Pal
 * @version $Revision$
 */
public class SummaryAnalyzer extends Analyzer {

  private Set<Object> stopset;
  
  public SummaryAnalyzer() throws IOException {
    String[] stopwords = filterComments(StringUtils.split(
      FileUtils.readFileToString(new File(
      "src/main/resources/stopwords.txt"), "UTF-8")));
    this.stopset = StopFilter.makeStopSet(stopwords, true);
  }
  
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    return new PorterStemFilter(
      new StopFilter(
        false, // enable_position_increment_default == false, for backward compat
        new LowerCaseFilter(
          new NumericTokenFilter(
            new StandardFilter(
              new StandardTokenizer(Version.LUCENE_30, reader)))), 
        stopset));
  }
  
  private String[] filterComments(String[] input) {
    List<String> stopwords = new ArrayList<String>();
    for (String stopword : input) {
      if (! stopword.startsWith("#")) {
        stopwords.add(stopword);
      }
    }
    return stopwords.toArray(new String[0]);
  }
}
