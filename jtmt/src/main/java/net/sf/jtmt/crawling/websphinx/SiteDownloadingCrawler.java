// $Id$
// $Source$
package net.sf.jtmt.crawling.websphinx;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import websphinx.Link;
import websphinx.Page;

/**
 * Used to download a small public site as a test.
 * @author Sujit Pal
 * @version $Revision$
 */
public class SiteDownloadingCrawler extends MyCrawler {

  private static final long serialVersionUID = 64989986095789110L;

  private String targetDir;
  
  public void setTargetDir(String targetDir) {
    this.targetDir = targetDir;
  }
  
  private void init() throws Exception {
    File targetDirFile = new File(targetDir);
    if (targetDirFile.exists()) {
      FileUtils.forceDelete(targetDirFile);
    }
    FileUtils.forceMkdir(targetDirFile);
  }
  
  @Override
  protected void doVisit(Page page) {
    URL url = page.getURL();
    try {
      String path = url.getPath().replaceFirst("/", "");
      if (StringUtils.isNotEmpty(path)) {
        String targetPathName = FilenameUtils.concat(targetDir, path);
        File targetFile = new File(targetPathName);
        File targetPath = new File(FilenameUtils.getPath(targetPathName));
        if (! targetPath.exists()) {
          FileUtils.forceMkdir(targetPath);
        }
        FileUtils.writeByteArrayToFile(targetFile, page.getContentBytes());
      }
    } catch (Exception e) {
      log.error("Could not download url:" + url.toString(), e);
    }
  }
  
  /**
   * This is how we are called.
   * @param argv command line args.
   */
  public static void main(String[] argv) {
    SiteDownloadingCrawler crawler = new SiteDownloadingCrawler();
    try {
      crawler.setTargetDir("/tmp/some-public-site");
      crawler.init();
      crawler.setRoot(new Link(new URL("http://www.some-public-site.com")));
      crawler.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
