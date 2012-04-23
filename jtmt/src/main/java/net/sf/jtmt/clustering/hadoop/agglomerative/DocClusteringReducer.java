// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.SparseRealVector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Reduces the normalized TF data produced by the mapper. The mapper
 * produced 2 documents with the same key, which are candidates for 
 * the new cluster. The reducer will compute the new cluster coordinates
 * using the coordinates of its components. The other documents are
 * written out unchanged.
 * @author Sujit Pal
 * @version $Revision$
 */
public class DocClusteringReducer extends Reducer<Text,Text,Text,Text> {

  public void reduce(Text key, Iterable<Text> values, Context context) 
      throws IOException, InterruptedException {
    double numRecords = 0D;
    SparseRealVector doc = null;
    for (Text value : values) {
      String[] tfs = StringUtils.split(value.toString(), ",");
      if (doc == null) {
        doc = new OpenMapRealVector(tfs.length);
      }
      for (int i = 0; i < tfs.length; i++) {
        doc.setEntry(i, doc.getEntry(i) + Double.valueOf(tfs[i]));
      }
      numRecords++;
    }
    SparseRealVector cluster = new OpenMapRealVector(doc.mapDivide(numRecords));
    int numTerms = cluster.getDimension();
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < numTerms; i++) {
      if (i > 0) {
        buf.append(",");
      }
      buf.append(String.valueOf(cluster.getEntry(i)));
    }
    // replace the "+" in the key with "," since its done clustering
    String newKey = StringUtils.replace(key.toString(), "+", ",");
    context.write(new Text(newKey), new Text(buf.toString()));
  }
}
