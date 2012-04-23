// $Id$
package net.sf.jtmt.crawling.textextraction;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Converts the contents of a page to a List of Chunk objects, and returns
 * the Chunks which have text densities exceeding the threshold. As a side
 * effect, also marks the Chunks with the keep/discard marker for training
 * and testing the classifier in a downstream filter.
 * @author Sujit Pal
 * @version $Revision$
 */
public class DensityFilter {

  private static final int MIN_CHARS_IN_TEXT = 60;

  public List<Chunk> filter(String content) throws Exception {
    List<Chunk> usefulChunks = new ArrayList<Chunk>();
    List<Float> densities = new ArrayList<Float>();
    BufferedReader reader = new BufferedReader(new StringReader(content));
    String line = null;
    int lno = 0;
    boolean keepContent = false;
    while ((line = reader.readLine()) != null) {
      line = StringUtils.trim(line);
      if (StringUtils.isEmpty(line)) {
        continue;
      }
      // This block is completely specific to the current corpus, we are
      // just exploiting a quirk of the data to minimize manual work 
      // annotating the corpus for training the classifier.
      if (line.contains("<!-- content -->")) {
        keepContent = true;
      } else if (line.contains("<!-- /content -->")) {
        keepContent = false;
      }
      char[] chars = line.toCharArray();
      boolean inMarkup = false;
      int numCharsMarkup = 0;
      int numCharsText = 0;
      StringBuilder text = new StringBuilder();
      for (char c : chars) {
        switch (c) {
          case '<':
            inMarkup = true;
            break;
          case '>':
            inMarkup = false;
            break;
          default:
            if (inMarkup) {
              numCharsMarkup++;
            } else {
              text.append(c);
            }
            break;
        }
      }
      String chunktext = text.toString().trim();
      numCharsText = chunktext.length();
      // this block reduced the error rate from 19% to 0%. This may
      // overfit the data, and may need to be adjusted for other datasets
      if (numCharsText < MIN_CHARS_IN_TEXT) {
        continue;
      }
      // 1 is added to both the numerator and denominator to prevent
      // NaN results.
      float density = 
        ((float) numCharsText + 1) / 
        ((float) numCharsMarkup + (float) numCharsText + 1);
      densities.add(density);
      usefulChunks.add(new Chunk(text.toString(), density, keepContent));
      lno++;
    }
    DescriptiveStatistics stat = new DescriptiveStatistics();
    for (Float density : densities) {
      stat.addValue((double) density);
    }
    final double threshold = 0.5D - stat.getStandardDeviation();
    CollectionUtils.filter(usefulChunks, new Predicate<Chunk>() {
      public boolean evaluate(Chunk chunk) {
        return (chunk.density > threshold);
      }
    });
    return usefulChunks;
  }
}
