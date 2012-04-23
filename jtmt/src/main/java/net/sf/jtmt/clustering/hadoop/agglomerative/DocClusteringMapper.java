// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Uses the similarity map and the normalized TF data from the previous
 * run, and computes the pair with the maximum similarity. For the docs
 * that are part of this pair, it will change the key to be the combined
 * key for the cluster to be formed and pass it to the reducer. This will
 * only happen for 2 documents in each iteration. The other docs are passed
 * through unchanged. 
 * @author Sujit Pal
 * @version $Revision$
 */
public class DocClusteringMapper extends Mapper<LongWritable,Text,Text,Text> {

  private double similarityThreshold = 0.0D;
  private double maxSim = 0.0D;
  private String[] maxSimKeys = null;
  
  public void setup(Context context) throws IOException, InterruptedException {
    similarityThreshold = context.getConfiguration().getFloat(
      HierarchicalAgglomerativeClusterer.SIMILARITY_THRESHOLD_KEY, 0.0F); 
    String simDir = context.getConfiguration().get(
      HierarchicalAgglomerativeClusterer.INPUT_SIMILARITY_MAP_DIR_KEY);
    if (simDir == null) {
      System.err.println("Cant get value for key: " + 
        HierarchicalAgglomerativeClusterer.INPUT_SIMILARITY_MAP_DIR_KEY + 
        ", abort");
      System.exit(-1);
    }
    FileSystem fs = FileSystem.get(context.getConfiguration());
    FileStatus[] fstatuses = fs.listStatus(new Path(simDir));
    for (FileStatus fstatus : fstatuses) {
      Path path = fstatus.getPath();
      if (! path.getName().startsWith("part-r")) {
        continue;
      }
      FSDataInputStream fis = fs.open(path);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] kvpair = StringUtils.split(line, "\t");
        String[] docIdPair = StringUtils.split(kvpair[0], "/");
        if (docIdPair[0].equals(docIdPair[1])) {
          // same doc, expected max, so skip
          continue;
        }
        if (Double.valueOf(kvpair[1]) > maxSim) {
          maxSim = Double.valueOf(kvpair[1]);
          maxSimKeys = docIdPair;
        }
      }
      reader.close();
      fis.close();
    }
  }
  
  @Override
  public void map(LongWritable key, Text value, Context context) 
      throws IOException, InterruptedException {
    String[] kvpair = StringUtils.split(value.toString(), "\t");
    if (maxSim > similarityThreshold &&
       (kvpair[0].equals(maxSimKeys[0]) || 
        kvpair[0].equals(maxSimKeys[1]))) {
      // if either of the keys in maxSimKeys match the key in the 
      // record, then replace the key with the combo-key (this key
      // represents the "cluster")
      String newKey = StringUtils.join(maxSimKeys, "+");
      context.write(new Text(newKey), new Text(kvpair[1]));
    } else {
      // pass record through unchanged
      context.write(new Text(kvpair[0]), new Text(kvpair[1]));
    }
  }
}
