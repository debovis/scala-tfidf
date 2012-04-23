// $Id$
package net.sf.jtmt.phrase;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Simple identity reducer, writes out qualifying phrase and its log likelihood
 * ratio to a text output.
 * @author Sujit Pal
 * @version $Revision$
 */
public class LikelyPhraseReducer
    extends Reducer<Text,DoubleWritable,Text,DoubleWritable> {

  @Override
  public void reduce(Text key, Iterable<DoubleWritable> values, Context context) 
  throws IOException, InterruptedException {
    for (DoubleWritable value : values) {
      // will only have 1 value in the iterable
      context.write(key, value);
    }
  }
}
