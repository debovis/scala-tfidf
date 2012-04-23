package net.sf.jtmt.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.linear.RealMatrix;

/**
 * The algorithm is:
 * 1) The user chooses a maximum diameter for clusters.
 * 2) Build a candidate cluster for each point by including the closest point, 
 *    the next closest, and so on, until the diameter of the cluster surpasses 
 *    the threshold.
 * 3) Save the candidate cluster with the most points as the first true cluster, 
 *    and remove all points in the cluster from further consideration. 
 *    Must clarify what happens if more than 1 cluster has the maximum number 
 *    of points ?
 * 4) Recurse with the reduced set of points.
 * The distance between a point and a group of points is computed using complete linkage, 
 * i.e. as the maximum distance from the point to any member of the group 
 * (see the "Agglomerative hierarchical clustering" section about distance between clusters).
 * @author Sujit Pal
 * @version $Revision$
 */
public class QtClusterer {

  private final Log log = LogFactory.getLog(getClass());
  
  private double maxDiameter;
  private boolean randomizeDocuments;
  
  public void setMaxRadius(double maxRadius) {
    this.maxDiameter = maxRadius * 2.0D;
  }
  
  public void setRandomizeDocuments(boolean randomizeDocuments) {
    this.randomizeDocuments = randomizeDocuments;
  }
  
  public List<Cluster> cluster(DocumentCollection collection) {
    if (randomizeDocuments) {
      collection.shuffle();
    }
    List<Cluster> clusters = new ArrayList<Cluster>();
    Set<String> clusteredDocNames = new HashSet<String>();
    cluster_r(collection, clusters, clusteredDocNames, 0);
    return clusters;
  }

  private void cluster_r(DocumentCollection collection, List<Cluster> clusters,
      Set<String> clusteredDocNames, int level) {
//    if (level > 3) {
//      return;
//    }
    int numDocs = collection.size();
    int numClustered = clusteredDocNames.size();
    if (numDocs == numClustered) {
      return;
    }
    Cluster cluster = new Cluster("C" + level);
    for (int i = 0; i < numDocs; i++) {
      RealMatrix document = collection.getDocumentAt(i);
      String docName = collection.getDocumentNameAt(i);
      if (clusteredDocNames.contains(docName)) {
        continue;
      }
      log.debug("max dist=" + cluster.getCompleteLinkageDistance(document));
      if (cluster.getCompleteLinkageDistance(document) < maxDiameter) {
        cluster.addDocument(docName, document);
        clusteredDocNames.add(docName);
      }
    }
    if (cluster.size() == 0) {
      log.warn("No clusters added at level " + level + ", check diameter");
    }
    clusters.add(cluster);
    cluster_r(collection, clusters, clusteredDocNames, level + 1);
  }
}
