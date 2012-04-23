// $Id$
// $Source$
package net.sf.jtmt.crawling.gdata;

import org.junit.Test;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal
 * @version $Revision$
 */
public class BlogIndexerTest {

  @Test
  public void testDownload() throws Exception {
    BlogIndexer indexer = new BlogIndexer();
    indexer.setBloggerEmail("google_email");
    indexer.setBloggerPasswd("google_pass");
    indexer.setBloggerFeedUrl("http://sujitpal.blogspot.com/feeds/posts/default");
    indexer.setUserAgent("salmonrun-bloggerclient-j-0.1");
    indexer.setIndexDirectory("src/test/resources/data/index");
    indexer.index();
  }
}
