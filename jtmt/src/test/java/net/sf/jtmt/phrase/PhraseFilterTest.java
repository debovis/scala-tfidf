// $Id$
package net.sf.jtmt.phrase;

import net.sf.jtmt.phrase.filters.ChiSquarePhraseFilter;
import net.sf.jtmt.phrase.filters.IPhraseFilter;
import net.sf.jtmt.phrase.filters.LikelihoodRatioPhraseFilter;
import net.sf.jtmt.phrase.filters.NormalApproximationPhraseFilter;
import net.sf.jtmt.phrase.filters.RatioPhraseFilter;

import org.junit.Test;

/**
 * Tests for the phrase filter implementations.
 * @author Sujit Pal
 * @version $Revision$
 */
public class PhraseFilterTest {

  int nNew = 1044;      // number of occurrences of first word
  int nYork = 597;      // number of occurrences of second word
  int nNewYork = 588;   // number of bigram occurrences
  int nWords = 1900000; // total words in document corpus

  @Test
  public void testRatioFilter() throws Exception {
    IPhraseFilter filter = new RatioPhraseFilter(nWords, nNew, nYork, nNewYork);
    double det = filter.getPhraseDeterminant();
    System.out.println("det(ratio)=" + det);
  }
  
  @Test
  public void testNormalApproximationFilter() throws Exception {
    IPhraseFilter filter = new NormalApproximationPhraseFilter(
      nWords, nNew, nYork, nNewYork);
    double det = filter.getPhraseDeterminant();
    System.out.println("det(normal)=" + det);
  }
  
  @Test
  public void testLikelihoodRatioFilter() throws Exception {
    IPhraseFilter filter = new LikelihoodRatioPhraseFilter(nWords, nNew, nYork, nNewYork);
    double det = filter.getPhraseDeterminant();
    System.out.println("det(llr)=" + det);
  }
  
  @Test
  public void testChiSquareFilter() throws Exception {
    IPhraseFilter filter = new ChiSquarePhraseFilter(nWords, nNew, nYork, nNewYork);
    double det = filter.getPhraseDeterminant();
    System.out.println("det(chi)=" + det);
  }
}
