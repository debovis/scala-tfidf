// $Id$
package net.sf.jtmt.ontology.graph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.Traverser;
import org.neo4j.api.core.Traverser.Order;

/**
 * Test to demonstrate sorting by relationship weights.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BrowserReturnableEvaluatorTest {

  private static final Object[][] QUADS = new Object[][] {
    new Object[] {"coke", RelTypes.GOES_WITH, 10.0F, "whopper"},
    new Object[] {"coke", RelTypes.GOES_WITH, 10.0F, "doubleWhopper"},
    new Object[] {"coke", RelTypes.GOES_WITH, 5.0F, "tripleWhopper"},
    new Object[] {"coke", RelTypes.HAS_INGREDIENTS, 10.0F, "water"},
    new Object[] {"coke", RelTypes.HAS_INGREDIENTS, 9.0F, "sugar"},
    new Object[] {"coke", RelTypes.HAS_INGREDIENTS, 2.0F, "carbonDioxide"},
    new Object[] {"coke", RelTypes.HAS_INGREDIENTS, 5.0F, "secretRecipe"}
  };
  
  private enum RelTypes implements RelationshipType {
    GOES_WITH,
    HAS_INGREDIENTS
  };
  
  private static NeoService neoService;
  private static Node coke;
  
  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    // load up the test data
    neoService = new EmbeddedNeo("/tmp/neotest");
    Transaction tx = neoService.beginTx();
    try {
      // drink nodes
      coke = neoService.createNode();
      coke.setProperty("name", "coke");
      for (Object[] quad : QUADS) {
        Node objectNode = neoService.createNode();
        objectNode.setProperty("name", (String) quad[3]);
        Relationship rel = 
          coke.createRelationshipTo(objectNode, (RelationshipType) quad[1]);
        rel.setProperty("name", ((RelationshipType) quad[1]).name());
        rel.setProperty("weight", (Float) quad[2]);
      }
      tx.success();
    } catch (Exception e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
  }
  
  @AfterClass
  public static void teardownAfterClass() throws Exception {
    if (neoService != null) {
      neoService.shutdown();
    }
  }
  
  @Test
  public void testCustomEvaluator() throws Exception {
    Transaction tx = neoService.beginTx();
    try {
      BrowserReturnableEvaluator customReturnEvaluator = 
        new BrowserReturnableEvaluator(coke);
      Traverser traverser = coke.traverse(
        Order.BREADTH_FIRST, 
        StopEvaluator.DEPTH_ONE, 
        customReturnEvaluator, 
        RelTypes.GOES_WITH, Direction.OUTGOING, 
        RelTypes.HAS_INGREDIENTS, Direction.OUTGOING);
      for (Iterator<Node> it = traverser.iterator(); it.hasNext();) {
        it.next();
      }
      Map<String,List<Node>> neighbors = customReturnEvaluator.getNeighbors();
      for (String relName : neighbors.keySet()) {
        System.out.println("-- " + relName + " --");
        List<Node> relatedNodes = neighbors.get(relName);
        for (Node relatedNode : relatedNodes) {
          System.out.println(relatedNode.getProperty("name"));
        }
      }
      tx.success();
    } catch (Exception e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
  }
}
