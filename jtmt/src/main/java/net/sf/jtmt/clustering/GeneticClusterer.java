package net.sf.jtmt.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 1) t = 0
 * 2) initialize population P(t)
 * 3) compute fitness P(t)
 * 4) t = t + 1
 * 5) if termination criterion achieved goto 10
 * 6) select P(t) from P(t-1)
 * 7) crossover P(t)
 * 8) mutate P(t)
 * 9) go to 3
 * 10) output best and stop
 *
 * @author Sujit Pal
 * @version $Revision$
 */
public class GeneticClusterer {

  private final Log log = LogFactory.getLog(getClass());
  
  private boolean randomizeData;
  private int numCrossoversPerMutation;
  private int maxGenerations;
  
  public void setRandomizeData(boolean randomizeData) {
    this.randomizeData = randomizeData;
  }
  
  public void setNumberOfCrossoversPerMutation(int ncpm) {
    this.numCrossoversPerMutation = ncpm;
  }

  public void setMaxGenerations(int maxGenerations) {
    this.maxGenerations = maxGenerations;
  }
  
  public List<Cluster> cluster(DocumentCollection collection) {
    // get initial clusters
    int k = (int) Math.floor(Math.sqrt(collection.size()));
    List<Cluster> clusters = new ArrayList<Cluster>();
    for (int i = 0; i < k; i++) {
      Cluster cluster = new Cluster("C" + i);
      clusters.add(cluster);
    }
    if (randomizeData) {
      collection.shuffle();
    }
    // load it up using mod partitioning, this is P(0)
    int docId = 0;
    for (String documentName : collection.getDocumentNames()) {
      int clusterId = docId % k;
      clusters.get(clusterId).addDocument(
        documentName, collection.getDocument(documentName));
      docId++;
    }
    log.debug("Initial clusters = " + clusters.toString());
    // holds previous cluster in the compute loop
    List<Cluster> prevClusters = new ArrayList<Cluster>();
    double prevFitness = 0.0D;
    int generations = 0;
    for (;;) {
      // compute fitness for P(t)
      double fitness = computeFitness(clusters);
      // if termination condition achieved, break and return clusters
      if (prevFitness > fitness) {
        clusters.clear();
        clusters.addAll(prevClusters);
        break;
      }
      // even if termination condition not met, terminate after the
      // maximum number of generations
      if (generations > maxGenerations) {
        break;
      }
      // do specified number of crossover operations for this generation
      for (int i = 0; i < numCrossoversPerMutation; i++) {
        crossover(clusters, collection, i);
        generations++;
      }
      // followed by a single mutation per generation
      mutate(clusters, collection);
      generations++;
      log.debug("..Intermediate clusters (" + generations + "): " + clusters.toString());
      // hold on to previous solution
      prevClusters.clear();
      prevClusters.addAll(clusters);
      prevFitness = computeFitness(prevClusters);
    }
    return clusters;
  }
  
  /**
   * Come up with something arbitary. Just compute the sum of the radii of
   * the clusters.
   * @param clusters
   * @return
   */
  private double computeFitness(List<Cluster> clusters) {
    double radius = 0.0D;
    for (Cluster cluster : clusters) {
      cluster.getCentroid();
      radius += cluster.getRadius();
    }
    return radius;
  }
  
  /**
   * Selects two random clusters from the list, then selects two cut-points
   * based on the minimum cluster size of the two clusters. Exchanges the
   * documents between the cut points.
   * @param clusters the clusters to operate on.
   * @param sequence the sequence number of the cross over operation.
   */
  public void crossover(List<Cluster> clusters, DocumentCollection collection, int sequence) {
    IdGenerator clusterIdGenerator = new IdGenerator(clusters.size());
    int[] clusterIds = new int[2];
    clusterIds[0] = clusterIdGenerator.getNextId();
    clusterIds[1] = clusterIdGenerator.getNextId();
    int minSize = 
      Math.min(clusters.get(clusterIds[0]).size(), clusters.get(clusterIds[1]).size());
    IdGenerator docIdGenerator = new IdGenerator(minSize);
    int[] cutPoints = new int[2];
    cutPoints[0] = docIdGenerator.getNextId();
    cutPoints[1] = docIdGenerator.getNextId();
    Arrays.sort(cutPoints);
    Cluster cluster1 = clusters.get(clusterIds[0]);
    Cluster cluster2 = clusters.get(clusterIds[1]);
    for (int i = 0; i < cutPoints[0]; i++) {
      String docName1 = cluster1.getDocumentName(i);
      String docName2 = cluster2.getDocumentName(i);
      cluster1.removeDocument(docName1);
      cluster2.addDocument(docName1, collection.getDocument(docName1));
      cluster2.removeDocument(docName2);
      cluster1.addDocument(docName2, collection.getDocument(docName2));
    }
    // leave the documents between the cut points alone
    for (int i = cutPoints[1]; i < minSize; i++) {
      String docName1 = cluster1.getDocumentName(i);
      String docName2 = cluster2.getDocumentName(i);
      cluster1.removeDocument(docName1);
      cluster2.addDocument(docName1, collection.getDocument(docName1));
      cluster2.removeDocument(docName2);
      cluster1.addDocument(docName2, collection.getDocument(docName2));
    }
    // rebuild the Cluster list, replacing the changed clusters.
    List<Cluster> crossoverClusters = new ArrayList<Cluster>();
    int clusterId = 0;
    for (Cluster cluster : clusters) {
      if (clusterId == clusterIds[0]) {
        crossoverClusters.add(cluster1);
      } else if (clusterId == clusterIds[1]) {
        crossoverClusters.add(cluster2);
      } else {
        crossoverClusters.add(cluster);
      }
      clusterId++;
    }
    clusters.clear();
    clusters.addAll(crossoverClusters);
  }
  
  /**
   * Exchanges a random document between two random clusters in the list.
   * @param clusters the clusters to operate on.
   */
  private void mutate(List<Cluster> clusters, DocumentCollection collection) {
    // choose two random clusters
    IdGenerator clusterIdGenerator = new IdGenerator(clusters.size());
    int[] clusterIds = new int[2];
    clusterIds[0] = clusterIdGenerator.getNextId();
    clusterIds[1] = clusterIdGenerator.getNextId();
    Cluster cluster1 = clusters.get(clusterIds[0]);
    Cluster cluster2 = clusters.get(clusterIds[1]);
    // choose two random documents in the clusters
    int minSize = 
      Math.min(clusters.get(clusterIds[0]).size(), clusters.get(clusterIds[1]).size());
    IdGenerator docIdGenerator = new IdGenerator(minSize);
    String docName1 = cluster1.getDocumentName(docIdGenerator.getNextId());
    String docName2 = cluster2.getDocumentName(docIdGenerator.getNextId());
    // exchange the documents
    cluster1.removeDocument(docName1);
    cluster1.addDocument(docName2, collection.getDocument(docName2));
    cluster2.removeDocument(docName2);
    cluster2.addDocument(docName1, collection.getDocument(docName1));
    // rebuild the cluster list, replacing changed clusters
    List<Cluster> mutatedClusters = new ArrayList<Cluster>();
    int clusterId = 0;
    for (Cluster cluster : clusters) {
      if (clusterId == clusterIds[0]) {
        mutatedClusters.add(cluster1);
      } else if (clusterId == clusterIds[1]) {
        mutatedClusters.add(cluster2);
      } else {
        mutatedClusters.add(cluster);
      }
      clusterId++;
    }
    clusters.clear();
    clusters.addAll(mutatedClusters);
  }
}
