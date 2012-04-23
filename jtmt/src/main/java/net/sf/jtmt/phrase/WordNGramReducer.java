// $Id$
package net.sf.jtmt.phrase;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Writes out n-gram counts, filtering out the n-grams whose occurrence
 * counts falls below a predefined threshold.
 * @author Sujit Pal
 * @version $Revision$
 */
public class WordNGramReducer 
    extends Reducer<Text,LongWritable,PhraseCounterDbWritable,NullWritable> {

  private static int minOccurrences;

  @Override
  public void setup(Context context) {
    minOccurrences = 
      context.getConfiguration().getInt(PhraseExtractor.MIN_OCCURRENCES_KEY, 
      PhraseExtractor.MIN_OCCURRENCES_THRESHOLD);
  }

  @Override
  public void reduce(Text key, Iterable<LongWritable> values, Context context) 
  throws IOException, InterruptedException {
    long sum = 0L;
    for (Iterator<LongWritable> it = values.iterator(); it.hasNext();) {
      it.next();
      sum++;
    }
    if (sum > minOccurrences) {
      int gramSize = StringUtils.split(key.toString(), " ").length;
      PhraseCounterDbWritable okey = new PhraseCounterDbWritable();
      okey.setPhrase(key.toString());
      okey.setGramSize(gramSize);
      okey.setOccurs(sum);
      context.write(okey, NullWritable.get());
    }
  }
}
