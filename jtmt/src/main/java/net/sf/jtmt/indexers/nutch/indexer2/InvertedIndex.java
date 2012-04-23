// $Id$
package net.sf.jtmt.indexers.nutch.indexer2;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * A home grown inverted index to count occurrences of multi-word terms
 * within a body of text. The underlying data structure is a Map keyed
 * by individual words. The value mapped by the key is a Set of positions
 * corresponding to the positions of the word (0-based) in the text. For
 * multi-word terms, consecutive words are looked up and their positions
 * used to find the number of times a multi-word term appears in the text.
 * @author Sujit Pal
 * @version $Revision$
 */
public class InvertedIndex {

  private Map<String,Set<Integer>> termPositions;
  
  public InvertedIndex(String text) {
    termPositions = new HashMap<String,Set<Integer>>();
    BreakIterator wordBreakIterator = BreakIterator.getWordInstance(
      Locale.getDefault());
    wordBreakIterator.setText(text);
    int current = 0;
    int wordPosition = 0;
    for (;;) {
      int end = wordBreakIterator.next();
      if (end == BreakIterator.DONE) {
        break;
      }
      String nextWord = text.substring(current, end);
      current = end;
      if (StringUtils.isBlank(nextWord) || nextWord.matches("\\p{Punct}")) {
        continue;
      }
      String[] words = getMultiWords(nextWord);
      for (String word : words) {
        wordPosition = addPosition(word, wordPosition);
      }
    }
  }

  public boolean exists(String term) {
    return countOccurrencesOf(term) > 0;
  }

  public int countOccurrencesOf(String term) {
    String[] multiwords = getMultiWords(StringUtils.replace(term, " ", "-"));
    Set<Integer> newPrevPositions = new HashSet<Integer>();
    Set<Integer> prevPositions = new HashSet<Integer>();
    int termId = 0;
    for (String word : multiwords) {
      termId++;
      if (termPositions.containsKey(word)) {
        if (termId == 1) {
          prevPositions.addAll(termPositions.get(word));
          // if this is the only word, we've found it
          if (multiwords.length == 1) {
            newPrevPositions.addAll(prevPositions);
          } else {
            continue;
          }
        } else {
          Set<Integer> currentPositions = termPositions.get(word);
          for (Integer currentPosition : currentPositions) {
            // check for the occurrence of (currentPosition - 1) in
            // the prevPositions, if so, copy to the newPrevPositions
            if (prevPositions.contains(currentPosition - 1)) {
              newPrevPositions.add(currentPosition);
            }
          }
          prevPositions.clear();
          prevPositions.addAll(newPrevPositions);
          newPrevPositions.clear();
        }
      } else {
        // the current term is not found in our index, invalidating
        // the results so far, we should exit at this point
        prevPositions.clear();
        break;
      }
    }
    return prevPositions.size();
  }

  private String[] getMultiWords(String term) {
    term = StringUtils.lowerCase(term);
    if (term.indexOf('-') > -1) {
      return StringUtils.split(term, "-");
    } else {
      return new String[] {term};
    }
  }

  private int addPosition(String word, int position) {
    Set<Integer> positions = (termPositions.containsKey(word) ?
      termPositions.get(word) : new HashSet<Integer>());
    positions.add(position);
    termPositions.put(word, positions);
    position++;
    return position;
  }
}
