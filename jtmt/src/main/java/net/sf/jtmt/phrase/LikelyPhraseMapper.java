// $Id$
package net.sf.jtmt.phrase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.jtmt.phrase.db.DBConfiguration;
import net.sf.jtmt.phrase.filters.ChiSquarePhraseFilter;
import net.sf.jtmt.phrase.filters.IPhraseFilter;
import net.sf.jtmt.phrase.filters.LikelihoodRatioPhraseFilter;
import net.sf.jtmt.phrase.filters.NormalApproximationPhraseFilter;
import net.sf.jtmt.phrase.filters.RatioPhraseFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Reads in bigrams from the database and computes the likelihood of the
 * phrase using Binomial distribution. Writes out phrases which satisfy
 * the test.
 * @author Sujit Pal
 * @version $Revision$
 */
public class LikelyPhraseMapper 
    extends Mapper<LongWritable,PhraseCounterDbWritable,Text,DoubleWritable> {

  private static DBConfiguration dbconf;
  private static long nWords;
  private static long nBigrams;
  
  private static final PhraseFilters SELECTED_PHRASEFILTER = PhraseFilters.CHISQUARE;

  private enum PhraseFilters {
    RATIO(50.0F),           // observed prob 50x the estimated by Binomial
    NORMAL_APPROX(0.1257F), // observed values are outside 10% of mean
    LIKELIHOOD(0),          // log of observed to estimated likelihood ratios
    CHISQUARE(0);           // difference between chi-square and critical value
                            // for alpha=0.05.
    
    private float threshold;
    
    PhraseFilters(float threshold) {
      this.threshold = threshold;
    }
  };
  
  @Override
  public void setup(Context context) throws IOException, InterruptedException {
    dbconf = new DBConfiguration(context.getConfiguration());
    nWords = context.getConfiguration().getLong(
      PhraseExtractor.NUM_WORDS_IN_CORPUS_KEY, -1L);
    nBigrams = context.getConfiguration().getLong(
      PhraseExtractor.NUM_2WORD_PHRASES_IN_CORPUS_KEY, -1L);
  }

  @Override
  public void map(LongWritable key, PhraseCounterDbWritable value, Context context) 
      throws IOException, InterruptedException {
    String phrase = value.getPhrase();
//    System.out.println("phrase=" + phrase);
    long phraseOccurs = value.getOccurs();
    String[] words = StringUtils.split(phrase, " ");
    long trailingWordOccurs = getOccurrence(words[0]);
    long leadingWordOccurs = getOccurrence(words[1]);
    if (phraseOccurs > trailingWordOccurs) {
      // TODO: fix this bug, this is impossible, and points to a bug in
      // the NGram generator code.
    } else {
      IPhraseFilter phraseFilter = null;
      double det = 0.0D;
      switch (SELECTED_PHRASEFILTER) {
        case RATIO:
          phraseFilter = new RatioPhraseFilter(
            nWords, leadingWordOccurs, trailingWordOccurs, phraseOccurs);
          det = phraseFilter.getPhraseDeterminant();
          if (det > PhraseFilters.RATIO.threshold) {
            context.write(new Text(phrase), new DoubleWritable(det));
          }
          break;
        case NORMAL_APPROX:
          phraseFilter = new NormalApproximationPhraseFilter(
            nWords, leadingWordOccurs, trailingWordOccurs, phraseOccurs);
          det = phraseFilter.getPhraseDeterminant();
          if (det > PhraseFilters.NORMAL_APPROX.threshold) {
            context.write(new Text(phrase), new DoubleWritable(det));
          }
          break;
        case LIKELIHOOD:
          phraseFilter = new LikelihoodRatioPhraseFilter(
            nWords, leadingWordOccurs, trailingWordOccurs, phraseOccurs);
          det = phraseFilter.getPhraseDeterminant();
          if (det > PhraseFilters.LIKELIHOOD.threshold) {
            context.write(new Text(phrase), new DoubleWritable(det));
          }
          break;
        case CHISQUARE:
          phraseFilter = new ChiSquarePhraseFilter(
            nBigrams, leadingWordOccurs, trailingWordOccurs, phraseOccurs);
          det = phraseFilter.getPhraseDeterminant();
          if (det > PhraseFilters.CHISQUARE.threshold) {
            context.write(new Text(phrase), new DoubleWritable(det));
          }
          break;
      }
    }
  }

  private long getOccurrence(String word) throws IOException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = dbconf.getConnection();
      ps = conn.prepareStatement("select occurs " +
        "from my_phrase_counts where phrase = ?");
      ps.setString(1, word);
      rs = ps.executeQuery();
      while (rs.next()) {
        return rs.getLong(1);
      }
      return 0L;
    } catch (SQLException e) {
      throw new IOException(e);
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    } finally {
      if (rs != null) {
        try { rs.close(); } catch (SQLException e) {}
        try { ps.close(); } catch (SQLException e) {}
        try { conn.close(); } catch (SQLException e) {}
      }
    }
  }
}
