// $Id$
package net.sf.jtmt.phrase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Reads a sequence file and converts it to {ngram, count}.
 * @author Sujit Pal
 * @version $Revision$
 */
public class WordNGramMapper 
    extends Mapper<LongWritable,Text,Text,LongWritable> {

  private static final LongWritable ONE = new LongWritable(1L);

  private static int minGramSize;
  private static int maxGramSize;
  private static Set<String> stopset;

  @Override
  public void setup(Context context) throws IOException, InterruptedException {
    minGramSize = context.getConfiguration().getInt(
      PhraseExtractor.MIN_GRAM_SIZE_KEY, PhraseExtractor.MIN_GRAM_SIZE);
    maxGramSize = context.getConfiguration().getInt(
      PhraseExtractor.MAX_GRAM_SIZE_KEY, PhraseExtractor.MAX_GRAM_SIZE);
    String stopwordsFile = 
      context.getConfiguration().get(PhraseExtractor.STOPWORDS_FILENAME_KEY);
    stopset = new HashSet<String>();
    BufferedReader stopwordsReader = new BufferedReader(new FileReader(stopwordsFile));
    String line = null;
    while ((line = stopwordsReader.readLine()) != null) {
      if (line.startsWith("#")) {
        continue;
      }
      stopset.add(StringUtils.trim(line));
    }
  }

  @Override
  public void map(LongWritable key, Text value, Context context) 
  throws IOException, InterruptedException {
    String sentence = value.toString();
    WordNGramGenerator ngramGenerator = new WordNGramGenerator();
    List<String> grams = ngramGenerator.generate(sentence, minGramSize, maxGramSize);
    for (String gram : grams) {
      // if multi-word gram, and either starts or ends with a stop word,
      // then it should not be considered
      String[] wordsInGram = StringUtils.split(gram, " ");
      if (wordsInGram.length > 1) {
        if (stopset.contains(wordsInGram[0]) ||
            stopset.contains(wordsInGram[wordsInGram.length - 1])) {
          continue;
        }
      }
      context.write(new Text(gram), ONE);
    }
  }
}
