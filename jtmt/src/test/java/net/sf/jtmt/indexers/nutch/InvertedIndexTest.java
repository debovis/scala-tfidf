// $Id$
package net.sf.jtmt.indexers.nutch;

import net.sf.jtmt.indexers.nutch.indexer2.InvertedIndex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for the inverted index data structure.
 * @author Sujit Pal
 * @version $Revision$
 */
public class InvertedIndexTest {

  private final Log log = LogFactory.getLog(getClass());
  
  private static final String TEST_TEXT = 
    "The deficit is the year-by-year gap between what the federal government " +
    "spends and the revenue it takes in. So even if the annual deficits are " +
    "cut, the total national debt will continue to grow. It now stands at just " +
    "over $10.8 trillion, according to the Department of the Treasury. Of that " +
    "amount, about $6.5 trillion is owed to individuals, corporations and " +
    "governments and other lenders both domestic and foreign, while $4.3 " +
    "trillion is owed for Social Security benefits, military and civil service " +
    "pensions and other government programs.";
  
  @Test
  public void testInvertedIndexOperations() throws Exception {
    InvertedIndex index = new InvertedIndex(TEST_TEXT);
    boolean exists = index.exists("total-national-debt");
    log.info("exists=" + exists);
    Assert.assertTrue(exists);
    int count1 = index.countOccurrencesOf("total national debt");
    log.info("count[1]=" + count1);
    Assert.assertEquals(1, count1);
    int count2 = index.countOccurrencesOf("trillion");
    log.info("count[2]=" + count2);
    Assert.assertEquals(3, count2);
  }
}
