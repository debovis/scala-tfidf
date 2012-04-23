// $Id$
package net.sf.jtmt.ontology.graph;

import net.sf.jtmt.ontology.graph.loaders.Owl2NeoLoader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * Test case for Owl2NeoLoader.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Owl2NeoLoaderTest {

  private static final String ROOT_NAME = "ConsumableThing";
  
  private final Log log = LogFactory.getLog(getClass());
  
  private static final String[][] SUB_ONTOLOGIES = new String[][] {
    new String[] {"wine.rdf", "Wine"},
    new String[] {"food.rdf", "EdibleThing"}
  };
  
  @Test
  public void testLoading() throws Exception {
    for (String[] subOntology : SUB_ONTOLOGIES) {
      log.info("Now processing " + subOntology[0]);
      Owl2NeoLoader loader = new Owl2NeoLoader();
      loader.setRefNodeName(ROOT_NAME);
      loader.setFilePath(FilenameUtils.concat(
        "/home/sujit/src/jtmt/src/main/resources", subOntology[0]));
      loader.setDbPath("/tmp/neodb");
      loader.setOntologyName(subOntology[1]);
      loader.load();
    }
  }
}
