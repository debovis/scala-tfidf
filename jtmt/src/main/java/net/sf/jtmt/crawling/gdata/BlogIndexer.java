// $Id$
// $Source$
package net.sf.jtmt.crawling.gdata;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.data.Category;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.HtmlTextConstruct;

/**
 * Create a Lucene index using the contents of my blog.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BlogIndexer {

  private final Log log = LogFactory.getLog(getClass());
  
  private static final SimpleDateFormat TS_FORMATTER = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss");

  private String bloggerEmail;
  private String bloggerPasswd;
  private String bloggerFeedUrl;
  private String userAgent;
  private String indexDirectory;
  
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

  public void setIndexDirectory(String indexDirectory) {
    this.indexDirectory = indexDirectory;
  }
  
  public void index() throws Exception {
    // connect to blogger
    GoogleService service = new GoogleService("blogger", userAgent);
    service.setUserCredentials(bloggerEmail, bloggerPasswd);
    URL feedUrl = new URL(bloggerFeedUrl);
    Query query = new Query(feedUrl);
    query.setPublishedMin(new DateTime(TS_FORMATTER.parse("2005-01-01T00:00:00")));
    query.setPublishedMax(new DateTime(TS_FORMATTER.parse("2009-12-31T00:00:00")));
    query.setMaxResults(1000); // I just have about 150+, so this will cover everything
    // fetch the results
    Feed result = service.query(query, Feed.class);
    List<Entry> entries = result.getEntries();
    // create an index writer
    IndexWriter indexWriter = new IndexWriter(
      FSDirectory.open(new File(indexDirectory)), 
      new StandardAnalyzer(Version.LUCENE_30), 
      true, MaxFieldLength.UNLIMITED);
    for (Entry entry : entries) {
      Document doc = new Document();
      // extract components of entry into strings and populate Doc
      String id = DigestUtils.md5Hex(entry.getId());
      doc.add(new Field("id", id, Store.YES, Index.NOT_ANALYZED, TermVector.NO));
      String title = entry.getTitle().getPlainText();
      doc.add(new Field("title", title, Store.YES, Index.ANALYZED, TermVector.YES));
      String url = entry.getHtmlLink().getHref();
      doc.add(new Field("url", url, Store.YES, Index.NOT_ANALYZED, TermVector.NO));
      Set<Category> categories = entry.getCategories();
      for (Category category : categories) {
        String tag = category.getTerm();
        doc.add(new Field("tags", tag, Store.YES, Index.NOT_ANALYZED, TermVector.NO));
      }
      HtmlTextConstruct hc = (HtmlTextConstruct) entry.getTextContent().getContent();
      String body = hc.getPlainText();
      doc.add(new Field("summary", StringUtils.abbreviate(body, 200), 
        Store.YES, Index.NO, TermVector.NO));
      doc.add(new Field("body", body, Store.YES, Index.ANALYZED, TermVector.YES));
      log.info(">>> Indexing post: " + title);
      indexWriter.addDocument(doc);
    }
    indexWriter.optimize();
    indexWriter.close();
  }
}
