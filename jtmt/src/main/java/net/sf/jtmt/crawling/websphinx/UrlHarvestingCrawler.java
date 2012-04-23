// $Id$
// $Source$
package net.sf.jtmt.crawling.websphinx;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

import websphinx.Crawler;
import websphinx.Link;
import websphinx.Page;

/**
 * This crawls a given page, usually a directory page of a website, and lists
 * all the URLs it finds in its crawl. The URLs will presumably be passed to 
 * another component for analysis.
 * @author Sujit Pal
 * @version $Revision$
 */
public class UrlHarvestingCrawler extends MyCrawler {

  private static final long serialVersionUID = 9015164947202781853L;
  private static final String URL_PATTERN = "results.asp";

  private PrintWriter output;
  
  protected void init() throws Exception {
    output = new PrintWriter(new OutputStreamWriter(new FileOutputStream("/tmp/urls.txt")), true);
  }
  
  protected void destroy() {
    output.flush();
    output.close();
  }
  
  @Override
  protected void doVisit(Page page) {
    URL url = page.getURL();
    log.debug("url = " + url.toString());
    output.println(url.toString());
  }
  
  @Override
  public boolean shouldVisit(Link link) {
    URL linkUrl = link.getURL();
    return (linkUrl.toString().contains(URL_PATTERN));
  }
  
  /**
   * This is how we are called.
   * @param argv command line args.
   */
  public static void main(String[] argv) {
    UrlHarvestingCrawler crawler = new UrlHarvestingCrawler();
    try {
      crawler.init();
      crawler.setRoot(new Link(new URL("http://health.nih.gov/topics.asp/A")));
      crawler.setDomain(Crawler.SERVER); // reset this since we are interested in siblings
      crawler.setMaxDepth(2); // only crawl 2 levels deep, default 5
      crawler.run();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      crawler.destroy();
    }
  }
}
