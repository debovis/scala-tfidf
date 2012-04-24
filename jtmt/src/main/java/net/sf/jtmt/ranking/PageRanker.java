package net.sf.jtmt.ranking;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

/**
 * Implementation of the published version of Google's PageRank algorithm.
 * @author Sujit Pal
 * @version $Revision$
 */
public class PageRanker {

  private Map<String,Boolean> linkMap;
  private double d;
  private double threshold;
  private List<String> docIds;
  private int numDocs;
  
  public void setLinkMap(Map<String,Boolean> linkMap) {
    this.linkMap = linkMap;
  }
  
  public void setDocIds(List<String> docIds) {
    this.docIds = docIds;
    this.numDocs = docIds.size();
  }
  
  public void setDampingFactor(double dampingFactor) {
    this.d = dampingFactor;
  }
  
  public void setConvergenceThreshold(double threshold) {
    this.threshold = threshold;
  }
  
  public RealMatrix rank() throws Exception {
    // create and initialize the probability matrix, start with all
    // equal probability p(i,j) of 0 or 1/n depending on if there is 
    // a link or not from page i to j.
    RealMatrix a = new OpenMapRealMatrix(numDocs, numDocs);
    for (int i = 0; i < numDocs; i++) {
      for (int j = 0; j < numDocs; j++) {
        String key = StringUtils.join(new String[] {
          docIds.get(i), docIds.get(j)
        }, ",");
        if (linkMap.containsKey(key)) {
          a.setEntry(i, j, 1.0D / numDocs);
        }
      }
    }
    // create and initialize the constant matrix
    RealMatrix c = new OpenMapRealMatrix(numDocs, 1);
    for (int i = 0; i < numDocs; i++) {
      c.setEntry(i, 0, ((1.0D - d) / numDocs));
    }
    // create and initialize the rank matrix
    RealMatrix r0 = new OpenMapRealMatrix(numDocs, 1);
    for (int i = 0; i < numDocs; i++) {
      r0.setEntry(i, 0, (1.0D / numDocs));
    }
    // solve for the pagerank matrix r
    RealMatrix r;
    int i = 0;
    for(;;) {
      r = c.add(a.scalarMultiply(d).multiply(r0));
      // check for convergence
      if (r.subtract(r0).getNorm() < threshold) {
        break;
      }
      r0 = r.copy();
      i++;
    }
    return r;
  }
}
