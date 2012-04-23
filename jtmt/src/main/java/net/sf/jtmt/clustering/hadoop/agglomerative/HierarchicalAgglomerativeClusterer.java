// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Hadoop Job to do Hierarchical Agglomerative (bottom up) clustering.
 * We start with a normalized set of term frequencies for each document.
 * At each stage, we find the two documents with the highest similarity
 * and merge them into a cluster "document". We stop when the similarity
 * between two documents is lower than a predefined threshold or when
 * the document set is clustered into a predefined threshold of clusters.
 * @author Sujit Pal
 * @version $Revision$
 */
public class HierarchicalAgglomerativeClusterer {

  // configuration:
  /** Don't cluster if similarity is below this threshold */
  public static final Float SIMILARITY_THRESHOLD = 0.05F;
  /** Maximum number of clusters to be created */
  public static final Long MAX_CLUSTERS = 10L;
  
  // keys: used by Mappers/Reducers
  public static final String INPUT_DIR_KEY = "input.dir";
  public static final String INPUT_SIMILARITY_MAP_DIR_KEY = "input.sim.mapdir";
  public static final String SIMILARITY_THRESHOLD_KEY = "similarity.threshold";

  // reporting:
  public enum Counters {REMAINING_RECORDS};

  /**
   * The input to this job is a text file of document ids mapped 
   * to a stringified list of raw term occurrences for each 
   * qualifying term in the document set. This method will do 
   * a self-join on the input file, and pass it to the reducer,
   * which will calculate and output the inter-document cosine
   * similarities.
   * @param conf the global Configuration object.
   * @param indir the input directory for the raw TFs.
   * @param outdir the output directory for the normalized TFs.
   * @throws Exception if thrown.
   */
  private static void normalizeFrequencies(
      Configuration conf, Path indir, Path outdir) throws Exception {
    Job job = new Job(conf, "normalize-freqs");
    job.setJarByClass(HierarchicalAgglomerativeClusterer.class);
    FileInputFormat.addInputPath(job, indir);
    FileOutputFormat.setOutputPath(job, outdir);
    job.setMapperClass(TfNormalizerMapper.class);
    job.setReducerClass(TfNormalizerReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setNumReduceTasks(2);
    boolean jobStatus = job.waitForCompletion(true);
    if (! jobStatus) {
      throw new Exception(job.getJobName() + " failed");
    }
  }

  /**
   * Does a self-join on the normalized TF file (for the current run),
   * and passes each pair to the reducer, which computes the inter-document
   * cosine similarity, and writes out the interdocument cosine similarities
   * as the output.
   * @param conf the global Configuration object.
   * @param dataInput the directory containing the normalized TF file.
   * @param simInput the directory containing the similarity data from the
   *        previous run (null in case of the first run).
   * @param simOutput the output directory where the new similarity data
   *        is written. At each stage, only the similarity data that was
   *        not computed previously is computed.
   * @param iteration the run number (0-based).
   * @throws Exception if thrown.
   */
  private static void computeSimilarity(Configuration conf, Path dataInput,
      Path simInput, Path simOutput, int iteration) throws Exception {
    Job job = new Job(conf, "compute-similarity/" + iteration);
    job.getConfiguration().set(INPUT_DIR_KEY, dataInput.toString());
    if (iteration > 0) {
      job.getConfiguration().set(INPUT_SIMILARITY_MAP_DIR_KEY, simInput.toString());
    }
    FileInputFormat.addInputPath(job, dataInput);
    FileOutputFormat.setOutputPath(job, simOutput);
    job.setJarByClass(HierarchicalAgglomerativeClusterer.class);
    job.setMapperClass(SelfJoinMapper.class);
    job.setReducerClass(SimilarityCalculatorReducer.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(DoubleWritable.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setNumReduceTasks(2);
    boolean jobStatus = job.waitForCompletion(true);
    if (! jobStatus) {
      throw new Exception(job.getJobName() + " failed");
    }
  }

  /**
   * Runs through the similarity data, finds the pair with the highest
   * similarity. For documents that are part of that pair, the key is
   * set to the paired key (key1+key2) and sent to the reducer, where 
   * the coordinates of the merged cluster is computed by adding each
   * coordinate position and dividing by 2. Other records which are 
   * not part of the pair are passed through unchanged. The merging 
   * will happen only if the similarity is above the similarity threshold.
   * @param conf the global configuration object.
   * @param dataInput the input data file for this run, containing the
   *        normalized TFs.
   * @param dataOutput the output directory, which contains the new document
   *        set, after merging of the pair with the highest similarity.
   * @param simOutput the similarity output of the previous run, to 
   *        compute the most similar document/cluster pairs.
   * @param iteration the run number (0-based).
   * @throws Exception if thrown.
   */
  private static void clusterDocs(Configuration conf, Path dataInput,
      Path dataOutput, Path simOutput, int iteration) throws Exception {
    Job job = new Job(conf, "add-remove-docs/" + iteration);
    job.getConfiguration().setFloat(
      SIMILARITY_THRESHOLD_KEY, SIMILARITY_THRESHOLD);
    job.getConfiguration().set(
      INPUT_SIMILARITY_MAP_DIR_KEY, simOutput.toString());
    FileInputFormat.addInputPath(job, dataInput);
    FileOutputFormat.setOutputPath(job, dataOutput);
    job.setJarByClass(HierarchicalAgglomerativeClusterer.class);
    job.setMapperClass(DocClusteringMapper.class);
    job.setReducerClass(DocClusteringReducer.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setNumReduceTasks(2);
    boolean jobStatus = job.waitForCompletion(true);
    if (! jobStatus) {
      throw new Exception(job.getJobName() + " failed");
    }
  }

  private static long countRemainingRecords(Configuration conf, Path dataOutput,
      Path countOutput) throws Exception {
    Job job = new Job(conf, "count-remaining-records");
    FileInputFormat.addInputPath(job, dataOutput);
    FileOutputFormat.setOutputPath(job, countOutput);
    job.setJarByClass(HierarchicalAgglomerativeClusterer.class);
    job.setMapperClass(RecordCountMapper.class);
    job.setReducerClass(RecordCountReducer.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);
    boolean jobStatus = job.waitForCompletion(true);
    if (jobStatus) {
      FileSystem fs = FileSystem.get(conf);
      FileStatus[] fstatuses = fs.listStatus(countOutput);
      for (FileStatus fstatus : fstatuses) {
        Path path = fstatus.getPath();
        if (! path.getName().startsWith("part-r")) {
          continue;
        }
        FSDataInputStream fis = fs.open(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = reader.readLine();
        reader.close();
        fis.close();
        return Long.valueOf(StringUtils.split(line, "\t")[1]);
      }
    } else {
      throw new Exception(job.getJobName() + " failed");
    }
    return 0L;
  }
  
  /**
   * This is how we are called.
   * @param argv the input directory containing the raw TFs.
   * @throws Exception if thrown.
   */
  public static void main(String[] argv) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, argv).getRemainingArgs();
    if (otherArgs.length != 1) {
      System.err.println("Usage hac <indir>");
      System.exit(-1);
    }
    Path indir = new Path(otherArgs[0]);
    Path basedir = indir.getParent();
    
    // phase 1: normalize the term frequency across each document
    normalizeFrequencies(conf, indir, new Path(basedir, "temp0"));

    int iteration = 0;
    long previousRemainingRecords = 0L;
    for (;;) {
      // set up constants for current iteration
      Path dataInput = new Path(basedir, "temp" + iteration);
      Path dataOutput = new Path(basedir, "temp" + (iteration + 1));
      Path simInput = new Path(basedir, "temp_sim" + iteration);
      Path simOutput = new Path(basedir, "temp_sim" + (iteration + 1));
      Path countOutput = new Path(basedir, "temp_count" + (iteration + 1));

      // phase 2: do self-join on input file and compute similarity matrix
      // inputs:  self-join on files from temp_${iteration}
      // reference: similarity matrix file from temp_sim_${iteration},
      //            null if iteration=0.
      // outputs: similarity matrix file into temp_sim_${iteration+1}
      computeSimilarity(conf, dataInput, simInput, simOutput, iteration);

      // phase 3: find most similar pair, add pair, remove components 
      // input: files from temp_${iteration}
      // reference: files from temp_sim_${iteration} to create matrix
      // output: files into temp_${iteration+1}
      clusterDocs(conf, dataInput, dataOutput, simOutput, iteration);

      // check for termination criteria: either our pre-set maximum 
      // clusters for the document set has been reached, or clustering
      // has converged, so any cluster that will be created is "too large".
      // This is checked for in the DocClusteringReducer and it will 
      // not merge the rows in that case.
      long numRemainingRecords = 
        countRemainingRecords(conf, dataOutput, countOutput);
      if (numRemainingRecords <= MAX_CLUSTERS ||
          numRemainingRecords == previousRemainingRecords) {
        break;
      }
      previousRemainingRecords = numRemainingRecords;
      iteration++;
    }
    System.out.println("Output in " + new Path(basedir, "temp" + iteration));
  }
}
