// $Id$
// $Source$
package net.sf.jtmt.crawling.gdata;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.data.Category;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.HtmlTextConstruct;

/**
 * Download my entire blog into a directory. A catalog file is used to 
 * keep track of the file name by title, and a category file is used 
 * to store the previously tagged categories.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BlogDownloader {

  private final Log log = LogFactory.getLog(getClass());
  
  private static final SimpleDateFormat TS_FORMATTER = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss");

  private String bloggerEmail;
  private String bloggerPasswd;
  private String bloggerFeedUrl;
  private String userAgent;
  private String catalogFile;
  private String categoryFile;
  private String downloadDirectory;
  
  public void setBloggerEmail(String bloggerEmail) {
    this.bloggerEmail = bloggerEmail;
  }

  public void setBloggerPasswd(String bloggerPasswd) {
    this.bloggerPasswd = bloggerPasswd;
  }

  public void setBloggerFeedUrl(String bloggerFeedUrl) {
    this.bloggerFeedUrl = bloggerFeedUrl;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }
  
  public void setCatalogFile(String catalogFile) {
    this.catalogFile = catalogFile;
  }

  public void setCategoryFile(String categoryFile) {
    this.categoryFile = categoryFile;
  }
  
  public void setDownloadDirectory(String downloadDirectory) {
    this.downloadDirectory = downloadDirectory;
  }

  public void download() throws Exception {
    File downloadDir = new File(downloadDirectory);
    if (downloadDir.exists() && downloadDir.isDirectory()) {
      FileUtils.deleteDirectory(downloadDir);
    }
    FileUtils.forceMkdir(downloadDir);
    PrintWriter catalogWriter = new PrintWriter(
      new FileWriter(new File(downloadDir, catalogFile)), true);
    PrintWriter categoryWriter = new PrintWriter(
      new FileWriter(new File(downloadDir, categoryFile)), true);
    GoogleService service = new GoogleService("blogger", userAgent);
    service.setUserCredentials(bloggerEmail, bloggerPasswd);
    URL feedUrl = new URL(bloggerFeedUrl);
    Query query = new Query(feedUrl);
    query.setPublishedMin(new DateTime(TS_FORMATTER.parse("2005-01-01T00:00:00")));
    query.setPublishedMax(new DateTime(TS_FORMATTER.parse("2009-12-31T00:00:00")));
    query.setMaxResults(1000); // I just have about 150+, so this will cover everything
    Feed result = service.query(query, Feed.class);
    List<Entry> entries = result.getEntries();
    for (Entry entry : entries) {
      String id = entry.getId();
      String title = entry.getTitle().getPlainText();
//      String url = entry.getHtmlLink().getHref();
      log.info(">>> Downloading post: " + title);
      catalogWriter.println(StringUtils.join(new String[] {
//          url, title
        id + ".html", title
      }, "|"));
      HtmlTextConstruct hc = (HtmlTextConstruct) entry.getTextContent().getContent();
      String body = hc.getHtml();
      File bf = new File(downloadDir, id + ".html");
      FileUtils.writeStringToFile(bf, body, "UTF-8");
      Set<Category> categories = entry.getCategories();
      for (Category category : categories) {
        categoryWriter.println(StringUtils.join(new String[] {
          id, category.getTerm()
        }, "|"));
      } // categories
    } // entries
    catalogWriter.flush();
    catalogWriter.close();
    categoryWriter.flush();
    categoryWriter.close();
  }
}
