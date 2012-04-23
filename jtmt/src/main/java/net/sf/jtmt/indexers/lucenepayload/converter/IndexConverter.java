package net.sf.jtmt.indexers.lucenepayload.converter;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

/**
 * Converts existing Adam index pair into single index with payload
 * @author Sujit Pal 
 * @version $Revision$
 */
public class IndexConverter {

  private String conceptIndexPath;
  private String fulltextIndexPath;
  private String combinedIndexPath;
  
  public void setConceptIndexPath(String conceptIndexPath) {
    this.conceptIndexPath = conceptIndexPath;
  }

  public void setFulltextIndexPath(String fulltextIndexPath) {
    this.fulltextIndexPath = fulltextIndexPath;
  }

  public void setCombinedIndexPath(String combinedIndexPath) {
    this.combinedIndexPath = combinedIndexPath;
  }

  public void convert() throws Exception {
    IndexReader ftReader = IndexReader.open(
      FSDirectory.open(new File(fulltextIndexPath)));
    PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(
      new WhitespaceAnalyzer());
    analyzer.addAnalyzer("imuids", new MyPayloadAnalyzer());
    IndexSearcher cpSearcher = new IndexSearcher(
      FSDirectory.open(new File(conceptIndexPath)), 
      true);
    IndexWriter writer = new IndexWriter(
      FSDirectory.open(new File(combinedIndexPath)), 
      analyzer, true, MaxFieldLength.UNLIMITED);
    int maxCpDocs = cpSearcher.maxDoc();
    int maxFtDocs = ftReader.maxDoc();
    for (int i = 0; i < maxFtDocs; i++) {
      Document doc = ftReader.document(i);
      String url = doc.get("url");
      if (StringUtils.isEmpty(url)) {
        writer.addDocument(doc);
      } else {
        Map<String,Float> conceptmap = getConceptMap(cpSearcher, maxCpDocs, url);
        StringBuilder buf = new StringBuilder();
        int currentConcept = 0;
        for (String concept : conceptmap.keySet()) {
          if (currentConcept > 0) {
            buf.append(" ");
          }
          buf.append(StringUtils.join(new String[] {
            concept, String.valueOf(conceptmap.get(concept))
          }, "$"));
          currentConcept++;
        }
        doc.add(new Field("imuids", new StringReader(buf.toString())));
        System.out.println(StringUtils.join(new String[] {
          doc.get("domainTitle"),
          buf.toString()
        }, "  "));
        writer.addDocument(doc);
      }
    }
    cpSearcher.close();
    ftReader.clone();
    writer.optimize();
    writer.close();
  }

  private Map<String,Float> getConceptMap(IndexSearcher searcher, 
      int maxDocs, String url) throws Exception {
    Map<String,Float> conceptmap = new HashMap<String,Float>();
    Query urlQuery = new TermQuery(new Term("url", url));
    ScoreDoc[] hits = searcher.search(urlQuery, maxDocs).scoreDocs;
    for (int i = 0; i < hits.length; i++) {
      Document doc = searcher.doc(hits[i].doc);
      String imuid = doc.get("imuid");
      Float maprel = Float.valueOf(doc.get("maprelevancy"));
      conceptmap.put(imuid, maprel);
    }
    return conceptmap;
  }
}
