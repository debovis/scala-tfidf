package net.sf.jtmt.crawling.gdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.TextContent;

/**
 * Simple test case to upload locally updated blogger pages back to Blogger.
 */
public class GDataBloggerUploadTest {

  private static final String BLOGGER_EMAIL = "google_email";
  private static final String BLOGGER_PASSWD = "google_pass";
  private static final String DOWNLOAD_DIR = "src/test/resources/data/blog";
  private static final String UPLOAD_DIR = "/tmp/blog";
  private static final String FEED_URL = "http://blogger.feed.url/";
  private static final String BLOG_ID = "your_blog_id";
  
  private static final SimpleDateFormat TS_FORMATTER = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss");

//  @Test
  public void testUploadByPubdate() throws Exception {
    GoogleService service = new GoogleService("blogger", "salmonrun-bloggerclient-j-0.1");
    // login
    service.setUserCredentials(BLOGGER_EMAIL, BLOGGER_PASSWD);
    // read catalog file
    BufferedReader catalogReader = new BufferedReader(new FileReader(
      DOWNLOAD_DIR + "/catalog.txt"));
    String catalogLine;
    // read through the catalog file for metadata
    while ((catalogLine = catalogReader.readLine()) != null) {
      String[] cols = StringUtils.split(catalogLine, "|");
      String id = cols[0];
      String pubDate = cols[1];
      String pubUrl = cols[2];
      String title = cols[3];
      // check to see if the file needs to be uploaded (if not available,
      // then it does not need to be uploaded).
      File uploadFile = new File(UPLOAD_DIR + "/" + id + ".txt");
      if (! uploadFile.exists()) {
        System.out.println("Skipping post (" + id + "): " + title + ", no changes");
        continue;
      }
      System.out.println("Uploading post (" + id + "): " + title);
      // suck out all the data into a data buffer
      BufferedReader uploadReader = new BufferedReader(new FileReader(
        UPLOAD_DIR + "/" + id + ".txt"));
      StringBuilder uploadDataBuffer = new StringBuilder();
      String uploadLine;
      while ((uploadLine = uploadReader.readLine()) != null) {
        uploadDataBuffer.append(uploadLine).append("\n");
      }
      uploadReader.close();
      // retrieve the post
      long pubMinAsLong = TS_FORMATTER.parse(pubDate).getTime();
      DateTime pubMin = new DateTime(pubMinAsLong);
      DateTime pubMax = new DateTime(pubMinAsLong + 3600000L); // 1 hour after
      URL feedUrl = new URL(FEED_URL);
      Query query = new Query(feedUrl);
      query.setPublishedMin(pubMin);
      query.setPublishedMax(pubMax);
      Feed result = service.query(query, Feed.class);
      List<Entry> entries = result.getEntries();
      if (entries.size() != 1) {
        System.out.println("Invalid number of entries: " + entries.size() + ", skip: " + id);
        continue;
      }
      Entry entry = entries.get(0);
      // then stick the updated content into the post
      entry.setContent(new TextContent(
        new HtmlTextConstruct(uploadDataBuffer.toString())));
      // then upload
      service.update(new URL(pubUrl), entry);
      // rename them so they are not picked up next time round
      uploadFile.renameTo(new File(UPLOAD_DIR + "/" + id + ".uploaded"));
    }
    catalogReader.close();
  }
  
//  @Test
  public void testUploadAll() throws Exception {
    GoogleService service = new GoogleService("blogger", "salmonrun-bloggerclient-j-0.1");
    // login
    service.setUserCredentials(BLOGGER_EMAIL, BLOGGER_PASSWD);
    // read catalog file
    BufferedReader catalogReader = new BufferedReader(new FileReader(
      DOWNLOAD_DIR + "/catalog.txt"));
    String catalogLine;
    // read through the catalog file for metadata, and build a set of 
    // entries to upload
    Set<String> ids = new HashSet<String>();
    while ((catalogLine = catalogReader.readLine()) != null) {
      String[] cols = StringUtils.split(catalogLine, "|");
      String id = cols[0];
      // check to see if the file needs to be uploaded (if not available,
      // then it does not need to be uploaded).
      File uploadFile = new File(UPLOAD_DIR + "/" + id + ".txt");
      if (! uploadFile.exists()) {
        continue;
      }
      ids.add("tag:blogger.com,1999:blog-" + BLOG_ID + ".post-" + id);
    }
    catalogReader.close();
    System.out.println("#-entries to upload: " + ids.size());
    // now get all the posts
    URL feedUrl = new URL(FEED_URL);
    Query query = new Query(feedUrl);
    query.setPublishedMin(new DateTime(TS_FORMATTER.parse("2005-01-01T00:00:00")));
    query.setPublishedMax(new DateTime(TS_FORMATTER.parse("2009-12-31T00:00:00")));
    query.setMaxResults(1000); // I just have about 150, so this will cover everything
    Feed result = service.query(query, Feed.class);
    List<Entry> entries = result.getEntries();
    for (Entry entry : entries) {
      String id = entry.getId();
      if (! ids.contains(id)) {
        continue;
      }
      String title = entry.getTitle().getPlainText();
      // get contents to update
      String fn = id.substring(id.lastIndexOf('-') + 1);
      System.out.println(">>> Uploading entry (" + id + "): [" + title + "] from file: " + 
        fn + ".txt");
      File uploadFile = new File(UPLOAD_DIR, fn + ".txt");
      if (! uploadFile.exists()) {
        System.out.println("Upload file does not exist: " + uploadFile.toString());
        continue;
      }
      String contents = FileUtils.readFileToString(uploadFile, "UTF-8");
      if (StringUtils.trim(contents).length() == 0) {
        System.out.println("Zero bytes for " + fn + ", skipping");
        continue;
      }
      // then stick the updated content into the post
      entry.setContent(new TextContent(
        new HtmlTextConstruct(contents)));
      String publishUrl = entry.getEditLink().getHref();
      // then upload
      service.update(new URL(publishUrl), entry);
    }
  }
  
  @Test
  public void testFindEmptyBlogs() throws Exception {
    GoogleService service = new GoogleService("blogger", "salmonrun-bloggerclient-j-0.1");
    // login
    service.setUserCredentials(BLOGGER_EMAIL, BLOGGER_PASSWD);
    // get all posts
    URL feedUrl = new URL(FEED_URL);
    Query query = new Query(feedUrl);
    query.setPublishedMin(new DateTime(TS_FORMATTER.parse("2005-01-01T00:00:00")));
    query.setPublishedMax(new DateTime(TS_FORMATTER.parse("2009-12-31T00:00:00")));
    query.setMaxResults(1000); // I just have about 150, so this will cover everything
    Feed result = service.query(query, Feed.class);
    List<Entry> entries = result.getEntries();
    for (Entry entry : entries) {
      String id = entry.getId();
      String title = entry.getTitle().getPlainText();
      String content = ((TextContent) entry.getContent()).getContent().getPlainText();
      if (StringUtils.trim(content).length() == 0) {
        String postId = id.substring(id.lastIndexOf('-') + 1);
        System.out.println(postId + " (" + title + ")");
      }
    }
  }
}

