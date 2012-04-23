// $Id$
package net.sf.jtmt.concurrent.hadoop.querycocanalyzer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Analyzes a set of access.log files looking for word-cooccurrences.
 * The output is a set of sorted word pairs and the occurrence count.
 * @author Sujit Pal
 * @version $Revision$
 */
public class QueryTermCoOccurrenceAnalyzer {

  private static class MapClass extends 
      Mapper<Text,Writable,Text,Writable> {

    @Override
    public void map(Text key, Writable value, Context context) 
        throws IOException, InterruptedException {
      String line = ((Text) value).toString();
      List<String> tokens = NcsaLogParser.parse(line);
      String url = StringUtils.split(tokens.get(4), " ")[1];
      if (url.startsWith("/search")) {
        Map<String,String> parameters = getUrlParameters(url);
        String searchTerms = parameters.get("q1");
        String[] terms = StringUtils.split(searchTerms, " ");
        if (terms.length > 1) {
          // need to have at least 2 words to generate pair-wise combinations
          CombinationGenerator combinationGenerator = 
            new CombinationGenerator(terms.length, 2);
          Set<Pair> combinations = new HashSet<Pair>();
          while (combinationGenerator.hasMore()) {
            int[] indices = combinationGenerator.getNext();
            combinations.add(new Pair(terms[indices[0]], terms[indices[1]]));
          }
          for (Pair combination : combinations) {
            context.write(new Text(combination.toString()), new LongWritable(1));
          }
        }
      }
    }
    
    private static Map<String,String> getUrlParameters(String url) throws IOException {
      Map<String,String> parameters = new HashMap<String,String>();
      int pos = url.indexOf('?');
      if (pos == -1) {
        return parameters;
      }
      String queryString = url.substring(pos + 1);
      String[] nvps = queryString.split("&");
      for (String nvp : nvps) {
        String[] pair = nvp.split("=");
        if (pair.length != 2) {
          continue;
        }
        String key = pair[0];
        String value = pair[1];
        // URL decode the value, replacing + and %20 etc chars with their
        // non-encoded equivalents.
        try {
          value = URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new IOException("Unsupported encoding", e);
        }
        // replace all punctuation by space
        value = value.replaceAll("\\p{Punct}", " ");
        // lowercase it
        value = StringUtils.lowerCase(value);
        parameters.put(key, value); 
      }
      return parameters;
    }
  }
  
  private static class ReduceClass extends 
      Reducer<Text,Writable,Text,Writable> {

    @Override
    public void reduce(Text key, Iterable<Writable> values,
        Context context) throws IOException, InterruptedException {
      long occurs = 0;
      for (Iterator<Writable> it = values.iterator(); it.hasNext();) {
        occurs += ((LongWritable) it.next()).get();
      }
      context.write(key, new LongWritable(occurs));
    }
  }

  private static class PartitionerClass  
    extends Partitioner<Text,LongWritable> {

    @Override
    public int getPartition(Text key, LongWritable value, 
        int numReduceTasks) {
      if (numReduceTasks > 1) {
        String k = ((Text) key).toString();
        return (k.contains(",") ? 1 : 0);
      }
      return 0;
    }
  }
  
  static class Pair {
    public String first;
    public String second;

    public Pair(String first, String second) {
      String[] pair = new String[] {first, second};
      Arrays.sort(pair);
      this.first = pair[0];
      this.second = pair[1];
    }
    
    @Override
    public int hashCode() {
      return toString().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Pair)) {
        return false;
      }
      Pair that = (Pair) obj;
      return (this.first.equals(that.first) &&
        this.second.equals(that.second));
    }
    
    @Override
    public String toString() {
      return StringUtils.join(new String[] {first, second}, ",");
    }
  }

  public static void main(String[] argv) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, argv).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage:calc input_path output_path");
      System.exit(-1);
    }
    Job job = new Job(conf, "analyze-co-occurrence");
    
    FileInputFormat.addInputPath(job, new Path(argv[0]));
    FileOutputFormat.setOutputPath(job, new Path(argv[1]));
    
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);
    
    job.setMapperClass(MapClass.class);
    job.setCombinerClass(ReduceClass.class);
    job.setReducerClass(ReduceClass.class);
//    job.setPartitionerClass(PartitionerClass.class);
    
    job.setNumReduceTasks(2);
    
    boolean status = job.waitForCompletion(true);
    if (! status) {
      throw new Exception("Job " + job.getJobName() + " failed!");
    }
  }
}
