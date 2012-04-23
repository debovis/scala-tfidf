// $Id$
package net.sf.jtmt.phrase.filters;

/**
 * Uses the ratio of the likelihood of an dependence hypothesis to the
 * likelihood of the independence hypothesis for phrase bigrams to 
 * filter out unlikely phrases. 
 * @author Sujit Pal
 * @version $Revision$
 */
public class LikelihoodRatioPhraseFilter implements IPhraseFilter {

  private long n;    // number of words in the corpus
  private long n1;   // number of occurrences of leading word in corpus
  private long n2;   // number of occurrences of trailing word in corpus
  private long n12;  // number of occurrences of phrase in corpus
  
  public LikelihoodRatioPhraseFilter(long n, long n1, long n2, long n12) {
    this.n = n;
    this.n1 = n1;
    this.n2 = n2;
    this.n12 = n12;
  }
  
  /**
   * Returns the log of the likelihood ratio between the dependence hypothesis
   * and the independence hypothesis. If the value is positive, then the 
   * dependence hypothesis is more likely than the independence hypothesis.
   */
  @Override
  public double getPhraseDeterminant() {
    double p00 = (double) n2 / (double) n;
    double p01 = p00;
    double p10 = (double) n12 / (double) n1;
    double p11 = (double) (n2 - n12) / (double) (n - n1);
    double llr = n12 * (Math.log(p10) - Math.log(p00)) + 
      (n2 - n12) * (Math.log(1 - p10) - Math.log(1 - p00)) + 
      (n12 - n2) * (Math.log(p01) - Math.log(p11)) + 
      (n - n1 - n12 + n2) * (Math.log(1 - p01) - Math.log(1 - p11));
    return llr;
  }
}
