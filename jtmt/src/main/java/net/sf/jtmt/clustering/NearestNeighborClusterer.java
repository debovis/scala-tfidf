package net.sf.jtmt.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Algorithm from Text Mining Application Programming, ch 8.
 * 1) Compute the sum of the similarities of every document with its 2r neighbors.
 * 2) Loop through the list of documents in descending order of the sum of the
 *    similarities.
 *    2.1) Skip if the document is assigned to a cluster.
 *    2.2) Create a new cluster with the current document.
 *    2.3) Find all neighboring documents to the left and right of the current
 *         document that are not assigned to a cluster and have a similarity
 *         greater than a threshold.
 *    2.4) Add these documents to the new cluster.
 * 3) Return the list of documents.
 * 
 * @author Sujit Pal
 * @version $Revision$
 */
public class NearestNeighborClusterer {

  private final Log log = LogFactory.getLog(getClass());
  
  private int numNeighbors;
  private double similarityThreshold;
  
  public void setNumNeighbors(int numNeighbors) {
    this.numNeighbors = numNeighbors;
  }

  public void setSimilarityThreshold(double similarityThreshold) {
    this.similarityThreshold = similarityThreshold;
  }

  public List<Cluster> cluster(DocumentCollection collection) {
    // get neighbors for every document
    Map<String,Double> similarityMap = collection.getSimilarityMap();
//    for (String key : similarityMap.keySet()) {
//      log.debug("sim(" + key + ") => " + similarityMap.get(key));
//    }
    Map<String,List<String>> neighborMap = new HashMap<String,List<String>>();
    for (String documentName : collection.getDocumentNames()) {
      neighborMap.put(documentName, 
        collection.getNeighbors(documentName, similarityMap, numNeighbors));
    }
    // compute sum of similarities of every document with its numNeighbors
    Map<String,Double> fitnesses = getFitnesses(collection, similarityMap, neighborMap);
    List<String> sortedDocNames = new ArrayList<String>();
    // sort by sum of similarities descending
    sortedDocNames.addAll(collection.getDocumentNames());
    Collections.sort(sortedDocNames, Collections.reverseOrder(
      new ByValueComparator<String,Double>(fitnesses)));
//    for (String sortedDocName : sortedDocNames) {
//      log.debug(sortedDocName + " => " + fitnesses.get(sortedDocName));
//    }
    List<Cluster> clusters = new ArrayList<Cluster>();
    int clusterId = 0;
    // Loop through the list of documents in descending order of the sum of the
    // similarities.
    Map<String,String> documentClusterMap = new HashMap<String,String>();
    for (String docName : sortedDocNames) {
      // skip if document already assigned to cluster
      if (documentClusterMap.containsKey(docName)) {
        continue;
      }
      // create cluster with current document
      Cluster cluster = new Cluster("C" + clusterId);
      cluster.addDocument(docName, collection.getDocument(docName));
      documentClusterMap.put(docName, cluster.getId());
      // find all neighboring documents to the left and right of the current
      // document that are not assigned to a cluster, and have a similarity
      // greater than our threshold. Add these documents to the new cluster
      List<String> neighbors = neighborMap.get(docName);
      for (String neighbor : neighbors) {
        if (documentClusterMap.containsKey(neighbor)) {
          continue;
        }
        double similarity = similarityMap.get(
          StringUtils.join(new String[] {docName, neighbor}, ":"));
        if (similarity < similarityThreshold) {
          continue;
        }
        cluster.addDocument(neighbor, collection.getDocument(neighbor));
        documentClusterMap.put(neighbor, cluster.getId());
      }
      clusters.add(cluster);
      clusterId++;
    }
    return clusters;
  }

  private Map<String,Double> getFitnesses(DocumentCollection collection, 
      Map<String,Double> similarityMap, Map<String,List<String>> neighbors) {
    Map<String,Double> fitnesses = new HashMap<String,Double>();
    for (String docName : collection.getDocumentNames()) {
      double fitness = 0.0D;
      for (String neighborDoc : neighbors.get(docName)) {
        String key = StringUtils.join(new String[] {docName, neighborDoc}, ":");
        fitness += similarityMap.get(key);
      }
      fitnesses.put(docName, fitness);
    }
    return fitnesses;
  }
}
