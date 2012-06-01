package com.sparcedge.analytics.clustering;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Clusters a collection of documents using K-Means clustering algorithm.
 * Algorithm from Text Mining Application Programming, chapter 8:
 * 1) Select k documents from the collection to form k initial singleton
 *    clusters. Estimate k = floor(sqrt(N)).
 * 2) Repeat until termination conditions are satisfied:
 *    2.1) For every document d, find the cluster i whose centroid is 
 *         most similar, assign d to cluster i.
 *    2.2) For every cluster i, recompute the centroid based on the
 *         current member documents.
 *    2.3) Check for termination -- minimal or no change to the assignment
 *         of documents to clusters.
 * 3) Return list of cluster.
 * 
 * @author Sujit Pal
 * @version $Revision$
 */
public class KMeansClusterer {

  private final Log log = LogFactory.getLog(getClass());
  
  private String[] initialClusterAssignments = null;
  
  public void setInitialClusterAssignments(String[] documentNames) {
    this.initialClusterAssignments = documentNames;
  }
  
  public List<Cluster> cluster(DocumentCollection collection) {
    int numDocs = collection.size();
    int numClusters = 0;
    if (initialClusterAssignments == null) {
      // compute initial cluster assignments
      IdGenerator idGenerator = new IdGenerator(numDocs);
      numClusters = (int) Math.floor(Math.sqrt(numDocs));
      initialClusterAssignments = new String[numClusters];
      for (int i = 0; i < numClusters; i++) {
        int docId = idGenerator.getNextId();
        initialClusterAssignments[i] = collection.getDocumentNameAt(docId);
      }
    } else {
      numClusters = initialClusterAssignments.length;
    }

    // build initial clusters
    List<Cluster> clusters = new ArrayList<Cluster>();
    for (int i = 0; i < numClusters; i++) {
      Cluster cluster = new Cluster("C" + i);
      cluster.addDocument(initialClusterAssignments[i], 
        collection.getDocument(initialClusterAssignments[i]));
      clusters.add(cluster);
    }
    log.debug("..Initial clusters:" + clusters.toString());

    List<Cluster> prevClusters = new ArrayList<Cluster>();

    // Repeat until termination conditions are satisfied
    for (;;) {
      // For every cluster i, (re-)compute the centroid based on the
      // current member documents. (We have moved 2.2 above 2.1 because
      // this needs to be done before every iteration).
      RealMatrix[] centroids = new RealMatrix[numClusters];
      for (int i = 0; i < numClusters; i++) {
        RealMatrix centroid = clusters.get(i).getCentroid();
        centroids[i] = centroid;
      }
      // For every document d, find the cluster i whose centroid is 
      // most similar, assign d to cluster i. (If a document is 
      // equally similar from all centroids, then just dump it into 
      // cluster 0).
      for (int i = 0; i < numDocs; i++) {
        int bestCluster = 0;
        double maxSimilarity = Double.MIN_VALUE;
        RealMatrix document = collection.getDocumentAt(i);
        String docName = collection.getDocumentNameAt(i);
        for (int j = 0; j < numClusters; j++) {
          double similarity = clusters.get(j).getSimilarity(document);
          if (similarity > maxSimilarity) {
            bestCluster = j;
            maxSimilarity = similarity;
          }
        }
        for (Cluster cluster : clusters) {
          if (cluster.getDocument(docName) != null) {
            cluster.removeDocument(docName);
          }
        }
        clusters.get(bestCluster).addDocument(docName, document);
      }
      log.debug("..Intermediate clusters: " + clusters.toString());

      // Check for termination -- minimal or no change to the assignment
      // of documents to clusters.
      if (CollectionUtils.isEqualCollection(clusters, prevClusters)) {
        break;
      }
      prevClusters.clear();
      prevClusters.addAll(clusters);
    }
    // Return list of clusters
    log.debug("..Final clusters: " + clusters.toString());
    return clusters;
  }
}
