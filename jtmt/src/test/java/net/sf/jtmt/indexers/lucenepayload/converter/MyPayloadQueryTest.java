package net.sf.jtmt.indexers.lucenepayload.converter;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Functional test for payloads.
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public class MyPayloadQueryTest {

  private static IndexSearcher searcher;
  
  private static String[] data = {
    "p1$123.0 p2$2.0 p3$89.0",
    "p2$91.0 p1$5.0",
    "p3$56.0 p1$25.0",
    "p4$98.0 p5$65.0 p1$33.0"
  };

  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    Directory directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, 
      new MyPayloadAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);;
    for (int i = 0; i < data.length; i++) {
      Document doc = new Document();
      doc.add(new Field("title", "Document #" + i, Store.YES, Index.NO));
      doc.add(new Field("data", data[i], Store.YES, Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
    searcher = new IndexSearcher(directory);
    searcher.setSimilarity(new MyPayloadSimilarity());
  }

  @AfterClass
  public static void teardownAfterClass() throws Exception {
    if (searcher != null) {
      searcher.close();
    }
  }
  
  @Test
  public void testSingleTerm() throws Exception {
    PayloadTermQuery p1Query = new PayloadTermQuery(
      new Term("data", "p1"), new AveragePayloadFunction(), false);
    search(p1Query);
  }
  
  @Test
  public void testAndQuery() throws Exception {
    PayloadTermQuery p1Query = new PayloadTermQuery(
      new Term("data", "p1"), new AveragePayloadFunction(), false);
    PayloadTermQuery p2Query = new PayloadTermQuery(
      new Term("data", "p2"), new AveragePayloadFunction(), false);
    BooleanQuery query = new BooleanQuery();
    query.add(p1Query, Occur.MUST);
    query.add(p2Query, Occur.MUST);
    search(query);
  }
  
  @Test
  public void testOrQuery() throws Exception {
    PayloadTermQuery p1Query = new PayloadTermQuery(
      new Term("data", "p1"), new AveragePayloadFunction(), false);
    PayloadTermQuery p2Query = new PayloadTermQuery(
      new Term("data", "p2"), new AveragePayloadFunction(), false);
    BooleanQuery query = new BooleanQuery();
    query.add(p1Query, Occur.SHOULD);
    query.add(p2Query, Occur.SHOULD);
    search(query);
  }
  
  private void search(Query query) throws Exception {
    System.out.println("=== Running query: " + query.toString() + " ===");
    ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;
    for (int i = 0; i < hits.length; i++) {
      Document doc = searcher.doc(hits[i].doc);
      System.out.println(StringUtils.join(new String[] {
        doc.get("title"),
        doc.get("data"),
        String.valueOf(hits[i].score)
      }, "  "));
    }
  }
}
