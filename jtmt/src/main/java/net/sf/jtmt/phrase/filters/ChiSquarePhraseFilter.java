// $Id$
package net.sf.jtmt.phrase.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.inference.ChiSquareTest;
import org.apache.commons.math.stat.inference.ChiSquareTestImpl;

/**
 * Calculates the Chi-Squared statistic for the expected vs observed frequencies,
 * and finds if the statistic is higher than the critical value computed at
 * a 0.05 significance level (ie 95% coverage).
 */
public class ChiSquarePhraseFilter implements IPhraseFilter {

  private final Log log = LogFactory.getLog(getClass());
  
  private static final double ALPHA = 0.05D;
  
  private long n;    // number of 2 word phrases in corpus
  private long n1;   // number of occurrences of leading word in corpus
  private long n2;   // number of occurrences of trailing word in corpus
  private long n12;  // number of occurrences of phrase in corpus

  public ChiSquarePhraseFilter(long n, long n1, long n2, long n12) {
    this.n = n;
    this.n1 = n1;
    this.n2 = n2;
    this.n12 = n12;
  }
  
  @Override
  public double getPhraseDeterminant() {
    if (n1 < n12 || n2 < n12 || n < n12) {
      // TODO: fix bug in bigram generator
      return 0.0D;
    }
    // set up contingency table of observed frequencies
    RealMatrix obs = new Array2DRowRealMatrix(2, 2);
    obs.setEntry(0, 0, n12);
    obs.setEntry(0, 1, (n1 - n12));
    obs.setEntry(1, 0, (n2 - n12));
    obs.setEntry(1, 1, (n - n12));
    // compute marginal frequencies
    RealVector rowTotals = obs.getRowVector(0).add(obs.getRowVector(1));
    RealVector colTotals = obs.getColumnVector(0).add(obs.getColumnVector(1));
    double total = colTotals.getL1Norm();
    // flatten contingency table of observed frequencies
    long[] observed = new long[4];
    int k = 0;
    for (int i = 0; i < obs.getRowDimension(); i++) {
      for (int j = 0; j < obs.getColumnDimension(); j++) {
        observed[k++] = (long) obs.getEntry(i, j);
      }
    }
    // compute expected frequencies based on marginal frequencies
    double[] expected = new double[4];
    k = 0;
    for (int i = 0; i < obs.getRowDimension(); i++) {
      for (int j = 0; j < obs.getColumnDimension(); j++) {
        expected[k++] = 
          (double) colTotals.getEntry(i) * rowTotals.getEntry(j) / total;
      }
    }
    // find the test statistic
    ChiSquareTest test = new ChiSquareTestImpl();
    double chiSquare = test.chiSquare(expected, observed);
//    System.out.println("chi-square=" + chiSquare);
    // find the critical value
    ChiSquaredDistribution dist = new ChiSquaredDistributionImpl(6.0D);
    double criticalValue = 0.0D;
    try {
      criticalValue = dist.inverseCumulativeProbability(ALPHA);
    } catch (MathException e) {
      log.warn(e);
    }
//    System.out.println("criticalvalue=" + criticalValue);
    // return the difference between the test statistic and critical value
    return (chiSquare - criticalValue);
  }
}
