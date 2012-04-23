// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Mapper to count number of lines in a file. For each line sent to the
 * mapper, it sends a constant key and a 1 to the reducer to add.
 * @author Sujit Pal
 * @version $Revision$
 */
public class RecordCountMapper 
    extends Mapper<LongWritable,Text,Text,LongWritable> {

  private static final Text COUNT_KEY = new Text("count");
  private static final LongWritable ONE = new LongWritable(1L);
  
  @Override
  public void map(LongWritable key, Text value, Context context) 
      throws IOException, InterruptedException {
    context.write(COUNT_KEY, ONE);
  }
}
