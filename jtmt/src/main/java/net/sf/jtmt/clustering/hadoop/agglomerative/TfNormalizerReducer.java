// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * This is a simple identity reducer. It does no reduction, the record
 * is already created in the mapper, it simply picks the first (and only)
 * mapped record in the Iterable and writes it.
 * @author Sujit Pal
 * @version $Revision$
 */
public class TfNormalizerReducer extends Reducer<Text,Text,Text,Text> {

  @Override
  public void reduce(Text key, Iterable<Text> values, Context context) 
      throws IOException, InterruptedException {
    Text value = values.iterator().next();
    context.write(key, value);
  }
}
