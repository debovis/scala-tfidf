package net.sf.jtmt.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.linear.RealMatrix;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Test;

/**
 * Test for the PageRanker.
 * @author Sujit Pal
 * @version $Revision$
 */
public class PageRankerTest {

  private final Log log = LogFactory.getLog(getClass());
  
  // ==================== Toy data test =============================
  
  @Test
  public void testRankWithToyData() throws Exception {
    Map<String,Boolean> linkMap = getLinkMapFromDatafile(
      "src/test/resources/pagerank_links.txt");
    PageRanker ranker = new PageRanker();
    ranker.setLinkMap(linkMap);
    ranker.setDocIds(Arrays.asList(new String[] {
      "1", "2", "3", "4", "5", "6", "7"
    }));
    ranker.setDampingFactor(0.85D);
    ranker.setConvergenceThreshold(0.001D);
    RealMatrix pageranks = ranker.rank();
    log.debug("pageRank=" + pageranks.toString());
  }

  private Map<String,Boolean> getLinkMapFromDatafile(String filename) throws Exception {
    Map<String,Boolean> linkMap = new HashMap<String,Boolean>();
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line;
    while ((line = reader.readLine()) != null) {
      if (StringUtils.isEmpty(line) || line.startsWith("#")) {
        continue;
      }
      String[] pairs = StringUtils.split(line, "\t");
      linkMap.put(pairs[0], Boolean.TRUE);
    }
    return linkMap;
  }

  // ===================== Real data test =============================
  
//  @Test
  public void testRankWithRealData() throws Exception {
    List<String> docIds = new ArrayList<String>();
    Map<String,Boolean> linkMap = getLinkMapFromCollection(
      "/home/sujit/src/prod/web/data/adamxmlsource.20080730a", docIds);
    PageRanker ranker = new PageRanker();
    ranker.setLinkMap(linkMap);
    ranker.setDocIds(docIds);
    ranker.setDampingFactor(0.85D);
    ranker.rank();
  }

  private Map<String,Boolean> getLinkMapFromCollection(String rootDir, 
      List<String> docIds) throws Exception {
    Map<String,Boolean> linkMap = new HashMap<String,Boolean>();
    SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
    XPath query = XPath.newInstance("//textLink");
    List<File> files = listFiles(rootDir);
    for (File file : files) {
      String sourceContentId = getContentId(file);
      Document doc = builder.build(file);
      List<Element> textLinkElements = (List<Element>) query.selectNodes(doc);
      for (Element textLinkElement : textLinkElements) {
        String targetContentId = StringUtils.join(new String[] {
          textLinkElement.getAttributeValue("projectTypeID"),
          textLinkElement.getAttributeValue("genContentID")
        }, "-");
        linkMap.put(StringUtils.join(new String[] {
          sourceContentId, targetContentId
        }, ","), Boolean.TRUE);
      }
    }
    return linkMap;
  }

  private List<File> listFiles(String rootDir) {
    List<File> files = new ArrayList<File>();
    File documentDir = new File(rootDir);
    if (! documentDir.isDirectory()) {
      files.add(documentDir);
      return files;
    }
    // recursively...
    listFiles_r(files, documentDir);
    return files;
  }
  
  private void listFiles_r(List<File> files, File dir) {
    File[] docs = dir.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return (pathname.isDirectory() || 
          pathname.getName().endsWith(".xml"));
      }
    });
    for (File doc : docs) {
      if (doc.isDirectory()) {
        listFiles_r(files, doc);
      } else {
        files.add(doc);
      }
    }
  }
  
  private String getContentId(File doc) {
    String fullpath = doc.getAbsolutePath();
    if (fullpath.contains("Health Illustrated Encyclopedia/1/")) {
      return ("1-" + FilenameUtils.getBaseName(fullpath));
    } else if (fullpath.contains("Health Illustrated Encyclopedia/2/")) {
      return ("2-" + FilenameUtils.getBaseName(fullpath));
    } else if (fullpath.contains("Health Illustrated Encyclopedia/3/")) {
      return ("3-" + FilenameUtils.getBaseName(fullpath));
    } else if (fullpath.contains("Surgery and Procedures")) {
      return ("13-" + FilenameUtils.getBaseName(fullpath));
    } else {
      return null;
    }
  }

}
