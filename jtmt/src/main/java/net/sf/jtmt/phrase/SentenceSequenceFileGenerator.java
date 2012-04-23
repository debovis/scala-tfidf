// $Id$
package net.sf.jtmt.phrase;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.sf.jtmt.tokenizers.SentenceTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 * Parses a Gutenberg text into sentences and writes them out
 * to a Hadoop Sequence File.
 * @author Sujit Pal
 * @version $Revision$
 */
public class SentenceSequenceFileGenerator {

  private final Log log = LogFactory.getLog(getClass());
  
  private static final String SENTENCE_TOKENIZATION_RULES_FILE = 
    "/home/sujit/src/jtmt/src/main/resources/sentence_break_rules.txt";
  
  public void generate(String inputDir, String outputDir) throws Exception {
    Configuration conf = new Configuration();
    conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
    FileSystem fs = FileSystem.get(conf);
    // set up the output
    Path outputPath = new Path(outputDir);
    SequenceFile.Writer writer = SequenceFile.createWriter(
      fs, conf, outputPath, LongWritable.class, Text.class);
    // iterate over the inputs
    Path inputPath = new Path(inputDir);
    FileStatus[] fileStatuses = fs.listStatus(inputPath);
    for (FileStatus fileStatus : fileStatuses) {
      Path inputFilePath = fileStatus.getPath();
      System.out.println("Processing file:[" + inputFilePath + "]");
      StringBuilder buf = new StringBuilder();
      FSDataInputStream istream = fs.open(inputFilePath);
      BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
      String line = null;
      while ((line = reader.readLine()) != null) {
        buf.append(line);
      }
      SentenceTokenizer st = new SentenceTokenizer(SENTENCE_TOKENIZATION_RULES_FILE);
      st.setText(buf.toString());
      String sentence = null;
      long sno = 0L;
      while ((sentence = st.nextSentence()) != null) {
        LongWritable key = new LongWritable(sno);
        Text value = new Text(sentence);
        writer.append(key, value);
        sno++;
      }
    }
    writer.close();
  }
}
