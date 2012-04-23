package net.sf.jtmt.indexers.lucenepayload.converter;

import java.io.File;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

/**
 * Test for performance degradation with payloads (if any).
 * @author Sujit Pal
 * @version $Revision$
 */
public class PayloadPerformanceTest {

  private static final String[] data = {
    "2800541", // asthma
    "2790981", // breast cancer
    "5348177", // diabetes
    "2793084", // influenza
    "2800232", // diarrhea
  };
  private static final int MAX_ITERATIONS_PER_CONCEPT = 5;
  private static final int NUM_DOCS_TO_GET = 50;
  
  @Test
  public void testLogTimesForTermQueryApproach() throws Exception {
    IndexSearcher searcher = new IndexSearcher(
      FSDirectory.open(new File("/prod/web/data/adamindex.20100421/cpindex")));
    timeSearches(searcher, false, "Term Query with Custom Sort");
    searcher.close();
  }
  
  @Test
  public void testLogTimesForPayloadTermQueryApproach() throws Exception {
    IndexSearcher searcher = new IndexSearcher(
      FSDirectory.open(new File("/prod/web/data/adamindex.20101006/index")));
    searcher.setSimilarity(new MyPayloadSimilarity());
    timeSearches(searcher, true, "Payload Term Query with Relevance");
    searcher.close();
  }
  
  private void timeSearches(IndexSearcher searcher, boolean usePayload, 
      String title) throws Exception {
    System.out.println("== " + title + "==");
    for (int i = 0; i < data.length; i++) {
      long[] elapsed = new long[MAX_ITERATIONS_PER_CONCEPT - 1];
      int numResults = 0;
      for (int j = 0; j < MAX_ITERATIONS_PER_CONCEPT; j++) {
        long start = System.currentTimeMillis();
        Query query;
        Sort sort = null;
        if (usePayload) {
          query = new PayloadTermQuery(
            new Term("imuids", data[i]), new AveragePayloadFunction(), false);
          sort = Sort.RELEVANCE;
        } else {
          query = new TermQuery(new Term("imuid", data[i]));
          sort = new Sort(new SortField("maprelevancy", SortField.FLOAT));
        }
        ScoreDoc[] hits = searcher.search(query, null, NUM_DOCS_TO_GET, sort).scoreDocs;
        if (j > 0) {
          elapsed[j - 1] = System.currentTimeMillis() - start;
          numResults = hits.length;
        }
      }
      float average = 0.0F;
      for (int j = 0; j < MAX_ITERATIONS_PER_CONCEPT - 1; j++) {
        average += (float) elapsed[j];
      }
      average /= (float) (MAX_ITERATIONS_PER_CONCEPT - 1);
      System.out.println("Term: (" + data[i] + "): #-results=" + 
        numResults + ", search time(ms)=" + average);
    }
    searcher.close();
  }
}
