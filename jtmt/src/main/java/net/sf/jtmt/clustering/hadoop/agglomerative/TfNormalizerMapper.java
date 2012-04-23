// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.SparseRealVector;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Normalizes each document vector by dividing each element by the
 * sum of all the elements. Passes to the Reducer.
 * @author Sujit Pal
 * @version $Revision$
 */
public class TfNormalizerMapper extends Mapper<LongWritable,Text,Text,Text> {

  @Override
  public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {
    String[] kvp = StringUtils.split(value.toString(), "\t");
    String[] frequencies = StringUtils.split(kvp[1], ",");
    SparseRealVector tf = new OpenMapRealVector(frequencies.length);
    for (int i = 0; i < frequencies.length; i++) {
      tf.setEntry(i, Double.valueOf(frequencies[i]));
    }
    double sum = tf.getL1Norm();
    SparseRealVector normalizedTfs = 
      new OpenMapRealVector(tf.mapDivide(sum));
    StringBuilder nbuf = new StringBuilder();
    int len = normalizedTfs.getDimension();
    for (int i = 0; i < len; i++) {
      if (i > 0) {
        nbuf.append(",");
      }
      nbuf.append(String.valueOf(normalizedTfs.getEntry(i)));
    }
    context.write(new Text(kvp[0]), new Text(nbuf.toString()));
  }
}
