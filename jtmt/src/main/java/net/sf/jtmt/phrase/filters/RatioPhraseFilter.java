// $Id$
package net.sf.jtmt.phrase.filters;

import org.apache.commons.math.util.MathUtils;

/**
 * Uses the ratio of estimated (via Binomial distribution) probability versus
 * the actual observed occurrence probability to find whether a sequence of
 * words should be considered as a phrase.
 * 
 * Using a Binomial Distribution, we try to estimate the probability of
 * observing the number of occurrences of the second word in the bigram preceded
 * by the first word, out of the number of occurrences of the second word alone.
 * 
 * This is then compared to the observed probability of the phrase in the
 * document corpus. If the ratio is above a threshold, then it is considered
 * to be a phrase.
 *
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public class RatioPhraseFilter implements IPhraseFilter {

  private long n;    // number of words in the corpus
  private long n1;   // number of occurrences of leading word in corpus
  private long n2;   // number of occurrences of trailing word in corpus
  private long n12;  // number of occurrences of phrase in corpus

  public RatioPhraseFilter(long n, long n1, long n2, long n12) {
    this.n = n;
    this.n1 = n;
    this.n2 = n2;
    this.n12 = n12;
  }

  @Override
  public double getPhraseDeterminant() {
    double actualLogProbability = Math.log(n12) - Math.log(n);
    double pTrailingWord = (double) n2 / (double) n; 
    double estimatedLogProbability = getEstimatedLogProbability(
      n2, pTrailingWord, n12);
    double diff = actualLogProbability - estimatedLogProbability;
    return diff;
  }
  
  private double getEstimatedLogProbability(long n, double p, long x) {
    return MathUtils.binomialCoefficientLog((int) n, (int) x) + 
      (x * Math.log(p)) + ((n - x) * Math.log(1 - p));
  }
}
