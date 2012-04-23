// $Id$
// $Source$
package net.sf.jtmt.crawling.websphinx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import websphinx.Crawler;
import websphinx.DownloadParameters;
import websphinx.Page;

/**
 * Sets appropriate download parameters to make the crawler well-behaved.
 * Also sets common parameters that is likely to be used by all my crawlers.
 * @author Sujit Pal
 * @version $Revision$
 */
public abstract class MyCrawler extends Crawler {

  private static final long serialVersionUID = 2383514014091378008L;

  protected final Log log = LogFactory.getLog(getClass());

  public MyCrawler() {
    super();
    DownloadParameters dp = new DownloadParameters();
    dp.changeObeyRobotExclusion(true);
    dp.changeUserAgent("WebSPHINX Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.4) WebSPHINX 0.5");
    setDownloadParameters(dp);
    setDomain(Crawler.SUBTREE);
    setLinkType(Crawler.HYPERLINKS);
  }
  
  @Override
  public void visit(Page page) {
    doVisit(page);
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {;}
  }
  
  protected abstract void doVisit(Page page);
}
