// $Id$
package net.sf.jtmt.phrase.filters;

import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

/**
 * Uses the Binomial Distribution to find the expected probability of seeing
 * the phrase. This is done by estimating the probability of observing the
 * number of occurrences of the second word in the bigram preceded by the first
 * word, out of the number of occurrences of the second word alone.
 * 
 * We then calculate the mean and standard deviation of the Binomial distribution.
 * Since the Binomial Distribution can approximates a Normal Distribution in
 * certain cases, we assume that we can do this, and calculate the z-score for
 * the observed probability (which gives us how far it is from the mean in units
 * of standard deviation). The z-score is returned by the phrase filter, and
 * it is compared to a given confidence interval in the client.
 * 
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public class NormalApproximationPhraseFilter implements IPhraseFilter {

  private long n;    // number of words in the corpus
  private long n1;   // number of occurrences of leading word in corpus
  private long n2;   // number of occurrences of trailing word in corpus
  private long n12;  // number of occurrences of phrase in corpus

  public NormalApproximationPhraseFilter(long n, long n1, long n2, long n12) {
    this.n = n;
    this.n1 = n;
    this.n2 = n2;
    this.n12 = n12;
  }
  
  @Override
  public double getPhraseDeterminant() {
    double p2 = (double) n2 / (double) n;
    BinomialDistribution dist = new BinomialDistributionImpl((int) n2, p2);
    double estPhraseProbability = dist.probability(n12);
    double distMean = n2 * p2;
    double distStdDev = Math.sqrt(distMean * (1 - p2));
    double actualPhraseProbability = (double) n12 / (double) n;
    double zScore = (estPhraseProbability - distMean) / distStdDev;
    double diff = Math.abs((actualPhraseProbability - distMean) / zScore);
    return diff;
  }
}
