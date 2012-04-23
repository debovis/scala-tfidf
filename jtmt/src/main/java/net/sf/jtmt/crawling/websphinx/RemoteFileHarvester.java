// $Id$
// $Source$
package net.sf.jtmt.crawling.websphinx;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import websphinx.Link;

/**
 * Connects to a remote site protected by basic authentication, grabs all
 * the hyperlinks from it, and dumps the output to local disk.
 * @author Sujit Pal
 * @version $Revision$
 */
public class RemoteFileHarvester extends SiteDownloadingCrawler {

  private static final long serialVersionUID = 3466884716433043917L;
  
  /**
   * This is how we are called.
   * @param argv command line args.
   */
  public static void main(String[] argv) {
    RemoteFileHarvester crawler = new RemoteFileHarvester();
    try {
      crawler.setTargetDir("/tmp/usnews");
      URL rootUrl = new URL("http://staging4.usnews.com/hl_drop/");
      Authenticator.setDefault(new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication("hlcrawler", "hlcrawler".toCharArray());
        }
      });
      crawler.setRoot(new Link(rootUrl));
      crawler.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
