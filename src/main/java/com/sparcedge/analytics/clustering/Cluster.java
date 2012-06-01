package com.sparcedge.analytics.clustering;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.rank.Max;

/**
 * Simple POJO to model a cluster. Contains various convenience methods
 * that are used by the clusterers.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Cluster {
  
  private final Log log = LogFactory.getLog(getClass());
  
  private String id;
  private Map<String,RealMatrix> docs = new LinkedHashMap<String,RealMatrix>();
  private List<String> docNames = new LinkedList<String>();
  
  private RealMatrix centroid = null;
  
  public Cluster(String id) {
    super();
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public Set<String> getDocumentNames() {
    return docs.keySet();
  }

  public String getDocumentName(int pos) {
    return docNames.get(pos);
  }
  
  public RealMatrix getDocument(String documentName) {
    return docs.get(documentName);
  }

  public RealMatrix getDocument(int pos) {
    return docs.get(docNames.get(pos));
  }
  
  public void addDocument(String docName, RealMatrix docMatrix) {
    docs.put(docName, docMatrix);
    docNames.add(docName);
    log.debug("...." + id + " += " + docName);
  }

  public void removeDocument(String docName) {
    docs.remove(docName);
    docNames.remove(docName);
    log.debug("...." + id + " -= " + docName);
  }

  public int size() {
    return docs.size();
  }
  
  public boolean contains(String docName) {
    return docs.containsKey(docName);
  }
  
  /**
   * Returns a document (term vector) consisting of the average of the 
   * coordinates of the documents in the cluster. Returns a null Matrix
   * if there are no documents in the cluster. 
   * @return the centroid of the cluster, or null if no documents have 
   * been added to the cluster.
   */
  public RealMatrix getCentroid() {
    if (docs.size() == 0) {
      return null;
    }
    RealMatrix d = docs.get(docNames.get(0));
    centroid = new OpenMapRealMatrix(d.getRowDimension(), d.getColumnDimension()); 
    for (String docName : docs.keySet()) {
      RealMatrix docMatrix = docs.get(docName);
      centroid = centroid.add(docMatrix);
    }
    centroid = centroid.scalarMultiply(1.0D / docs.size());
    return centroid;
  }

  /**
   * Returns the radius of the cluster. The radius is the average of the
   * square root of the sum of squares of its constituent document term
   * vector coordinates with that of the centroid.
   * @return the radius of the cluster.
   */
  public double getRadius() {
    double radius = 0.0D;
    if (centroid != null) {
      for (String docName : docNames) {
        RealMatrix doc = getDocument(docName);
        radius += doc.subtract(centroid).getFrobeniusNorm();
      }
    }
    return radius / docNames.size();
  }
  
  /**
   * Returns the Eucledian distance between the centroid of this cluster
   * and the new document.
   * @param doc the document to be measured for distance.
   * @return the eucledian distance between the cluster centroid and the 
   * document.
   */
  public double getEucledianDistance(RealMatrix doc) {
    if (centroid != null) {
      return doc.subtract(centroid).getFrobeniusNorm();
    }
    return 0.0D;
  }
  
  /**
   * Returns the maximum distance from the specified document to any of
   * the documents in the cluster.
   * @param doc the document to be measured for distance.
   * @return the complete linkage distance from the cluster.
   */
  public double getCompleteLinkageDistance(RealMatrix doc) {
    Max max = new Max();
    if (docs.size() ==0) {
      return 0.0D;
    }
    double[] distances = new double[docs.size()];
    for (int i = 0; i < distances.length; i++) {
      RealMatrix clusterDoc = docs.get(docNames.get(i));
      distances[i] = clusterDoc.subtract(doc).getFrobeniusNorm();
    }
    return max.evaluate(distances);
  }
  
  /**
   * Returns the cosine similarity between the centroid of this cluster
   * and the new document.
   * @param doc the document to be measured for similarity.
   * @return the similarity of the centroid of the cluster to the document.
   */
  public double getSimilarity(RealMatrix doc) {
    if (centroid != null) {
      double dotProduct = centroid.transpose().multiply(doc).getNorm();
      double normProduct = centroid.getFrobeniusNorm() * doc.getFrobeniusNorm();
      return dotProduct / normProduct;
    }
    return 0.0D;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Cluster)) {
      return false;
    }
    Cluster that = (Cluster) obj;
    String[] thisDocNames = this.getDocumentNames().toArray(new String[0]);
    String[] thatDocNames = that.getDocumentNames().toArray(new String[0]);
    if (thisDocNames.length != thatDocNames.length) {
      return false;
    }
    Arrays.sort(thisDocNames);
    Arrays.sort(thatDocNames);
    return ArrayUtils.isEquals(thisDocNames, thatDocNames);
  }
  
  @Override
  public int hashCode() {
    String[] docNames = getDocumentNames().toArray(new String[0]);
    Arrays.sort(docNames);
    return StringUtils.join(docNames, ",").hashCode();
  }
  
  @Override
  public String toString() {
    return id + ":" + docs.keySet().toString();
  }
}
