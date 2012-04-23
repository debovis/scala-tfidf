package net.sf.jtmt.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import net.sf.jtmt.summarizers.SummaryAnalyzer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal
 * @version $Revision$
 */
public class LuceneNaiveBayesClassifierTest {

  private static final Log log = LogFactory.getLog(LuceneNaiveBayesClassifierTest.class);
  
  private static String INPUT_FILE = "src/test/resources/data/sugar-coffee-cocoa-docs.txt";
  private static String INDEX_DIR = "src/test/resources/data/scc-index";
  private static String[] DOCS_TO_CLASSIFY = new String[] {
    "src/test/resources/data/cocoa.txt",
    "src/test/resources/data/cocoa1.txt",
    "src/test/resources/data/cocoa2.txt",
    "src/test/resources/data/coffee.txt",
    "src/test/resources/data/coffee1.txt"
  };

  @BeforeClass
  public static void buildIndex() throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE));
    IndexWriter writer = new IndexWriter(FSDirectory.open(new File(INDEX_DIR)), 
      new SummaryAnalyzer(), MaxFieldLength.UNLIMITED);
    String line = null;
    int lno = 0;
    StringBuilder bodybuf = new StringBuilder();
    String category = null;
    while ((line = reader.readLine()) != null) {
      if (line.endsWith(".sgm")) {
        // header line
        if (lno > 0) {
          // not the very first line, so dump current body buffer and
          // reinit the buffer.
          writeToIndex(writer, category, bodybuf.toString());
          bodybuf = new StringBuilder();
        }
        category = StringUtils.trim(StringUtils.split(line, ":")[1]);
        continue;
      } else {
        // not a header line, accumulate line into bodybuf
        bodybuf.append(line).append(" ");
      }
      lno++;
    }
    // last record
    writeToIndex(writer, category, bodybuf.toString());
    reader.close();
    writer.commit();
    writer.optimize();
    writer.close();
  }

  private static void writeToIndex(IndexWriter writer, String category, 
      String body) throws Exception {
    Document doc = new Document();
    doc.add(new Field("category", category, Store.YES, Index.NOT_ANALYZED));
    doc.add(new Field("body", body, Store.NO, Index.ANALYZED));
    writer.addDocument(doc);
  }

  @AfterClass
  public static void deleteIndex() throws Exception {
    log.info("Deleting index directory...");
    FileUtils.deleteDirectory(new File(INDEX_DIR));
  }
  
  @Test
  public void testLuceneNaiveBayesClassifier() throws Exception {
    LuceneNaiveBayesClassifier classifier = train(false, false);
    categorize(classifier, DOCS_TO_CLASSIFY);
  }
  
  @Test
  public void testLuceneNaiveBayesClassifier2() throws Exception {
    LuceneNaiveBayesClassifier classifier = train(true, false);
    categorize(classifier, DOCS_TO_CLASSIFY);
  }
  
  @Test
  public void testLuceneNaiveBayesClassifier3() throws Exception {
    LuceneNaiveBayesClassifier classifier = train(true, true);
    categorize(classifier, DOCS_TO_CLASSIFY);
  }
  
  @Test
  public void testLuceneNaiveBayesClassifier4() throws Exception {
    LuceneNaiveBayesClassifier classifier = train(false, true);
    categorize(classifier, DOCS_TO_CLASSIFY);
  }
  
  private LuceneNaiveBayesClassifier train(boolean enableFeatureSelection, 
      boolean preventOverfitting) throws Exception {
    System.out.println(">>> Training (featureSelection=" + enableFeatureSelection +
      ", preventOverfitting=" + preventOverfitting + ")");
    LuceneNaiveBayesClassifier classifier = new LuceneNaiveBayesClassifier();
    classifier.setIndexDir(INDEX_DIR);
    classifier.setCategoryFieldName("category");
    classifier.setMatchCategoryValue("cocoa");
    classifier.setSelectTopFeatures(enableFeatureSelection);
    classifier.setPreventOverfitting(preventOverfitting);
    classifier.setAnalyzer(new SummaryAnalyzer());
    classifier.train();
    return classifier;
  }
  
  private void categorize(LuceneNaiveBayesClassifier classifier,
      String[] testDocs) throws Exception {
    Map<String,double[]> trainingSet = classifier.getTrainingSet();
    double categoryDocRatio = classifier.getCategoryDocRatio();
    // classify new document
    for (String testDoc : testDocs) {
      File f = new File(testDoc);
      boolean isCocoa = classifier.classify(trainingSet, categoryDocRatio, 
        FileUtils.readFileToString(f, "UTF-8"));
      System.out.println(">>> File: " + f.getName() + " in category:'cocoa'? " + isCocoa); 
    }
  }
}
