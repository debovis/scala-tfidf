package com.sparcedge.analytics.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sparcedge.analytics.similaritytypes.CosineSimilarity;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Provides utility methods to return documents as term vectors from
 * a Term Document Matrix.
 * @author Sujit Pal
 * @version $Revision$
 */
public class DocumentCollection {

  private RealMatrix tdMatrix;
  private Map<String,RealMatrix> documentMap;
  private List<String> documentNames;
  
  public DocumentCollection(RealMatrix tdMatrix, String[] docNames) {
    int position = 0;
    this.tdMatrix = tdMatrix;
    this.documentMap = new HashMap<String,RealMatrix>();
    this.documentNames = new ArrayList<String>();
    for (String documentName : docNames) {
      documentMap.put(documentName, 
        tdMatrix.getSubMatrix(0, tdMatrix.getRowDimension() - 1, position, position));
      documentNames.add(documentName);
      position++;
    }
  }

  public int size() {
    return documentMap.keySet().size();
  }
  
  public List<String> getDocumentNames() {
    return documentNames;
  }
  
  public String getDocumentNameAt(int position) {
    return documentNames.get(position);
  }
  
  public RealMatrix getDocumentAt(int position) {
    return documentMap.get(documentNames.get(position));
  }
  
  public RealMatrix getDocument(String documentName) {
    return documentMap.get(documentName);
  }
  
  public void shuffle() {
    Collections.shuffle(documentNames);
  }
  
  public Map<String,Double> getSimilarityMap() {
    Map<String,Double> similarityMap = new HashMap<String,Double>();
    CosineSimilarity similarity = new CosineSimilarity();
    RealMatrix similarityMatrix = similarity.transform(tdMatrix);
    for (int i = 0; i < similarityMatrix.getRowDimension(); i++) {
      for (int j = 0; j < similarityMatrix.getColumnDimension(); j++) {
        String sourceDoc = getDocumentNameAt(i);
        String targetDoc = getDocumentNameAt(j);
        similarityMap.put(StringUtils.join(new String[] {sourceDoc, targetDoc}, ":"),
          similarityMatrix.getEntry(i, j));
      }
    }
    return similarityMap;
  }
  
  public List<String> getNeighbors(String docName, Map<String,Double> similarityMap, int numNeighbors) {
    if (numNeighbors > size()) {
      throw new IllegalArgumentException("numNeighbors too large, max: " + size());
    }
    final Map<String,Double> differenceMap = new HashMap<String,Double>();
    List<String> neighbors = new ArrayList<String>();
    neighbors.addAll(documentNames);
    for (String documentName : documentNames) {
      String key = StringUtils.join(new String[] {docName, documentName}, ":");
      double difference = Math.abs(similarityMap.get(key) - 1.0D);
      differenceMap.put(documentName, difference);
    }
    Collections.sort(neighbors, new ByValueComparator<String,Double>(differenceMap));
    return neighbors.subList(0, numNeighbors + 1);
  }
}
