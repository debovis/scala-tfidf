// $Id$
package net.sf.jtmt.clustering.hadoop.agglomerative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.SparseRealVector;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Takes as input the self-join of the normalized TF data, and the 
 * similarity matrix data from the previous run (if it exists). Looks
 * up the similarity between a pair of keys (if it exists) and writes
 * it out to the output. If it does not exist, then it computes it.
 * The first time (when there is no previous run), the number of 
 * similarity computations is O(n**2). In later runs, it is O(n).
 * @author Sujit Pal
 * @version $Revision$
 */
public class SimilarityCalculatorReducer 
    extends Reducer<Text,Text,Text,DoubleWritable> {

  private String inputSimilarityMapDir;
  private FileSystem fs;
  private Map<String,Double> similarityMatrix = new HashMap<String,Double>();

  @Override
  public void setup(Context context) 
      throws IOException, InterruptedException {
    inputSimilarityMapDir = context.getConfiguration().get(
      HierarchicalAgglomerativeClusterer.INPUT_SIMILARITY_MAP_DIR_KEY);
    if (inputSimilarityMapDir == null) {
      System.err.println("Warning: no input similarity map dir, ignoring");
    } else {
      fs = FileSystem.get(context.getConfiguration());
      FileStatus[] fstatuses = fs.listStatus(new Path(inputSimilarityMapDir));
      for (FileStatus fstatus : fstatuses) {
        Path path = fstatus.getPath();
        if (! path.getName().startsWith("part-r")) {
          continue;
        }
        FSDataInputStream fis = fs.open(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        while ((line = reader.readLine()) != null) {
          String[] kvpairs = StringUtils.split(line, "\t");
          List<String> docIds = Arrays.asList(StringUtils.split(kvpairs[0], "/"));
          Collections.sort(docIds);
          String mapKey = StringUtils.join(docIds.iterator(), "/");
          if (! similarityMatrix.containsKey(mapKey)) {
            similarityMatrix.put(mapKey, Double.valueOf(kvpairs[1]));
          }
        }
        reader.close();
        fis.close();
      }
    }
  }

  @Override
  public void reduce(Text key, Iterable<Text> values, Context context)
  throws IOException, InterruptedException {
    Text value = values.iterator().next();
    List<String> docIds = Arrays.asList(StringUtils.split(key.toString(), "/"));
    Collections.sort(docIds);
    String mapKey = StringUtils.join(docIds.iterator(), "/");
    if (! similarityMatrix.containsKey(mapKey)) {
      // compute the cosine similarity between the two documents
      String[] termFreqs = StringUtils.split(value.toString(), "/");
      SparseRealVector doc1 = buildDocVector(termFreqs[0]);
      SparseRealVector doc2 = buildDocVector(termFreqs[1]);
      double cosim = doc1.dotProduct(doc2) / (doc1.getNorm() * doc2.getNorm());
      similarityMatrix.put(mapKey, cosim);
    }
    context.write(new Text(mapKey), 
      new DoubleWritable(similarityMatrix.get(mapKey)));
  }

  private SparseRealVector buildDocVector(String flist) {
    String[] freqs = StringUtils.split(flist, ",");
    SparseRealVector doc = new OpenMapRealVector(freqs.length);
    for (int i = 0; i < freqs.length; i++) {
      doc.setEntry(i, Double.valueOf(freqs[i]));
    }
    return doc;
  }
}
