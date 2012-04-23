package net.sf.jtmt.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jtmt.summarizers.SummaryAnalyzer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
import org.junit.Test;

import pitt.search.semanticvectors.DocVectors;
import pitt.search.semanticvectors.LuceneUtils;
import pitt.search.semanticvectors.ObjectVector;
import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.TermVectorsFromLucene;
import pitt.search.semanticvectors.VectorSearcher;
import pitt.search.semanticvectors.VectorStore;
import pitt.search.semanticvectors.VectorStoreReaderText;
import pitt.search.semanticvectors.VectorStoreWriter;

/**
 * Test case for this stuff.
 * @author Sujit Pal
 * @version $Revision$
 */
public class SemanticVectorFeatureSelectorTest {

  private static final Log log = LogFactory.getLog(SemanticVectorFeatureSelectorTest.class);
  
  private static String INPUT_FILE = "src/test/resources/data/sugar-coffee-cocoa-docs.txt";
  private static String INDEX_DIR = "src/test/resources/data/scc-index";
  
  private static Map<String,IndexWriter> writerMap;
  private static Map<String,File> pathMap;

//  @BeforeClass
  public static void buildIndex() throws Exception {
    log.debug("Building index...");
    buildIndexWriters(new String[] {"cocoa", "coffee", "sugar"});
    BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE));
    String line = null;
    int lno = 0;
    StringBuilder bodybuf = new StringBuilder();
    String category = null;
    String filename = null;
    while ((line = reader.readLine()) != null) {
      if (line.endsWith(".sgm")) {
        // header line
        filename = StringUtils.trim(StringUtils.split(line, "--")[2]);
        if (lno > 0) {
          // not the very first line, so dump current body buffer and
          // reinit the buffer.
          writeIndex(filename, category, bodybuf.toString());
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
    writeIndex(filename, category, bodybuf.toString());
    closeIndexes();
    reader.close();
    buildSemanticVectorData();
  }

  private static void buildIndexWriters(String[] categories) 
      throws IOException {
    writerMap = new HashMap<String,IndexWriter>();
    pathMap = new HashMap<String,File>();
    for (String category : categories) {
      File path = new File(INDEX_DIR, category);
      pathMap.put(category, path);
      writerMap.put(category, new IndexWriter(FSDirectory.open(path),
        new SummaryAnalyzer(), MaxFieldLength.UNLIMITED));
    }
  }

  private static void writeIndex(String filename, String category, String body) 
      throws Exception {
    Document doc = new Document();
    doc.add(new Field("filename", filename, Store.YES, Index.NOT_ANALYZED));
    doc.add(new Field("category", category, Store.YES, Index.NOT_ANALYZED));
    doc.add(new Field("contents", body, Store.YES, Index.ANALYZED, TermVector.YES));
    IndexWriter writer = writerMap.get(category);
    if (writer != null) {
      writer.addDocument(doc);
    }
  }

  private static void closeIndexes() throws IOException {
    for (IndexWriter writer : writerMap.values()) {
      writer.commit();
      writer.optimize();
      writer.close();
    }
  }

  private static void buildSemanticVectorData() throws IOException {
    // TODO Auto-generated method stub
    int minTermLength = 5;
    int minTermFreq = 5;
    String[] fields = new String[] {"contents"};
    int numTrainingCycles = 5;
    for (String category : pathMap.keySet()) {
      String termFile = FilenameUtils.concat(INDEX_DIR, "term_" + category + ".bin");
      String docFile = FilenameUtils.concat(INDEX_DIR, "doc_" + category + ".bin");
      String indexDir = pathMap.get(category).getAbsolutePath();
      TermVectorsFromLucene ltv = new TermVectorsFromLucene(
        indexDir, minTermLength, minTermFreq, null, fields);
      DocVectors dv = new DocVectors(ltv);
      for (int i = 0; i < numTrainingCycles; i++) {
        VectorStore tv = ltv.getBasicDocVectors();
        ltv = new TermVectorsFromLucene(indexDir, minTermLength, minTermFreq, tv, fields);
        dv = new DocVectors(ltv);
      }
      VectorStore wdv = dv.makeWriteableVectorStore();
      VectorStoreWriter writer = new VectorStoreWriter();
      writer.WriteVectorsAsText(termFile, ltv);
      writer.WriteVectorsAsText(docFile, dv);
      log.debug("Created doc and term vectors for category: " + category);
    }
  }

//  @AfterClass
  public static void deleteIndex() throws Exception {
    log.info("Deleting index directories...");
    FileUtils.deleteDirectory(new File(INDEX_DIR));
  }
  
  @Test
  public void testSelection() throws Exception {
    Assert.assertTrue(true);
    VectorStore vec1 = new VectorStoreReaderText("src/test/resources/data/scc-index/term_cocoa.bin");
    VectorStore vec2 = new VectorStoreReaderText("src/test/resources/data/scc-index/doc_cocoa.bin");
    LuceneUtils lutils = new LuceneUtils("src/test/resources/data/scc-index/cocoa");
    int numResults = 100;
    String[] qt = new String[] {"cocoa", "coffee", "sugar"};
    VectorSearcher searcher = new VectorSearcher.VectorSearcherCosine(vec1, vec2, lutils, qt);
    List<SearchResult> results = searcher.getNearestNeighbors(numResults);
    for (SearchResult result : results) {
      System.out.println(((ObjectVector) result.getObject()).getObject() + " => " + result.getScore());
    }
  }
}
