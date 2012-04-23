// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Sums up the number of line counts sent by the mapper and returns a 
 * single line of output.
 * @author Sujit Pal
 * @version $Revision$
 */
public class RecordCountReducer 
    extends Reducer<Text,LongWritable,Text,LongWritable> {

  @Override
  public void reduce(Text key, Iterable<LongWritable> values, Context context)
      throws IOException, InterruptedException {
    long sum = 0;
    for (LongWritable value : values) {
      sum++;
    }
    context.write(key, new LongWritable(sum));
  }
}
