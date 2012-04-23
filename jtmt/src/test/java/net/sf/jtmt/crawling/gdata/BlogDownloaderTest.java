// $Id$
// $Source$
package net.sf.jtmt.crawling.gdata;

import org.junit.Test;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal
 * @version $Revision$
 */
public class BlogDownloaderTest {

  @Test
  public void testDownload() throws Exception {
    BlogDownloader downloader = new BlogDownloader();
    downloader.setBloggerEmail("google_email");
    downloader.setBloggerPasswd("google_pass");
    downloader.setBloggerFeedUrl("http://sujitpal.blogspot.com/feeds/posts/default");
    downloader.setUserAgent("salmonrun-bloggerclient-j-0.1");
    downloader.setCatalogFile("catalog.txt");
    downloader.setCategoryFile("category.txt");
    downloader.setDownloadDirectory("src/test/resources/data/blog");
    downloader.download();
  }
}
