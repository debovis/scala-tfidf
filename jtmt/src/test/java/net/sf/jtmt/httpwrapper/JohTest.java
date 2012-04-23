// $Id$
package net.sf.jtmt.httpwrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal
 * @version $Revision$
 */
public class JohTest {

  private final Log log = LogFactory.getLog(getClass());
  
  @Test
  public void testLocalBlogDict() throws Exception {
    BlogDict dict = new BlogDict("/home/sujit/bin/blog_dict.txt");
    Set<String> labels = dict.getLabels();
    log.debug("labels=" + labels);
    Set<String> synonyms = dict.getSynonyms("crawling");
    log.debug("synonyms=" + synonyms);
    Set<String> categories = dict.getCategories("crawling");
    log.debug("categories=" + categories);
  }
  
  @Test
  public void testJohExpose() throws Exception {
    Map<String,Object> config = new HashMap<String,Object>();
    config.put(Joh.HTTP_PORT_KEY, new Integer(8080));
    Joh.expose(new BlogDictFacade("/home/sujit/bin/blog_dict.txt"), config);
  }
}
