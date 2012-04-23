// $Id$
package net.sf.jtmt.ontology.graph;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.api.core.Node;

/**
 * Test case for NeoDb Navigator.
 * @author Sujit Pal 
 * @version $Revision$
 */
public class NeoOntologyNavigatorTest {
  
  private final Log log = LogFactory.getLog(getClass());
  private static final String NEODB_PATH = "/tmp/neodb";
  private static NeoOntologyNavigator navigator;
  
  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    navigator = new NeoOntologyNavigator(NEODB_PATH);
    navigator.init();
  }
  
  @AfterClass
  public static void teardownAfterClass() throws Exception {
    navigator.destroy();
  }
  
  @Test
  public void testWhereIsLoireRegion() throws Exception {
    log.info("query> where is LoireRegion?");
    Node loireRegionNode = navigator.getByName("LoireRegion");
    if (loireRegionNode != null) {
      List<Node> locations = navigator.getNeighborsRelatedBy(
        loireRegionNode, OntologyRelationshipType.LOCATED_IN);
      for (Node location : locations) {
        log.info(location.getProperty(NeoOntologyNavigator.FIELD_ENTITY_NAME));
      }
    }
  }
  
  @Test
  public void testWhatRegionsAreInUsRegion() throws Exception {
    log.info("query> what regions are in USRegion?");
    Node usRegion = navigator.getByName("USRegion");
    if (usRegion != null) {
      List<Node> locations = navigator.getNeighborsRelatedBy(
        usRegion, OntologyRelationshipType.REGION_CONTAINS);
      for (Node location : locations) {
        log.info(location.getProperty(NeoOntologyNavigator.FIELD_ENTITY_NAME));
      }
    }
  }
  
  @Test
  public void testWhatAreSweetWines() throws Exception {
    log.info("query> what are Sweet wines?");
    Node sweetNode = navigator.getByName("Sweet");
    if (sweetNode != null) {
      List<Node> sweetWines = navigator.getNeighborsRelatedBy(
        sweetNode, OntologyRelationshipType.IS_SUGAR_CONTENT_OF);
      for (Node sweetWine : sweetWines) {
        log.info(sweetWine.getProperty(NeoOntologyNavigator.FIELD_ENTITY_NAME));
      }
    }
  }

  @Test
  public void testShowNeighborsForAReislingWine() throws Exception {
    log.info("query> show neighbors for SchlossVolradTrochenbierenausleseRiesling");
    Node rieslingNode = navigator.getByName("SchlossVolradTrochenbierenausleseRiesling");
    Map<String,List<Node>> neighbors = navigator.getAllNeighbors(rieslingNode);
    for (String relType : neighbors.keySet()) {
      log.info("--- " + relType + " ---");
      List<Node> relatedNodes = neighbors.get(relType);
      for (Node relatedNode : relatedNodes) {
        log.info(relatedNode.getProperty(NeoOntologyNavigator.FIELD_ENTITY_NAME));
      }
    }
  }
}
