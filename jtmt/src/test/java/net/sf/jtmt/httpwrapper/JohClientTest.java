// $Id$
// $Source$
// $URL$
package net.sf.jtmt.httpwrapper;

import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal
 * @version $Revision$
 */
public class JohClientTest {

  private final Log log = LogFactory.getLog(getClass());
  
  @Test
  public void testGetLabel() throws Exception {
    JohClient client = new JohClient();
    Set<String> labels = (Set<String>) client.request(
      "http://localhost:8080/getlabels", Set.class);
    log.debug("labels=" + labels);
    Assert.assertNotNull(labels);
    Assert.assertTrue(labels.contains("crawling"));
  }
  
  @Test
  public void testGetSynonyms() throws Exception {
    JohClient client = new JohClient();
    Set<String> synonyms = (Set<String>) client.request(
      "http://localhost:8080/getsynonyms?label=crawling", Set.class);
    log.debug("synonyms(crawling)=" + synonyms);
    Assert.assertNotNull(synonyms);
    Assert.assertTrue(synonyms.contains("crawler"));
  }
  
  @Test
  public void testGetCategories() throws Exception {
    JohClient client = new JohClient();
    Set<String> categories = (Set<String>) client.request(
      "http://localhost:8080/getcategories?label=crawling", Set.class);
    log.debug("categories(crawling)=" + categories);
    Assert.assertNotNull(categories);
    Assert.assertTrue(categories.contains("lucene"));
  }
}
