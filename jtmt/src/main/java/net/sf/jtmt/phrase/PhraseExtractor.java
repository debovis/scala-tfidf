// $Id$
package net.sf.jtmt.phrase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.jtmt.phrase.db.DBConfiguration;
import net.sf.jtmt.phrase.db.DBInputFormat;
import net.sf.jtmt.phrase.db.DBOutputFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Breaks up input text into sentences, then generates 3-5 grams of
 * the input text.
 * @author Sujit Pal
 * @version $Revision$
 */
public class PhraseExtractor {

  public static final int MIN_GRAM_SIZE = 1;
  public static final int MAX_GRAM_SIZE = 3;
  public static final int MIN_OCCURRENCES_THRESHOLD = 5;

  public static final String MIN_GRAM_SIZE_KEY = "phrase_extractor.min.gram.size";
  public static final String MAX_GRAM_SIZE_KEY = "phrase_extractor.max.gram.size";
  public static final String MIN_OCCURRENCES_KEY = "phrase_extractor.min.occurs";
  public static final String PHRASE_DETERMINANT_THRESHOLD_KEY = "phrase_extractor.min.llr";
  public static final String NUM_WORDS_IN_CORPUS_KEY = "phrase_extractor.num.words";
  public static final String NUM_2WORD_PHRASES_IN_CORPUS_KEY = "phrase_extractor.num.2wordphrases";
  public static final String STOPWORDS_FILENAME_KEY = "phrase_extractor.stopfilename";
  
  private static void recreateTable(Configuration conf) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      DBConfiguration dbconf = new DBConfiguration(conf);
      conn = dbconf.getConnection();
      ps = conn.prepareStatement("drop table my_phrase_counts");
      ps.executeUpdate();
      ps = conn.prepareStatement("create table my_phrase_counts (" +
        "phrase varchar(255) not null, " +
        "gram_size int not null," +
        "occurs long not null" +
        ") type=ISAM");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new Exception(e);
    } finally {
      if (ps != null) {
        try { ps.close(); } catch (SQLException e) {}
        try { conn.close(); } catch (SQLException e) {}
      }
    }
  }

  private static void writeNGrams(Configuration conf, Path inputPath,
      String stopwordFileLocation) throws Exception {
    DBConfiguration dbconf = new DBConfiguration(conf);
    dbconf.setOutputTableName("my_phrase_counts");
    dbconf.setOutputFieldNames("phrase", "gram_size", "occurs");
    Job job = new Job(conf, "write-ngrams");
    job.getConfiguration().setInt(MIN_GRAM_SIZE_KEY, MIN_GRAM_SIZE);
    job.getConfiguration().setInt(MAX_GRAM_SIZE_KEY, MAX_GRAM_SIZE);
    job.getConfiguration().setInt(MIN_OCCURRENCES_KEY, MIN_OCCURRENCES_THRESHOLD);
    job.getConfiguration().set(STOPWORDS_FILENAME_KEY, stopwordFileLocation); 
    job.setJarByClass(PhraseExtractor.class);
    FileInputFormat.addInputPath(job, inputPath);
    job.setInputFormatClass(SequenceFileInputFormat.class);
    job.setMapperClass(WordNGramMapper.class);
    job.setReducerClass(WordNGramReducer.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(LongWritable.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(PhraseCounterDbWritable.class);
    job.setOutputFormatClass(DBOutputFormat.class);
    job.setNumReduceTasks(2);
    boolean status = job.waitForCompletion(true);
    if (! status) {
      throw new Exception("Job " + job.getJobName() + " failed!");
    }
  }

  private static void createIndex(Configuration conf) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      DBConfiguration dbconf = new DBConfiguration(conf);
      conn = dbconf.getConnection();
      ps = conn.prepareStatement("create index my_phrase_counts_ix1 " +
        "on my_phrase_counts(gram_size)");
      ps.executeUpdate();
      ps = conn.prepareStatement("create index my_phrase_counts_ix2 " +
        "on my_phrase_counts(phrase)");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw(e);
    } finally {
      if (ps != null) {
        try { ps.close(); } catch (SQLException e) {}
        try { conn.close(); } catch (SQLException e) {}
      }
    }
  }

  private static long getNumGramsInCorpus(DBConfiguration dbconf, int gramsize) 
      throws IOException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = dbconf.getConnection();
      ps = conn.prepareStatement("select sum(occurs) " +
        "from my_phrase_counts where gram_size=?");
      ps.setInt(1, gramsize);
      rs = ps.executeQuery();
      rs.next();
      return rs.getLong(1);
    } catch (SQLException e) {
      throw new IOException(e);
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    } finally {
      if (rs != null) {
        try { rs.close(); } catch (SQLException e) {}
        try { ps.close(); } catch (SQLException e) {}
        try { conn.close(); } catch (SQLException e) {}
      }
    }
  }

  private static void findLikelyPhrases(Configuration conf, int gramSize,
      Path outputPath) throws Exception {
    DBConfiguration dbconf = new DBConfiguration(conf);
    dbconf.setInputTableName("my_phrase_counts");
    dbconf.setInputFieldNames("phrase", "gram_size", "occurs");
    dbconf.setInputConditions("gram_size=" + gramSize);
    dbconf.setInputClass(PhraseCounterDbWritable.class);
    Job job = new Job(conf, "find-likely-phrases");
    job.getConfiguration().setLong(
      NUM_WORDS_IN_CORPUS_KEY, getNumGramsInCorpus(dbconf, 1));
    job.getConfiguration().setLong(
      NUM_2WORD_PHRASES_IN_CORPUS_KEY, getNumGramsInCorpus(dbconf, 2));
    job.setJarByClass(PhraseExtractor.class);
    job.setInputFormatClass(DBInputFormat.class);
    job.setMapperClass(LikelyPhraseMapper.class);
    job.setReducerClass(LikelyPhraseReducer.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(DoubleWritable.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(DoubleWritable.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    FileOutputFormat.setOutputPath(job, outputPath);
    job.setNumReduceTasks(2);
    boolean status = job.waitForCompletion(true);
    if (! status) {
      throw new Exception("Job " + job.getJobName() + " failed!");
    }
  }

  public static void main(String[] argv) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, argv).getRemainingArgs();
    if (otherArgs.length != 3) {
      System.err.println("Usage: calc input_path stopword_path output_path");
      System.exit(-1);
    }
    // set up database properties
    DBConfiguration.configureDB(conf, 
      "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/tmdb", 
      "root", "orange");
//    recreateTable(conf);
//    writeNGrams(conf, new Path(otherArgs[0]), otherArgs[1]);
//    createIndex(conf);
    findLikelyPhrases(conf, 2, new Path(otherArgs[2]));
  }
}
