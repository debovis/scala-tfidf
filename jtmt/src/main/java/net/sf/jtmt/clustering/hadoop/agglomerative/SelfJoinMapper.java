// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sf.jtmt.clustering.hadoop.agglomerative.HierarchicalAgglomerativeClusterer.Counters;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Does a map-side replicated join to produce a self-join of the 
 * input data files of normalized TFs. I could probably have built
 * a mapper which uses a in-memory structure for one of the sides 
 * of the join given the size of the data, but I am trying to do a 
 * POC, so the "real" data is likely to be larger. So the join 
 * repeatedly opens and closes one copy of the file that is joined 
 * with the main input file. The output is the self-joined file.
 * @author Sujit Pal
 * @version $Revision$
 */
public class SelfJoinMapper extends Mapper<LongWritable,Text,Text,Text> {

  private String inputDir;
  private FileSystem fs;
  private int numRecords = 0;

  @Override
  public void setup(Context context) 
      throws IOException, InterruptedException {
    inputDir = context.getConfiguration().get(
      HierarchicalAgglomerativeClusterer.INPUT_DIR_KEY);
    if (inputDir == null) {
      System.err.println("Cant get value for key:" + 
        HierarchicalAgglomerativeClusterer.INPUT_DIR_KEY + ", abort");
      System.exit(-1);
    }
    fs = FileSystem.get(context.getConfiguration());
  }

  @Override
  public void map(LongWritable key, Text value, Context context) 
      throws IOException, InterruptedException {
    numRecords++;
    String[] lhsPair = StringUtils.split(value.toString(), "\t");
    FileStatus[] fstatuses = fs.listStatus(new Path(inputDir));
    for (FileStatus fstatus : fstatuses) {
      Path path = fstatus.getPath();
      if (! path.getName().startsWith("part-r")) {
        continue;
      }
      FSDataInputStream fis = fs.open(path);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] rhsPair = StringUtils.split(line, "\t");
        Text newKey = new Text(
            StringUtils.join(new String[] {lhsPair[0], rhsPair[0]}, "/"));
        Text newValue = new Text(
            StringUtils.join(new String[] {lhsPair[1], rhsPair[1]}, "/"));
        context.write(newKey, newValue);
      }
      reader.close();
      fis.close();
    }
    // report back to the framework how many records are remaining
    // for debugging purposes.
    context.getCounter(Counters.REMAINING_RECORDS).increment(1L);
  }

  @Override
  public void cleanup(Context context) 
      throws IOException, InterruptedException {
  }
}

