// $Id$
package net.sf.jtmt.inferencing.prolog;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases to exercise the Prolog Petstore JPL client.
 * @author Sujit Pal
 * @version $Revision$
 */
public class PrologPetstoreClientTest {
  
  private static PrologPetstoreClient CLIENT;
  
  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    CLIENT = new PrologPetstoreClient();
    CLIENT.init();
  }

  @AfterClass
  public static void teardownAfterClass() throws Exception {
    CLIENT.destroy();
  }
  
  @Test
  public void test5_0_0() throws Exception {
    Map<String,String> answers = ArrayUtils.toMap(new String[][] {
      new String[] {"how_many_food", "0"},
      new String[] {"add_a_tank", "0"}
    });
    System.out.println("?- checkout([5, 0, 0]).");
    PetstoreCart cart = CLIENT.checkout(new PetstoreCart(5, 0, 0), answers);
    System.out.println("?- cart(" + cart.prettyPrint() + ").");
    System.out.println();
  }

  @Test
  public void test10_0_0() throws Exception {
    Map<String,String> answers = ArrayUtils.toMap(new String[][] {
      new String[] {"how_many_food", "5"},
      new String[] {"add_a_tank", "0"}
    });
    System.out.println("?- checkout([10, 0, 0]).");
    PetstoreCart cart = CLIENT.checkout(new PetstoreCart(10, 0, 0), answers);
    System.out.println("?- cart(" + cart.prettyPrint() + ").");
    System.out.println();
  }

  @Test
  public void test10_0_0_1() throws Exception {
    Map<String,String> answers = ArrayUtils.toMap(new String[][] {
      new String[] {"how_many_food", "5"},
      new String[] {"add_a_tank", "1"}
    });
    System.out.println("?- checkout([10, 0, 0]).");
    PetstoreCart cart = CLIENT.checkout(new PetstoreCart(10, 0, 0), answers);
    System.out.println("?- cart(" + cart.prettyPrint() + ").");
    System.out.println();
  }

  @Test
  public void test0_10_0() throws Exception {
    Map<String,String> answers = ArrayUtils.toMap(new String[][] {
      new String[] {"how_many_food", "0"},
      new String[] {"add_a_tank", "0"}
    });
    System.out.println("?- checkout([0, 10, 0]).");
    PetstoreCart cart = CLIENT.checkout(new PetstoreCart(0, 10, 0), answers);
    System.out.println("?- cart(" + cart.prettyPrint() + ").");
    System.out.println();
  }
}
