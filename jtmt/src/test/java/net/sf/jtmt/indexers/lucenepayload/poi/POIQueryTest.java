package net.sf.jtmt.indexers.lucenepayload.poi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.payloads.PayloadNearQuery;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

/**
 * Test to check that required functionality is met.
 * @author Sujit Pal
 * @version $Revision$
 */
public class POIQueryTest {

  private static final boolean SHOW_EXPLANATION = false;
  
  private Directory directory;
  
  private String[] data = new String[] {
    "p1 p2 p3 p4",
    "p1 p2 p3",
    "p4 p1 p2 p3",
    "p2 p1 p3",
    "p1 p4 p2 p3",
    "p1 p2",
    "p4 p2",
    "p4 p5"
  };

  @Test
  public void testSpanQuery() throws Exception {
    index(new WhitespaceAnalyzer());
    String[] pois = StringUtils.split("p1 p2 p3", " ");
    SpanQuery[] baseQueries = new SpanQuery[pois.length];
    for (int i = 0; i < pois.length; i++) {
      baseQueries[i] = new SpanTermQuery(new Term("tour", pois[i]));
    }
    // first grab the cases where all points of interest are there
    // with a slop of (pois.length - 1) unordered
    SpanNearQuery allPois = new SpanNearQuery(baseQueries, 
      (pois.length - 1), false);
    allPois.setBoost(2.0F);
    // then append all the cases where at least one of the POIs appear
    SpanQuery[] poiQueries = new SpanQuery[pois.length + 1];
    poiQueries[0] = allPois;
    for (int i = 1; i < poiQueries.length; i++) {
      poiQueries[i] = baseQueries[i - 1];
    }
    SpanOrQuery query = new SpanOrQuery(poiQueries);
    printHits(query, "testSpanQuery", null, null, SHOW_EXPLANATION);    
  }
  
  @Test
  public void testPayloadSpanQuery() throws Exception {
    index(new POIAnalyzer());
    String[] pois = StringUtils.split("p1 p2 p3", " ");
    SpanQuery[] baseQueries = new SpanQuery[pois.length];
    for (int i = 0; i < pois.length; i++) {
      baseQueries[i] = new PayloadTermQuery(new Term("tour", pois[i]), 
        new POISumPayloadFunction());
    }
    // first grab the cases where all points of interest are there
    // with a slop of (pois.length - 1) unordered, and use our payload
    // scores to influence the ordering
    PayloadNearQuery allPois = new PayloadNearQuery(baseQueries, 
      (pois.length - 1), false, new POISumPayloadFunction());
    allPois.setBoost(2.0F);
    Set<String> deduper = new HashSet<String>();
    printHits(allPois, "testPayloadSpanQuery", new POISimilarity(), 
      deduper, SHOW_EXPLANATION);    
    // then backfill with the results from a SpanOrQuery filtering out
    // results that have already appeared (using deduper)
    SpanQuery[] poiQueries = new SpanQuery[pois.length + 1];
    poiQueries[0] = allPois;
    for (int i = 1; i < poiQueries.length; i++) {
      poiQueries[i] = baseQueries[i - 1];
    }
    SpanOrQuery query = new SpanOrQuery(poiQueries);
    printHits(query, "testBackfillSpanQuery", null, deduper, SHOW_EXPLANATION);    
  }

  private void index(Analyzer analyzer) throws Exception {
    directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, 
      analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
    for (int i = 0; i < data.length; i++) {
      Document doc = new Document();
      doc.add(new Field("title", "Tour #" + i, Store.YES, Index.NO));
      doc.add(new Field("tour", data[i], Store.YES, Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
  }

  private void printHits(Query query, String testName, 
      Similarity similarity, Set<String> deduper, 
      boolean showExplanation) throws IOException {
    IndexSearcher searcher = new IndexSearcher(directory);
    if (similarity != null) {
      searcher.setSimilarity(similarity);
    }
    TopDocs topdocs = searcher.search(query, 10);
    ScoreDoc[] hits = topdocs.scoreDocs;
    System.out.println("==== Query: " + query.toString());
    System.out.println("==== Results for " + testName + " ====");
    for (int i = 0; i < hits.length; i++) {
      Document doc = searcher.doc(hits[i].doc);
      String title = doc.get("title");
      String tour = doc.get("tour");
      float score = hits[i].score;
      if (deduper != null) {
        if (deduper.contains(title)) {
          continue;
        } else {
          deduper.add(title);
        }
      }
      System.out.println(StringUtils.join(new String[] {
        String.valueOf(i),
        title,
        tour,
        String.valueOf(score)
      }, "  "));
      if (showExplanation) {
        System.out.println("EXPLANATION:" + searcher.explain(query, hits[i].doc));
      }
    }
    searcher.close();
  }
}
