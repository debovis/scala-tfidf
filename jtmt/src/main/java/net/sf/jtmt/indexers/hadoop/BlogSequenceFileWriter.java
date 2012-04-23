// $Id$
package net.sf.jtmt.indexers.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 * Converts blog files downloaded from Blogger via the Blogger API
 * into Hadoop sequence files. Skips code blocks, tables, and 
 * lists in the post, and skips inlined HTML tags in the paragraphs.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BlogSequenceFileWriter {

  private final Log log = LogFactory.getLog(getClass());
  
  private String inputDir;
  private String outputDir;
  
  private Map<String,String> titleMap = new HashMap<String,String>();
  private Map<String,List<String>> categoryMap = 
    new HashMap<String,List<String>>();
  
  public BlogSequenceFileWriter(String inputDir, String outputDir) { 
    this.inputDir = inputDir;
    this.outputDir = outputDir;
  }
  
  protected void init() throws Exception {
    File titles = new File(inputDir, "catalog.txt");
    if (! titles.exists()) {
      throw new Exception("Can't find catalog.txt file");
    }
    BufferedReader titleReader = new BufferedReader(new FileReader(titles));
    String line = null;
    while ((line = titleReader.readLine()) != null) {
      String[] parts = StringUtils.split(line, "|");
      String contentId = extractContentId(parts[0]);
      this.titleMap.put(contentId, parts[1]);
    }
    titleReader.close();
    File categories = new File(inputDir, "category.txt");
    if (! categories.exists()) {
      throw new Exception("Can't find category.txt file");
    }
    BufferedReader categoryReader = new BufferedReader(new FileReader(categories));
    while ((line = categoryReader.readLine()) != null) {
      String[] parts = StringUtils.split(line, "|");
      String contentId = extractContentId(parts[0]);
      List<String> cats;
      if (categoryMap.containsKey(contentId)) {
        cats = categoryMap.get(contentId);
      } else {
        cats = new ArrayList<String>();
      }
      cats.add(parts[1]);
      categoryMap.put(contentId, cats);
    }
    categoryReader.close();
  }
  
  private String extractContentId(String filename) {
    // from category.txt:
    // tag:blogger.com,1999:blog-7583720.post-2221261245188111971
    // from catalog.txt:
    // tag:blogger.com,1999:blog-7583720.post-2221261245188111971.html
    if (filename.endsWith(".html")) {
      filename = StringUtils.replace(filename, ".html", "");
    }
    int hpos = filename.lastIndexOf('-');
    return filename.substring(hpos + 1, filename.length());
  }

  protected void generate() throws Exception {
    init();
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);
    Path outputPath = new Path(outputDir, "blog.seq");
    SequenceFile.Writer writer = SequenceFile.createWriter(
      fs, conf, outputPath, Text.class, MapWritable.class);
    File[] files = new File(inputDir).listFiles(new FileFilter() {
      public boolean accept(File file) {
        return file.getName().endsWith(".html");
      }
    });
    for (File file : files) {
      String contentId = extractContentId(file.getName());
      log.info("Reading (" + contentId + ")");
      String title = titleMap.get(contentId);
      // category is not necessary, some blogs don't have it
      String categories = "";
      if (categoryMap.containsKey(contentId)) {
        categories = StringUtils.join(
          categoryMap.get(contentId).iterator(), ",");
      }
      String content = readContent(file);
      MapWritable fields = new MapWritable();
      fields.put(new Text("title"), new Text(title));
      fields.put(new Text("categories"), new Text(categories));
      fields.put(new Text("content"), new Text(content));
      log.info("Writing (" + contentId + "): " + title);
      writer.append(new Text(contentId), fields);
    }
    writer.close();
  }

  private String readContent(File file) throws Exception {
    StringBuilder buf = new StringBuilder();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    while ((line = reader.readLine()) != null) {
      // consider only if line starts and ends with <p> tags, that
      // way we only consider paragraphs that are human written, ie
      // not code blocks or lists or tables.
      if (line.startsWith("<p>") && line.endsWith("</p>")) {
        // strip out inline <a>, <b> or <i> tags if they occur
        line = line.replaceAll("<.*?>", "");
        buf.append(line).append("\n");
      }
    }
    reader.close();
    return buf.toString();
  }
}
