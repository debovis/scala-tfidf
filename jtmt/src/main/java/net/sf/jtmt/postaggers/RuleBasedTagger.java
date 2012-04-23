package net.sf.jtmt.postaggers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.jtmt.clustering.ByValueComparator;
import net.sf.jtmt.tokenizers.Token;
import net.sf.jtmt.tokenizers.TokenType;
import net.sf.jtmt.tokenizers.WordTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;

/**
 * Tagger that uses rules to do POS tagging and recognition.
 * @author Sujit Pal
 * @version $Revision$
 */
public class RuleBasedTagger {

  private final Log log = LogFactory.getLog(getClass());
  
  private class Context {
    public String prev;
    public List<Pos> prevPos;
    public String curr;
    public List<Pos> nextPos;
    public String next;
    public String toString() {
      return StringUtils.join(new String[] {prev,curr,next}, "/");
    }
  };
  
  private IDictionary wordnetDictionary;
  private Map<String,Pos> suffixPosMap;
  private double[][] transitionProbabilities;
  
  public void setWordnetDictLocation(String wordnetDictLocation) throws Exception {
    this.wordnetDictionary = new Dictionary(new URL("file", null, wordnetDictLocation));
    this.wordnetDictionary.open();
  }

  public void setSuffixMappingLocation(String suffixMappingLocation) throws Exception {
    String line;
    this.suffixPosMap = new TreeMap<String,Pos>(new Comparator<String>() {
      public int compare(String s1, String s2) {
        int l1 = s1.length();
        int l2 = s2.length();
        if (l1 == l2) {
          return s1.compareTo(s2);
        } else {
          return (l2 > l1 ? 1 : -1);
        }
      }
    });
    BufferedReader reader = new BufferedReader(new FileReader(suffixMappingLocation));
    while ((line = reader.readLine()) != null) {
      if (StringUtils.isEmpty(line) || line.startsWith("#")) {
        continue;
      }
      String[] suffixPosPair = StringUtils.split(line, "\t");
      suffixPosMap.put(suffixPosPair[0], Pos.valueOf(suffixPosPair[1]));
    }
    reader.close();
  }

  public void setTransitionProbabilityDatafile(String transitionProbabilityDatafile) 
      throws Exception {
    int numPos = Pos.values().length;
    transitionProbabilities = new double[numPos][numPos];
    BufferedReader reader = new BufferedReader(
      new FileReader(transitionProbabilityDatafile));
    int i = 0; // row
    String line;
    while ((line = reader.readLine()) != null) {
      if (StringUtils.isEmpty(line) || line.startsWith("#")) {
        continue;
      }
      String[] parts = StringUtils.split(line, "\t");
      for (int j = 0; j < parts.length; j++) {
        transitionProbabilities[i][j] = Double.valueOf(parts[j]);
      }
      i++;
    }
    reader.close();
  }

  public String tagSentence(String sentence) throws Exception {
    StringBuilder taggedSentenceBuilder = new StringBuilder();
    WordTokenizer wordTokenizer = new WordTokenizer();
    wordTokenizer.setText(sentence);
    List<Token> tokens = new ArrayList<Token>();
    Token token = null;
    while ((token = wordTokenizer.nextToken()) != null) {
      tokens.add(token);
    }
    // extract the words from the tokens
    List<String> words = new ArrayList<String>();
    for (Token tok : tokens) {
      if (tok.getType() == TokenType.WORD) {
        words.add(tok.getValue());
      }
    }
    // for each word, find the pos
    int position = 0;
    for (String word : words) {
      Pos partOfSpeech = getPartOfSpeech(words, word, position);
      taggedSentenceBuilder.append(word).
        append("/").
        append(partOfSpeech.name()).
        append(" ");
      position++;
    }
    return taggedSentenceBuilder.toString();
  }

  private Pos getPartOfSpeech(List<String> wordList, String word, int position) {
    List<Pos> partsOfSpeech = getPosFromWordnet(word);
    int numPos = partsOfSpeech.size();
    if (numPos == 0) {
      // unknown Pos, apply word rules to figure out Pos
      if (startsWithUppercase(word)) {
        return Pos.NOUN;
      }
      Pos pos = getPosBasedOnSuffixRules(word);
      if (pos != null) {
        return pos;
      } else {
        return Pos.OTHER;
      }
    } else if (numPos == 1) {
      // unique Pos, return
      return partsOfSpeech.get(0);
    } else {
      // ambiguous Pos, apply disambiguation rules
      Context context = getContext(wordList, position);
      Map<Pos,Double> posProbs = new HashMap<Pos,Double>();
      if (context.prev != null) {
        // backward neighbor rule
        accumulatePosProbabilities(posProbs, word, partsOfSpeech, 
          context.prev, context.prevPos, false);
      }
      if (context.next != null) {
        // forward neighbor rule
        accumulatePosProbabilities(posProbs, word, partsOfSpeech, 
          context.next, context.nextPos, true);
      }
      if (posProbs.size() == 0) {
        return Pos.OTHER;
      } else {
        ByValueComparator<Pos,Double> bvc = 
          new ByValueComparator<Pos,Double>(posProbs);
        List<Pos> poslist = new ArrayList<Pos>();
        poslist.addAll(posProbs.keySet());
        Collections.sort(poslist, Collections.reverseOrder(bvc));
        return poslist.get(0);
      }
    }
  }

  private List<Pos> getPosFromWordnet(String word) {
    List<Pos> poslist = new ArrayList<Pos>();
    for (Pos pos : Pos.values()) {
      try {
        IIndexWord indexWord = 
          wordnetDictionary.getIndexWord(word, Pos.toWordnetPos(pos));
        if (indexWord != null) {
          poslist.add(pos);
        }
      } catch (NullPointerException e) {
        // JWI throws this if it cannot find the word in its dictionary
        // so we just dont add anything to the poslist.
        continue;
      }
    }
    return poslist;
  }

  private boolean startsWithUppercase(String word) {
    return word.charAt(0) == Character.UPPERCASE_LETTER;
  }

  private Pos getPosBasedOnSuffixRules(String word) {
    for (String suffix : suffixPosMap.keySet()) {
      if (StringUtils.lowerCase(word).endsWith(suffix)) {
        return suffixPosMap.get(suffix);
      }
    }
    return null;
  }

  private Context getContext(List<String> words, int wordPosition) {
    Context context = new Context();
    if ((wordPosition - 1) >= 0) {
      context.prev = words.get(wordPosition - 1);
      context.prevPos = getPosFromWordnet(context.prev);
    }
    context.curr = words.get(wordPosition);
    if ((wordPosition + 1) < words.size()) {
      context.next = words.get(wordPosition + 1);
      context.nextPos = getPosFromWordnet(context.next);
    }
    return context;
  }
  
  private void accumulatePosProbabilities(Map<Pos,Double> posProbabilities,
      String word, List<Pos> wordPosList, String neighbor, 
      List<Pos> neighborPosList, boolean isForwardRule) {
    if (isForwardRule) {
      for (Pos wordPos : wordPosList) {
        for (Pos neighborPos : neighborPosList) {
          double prob = 
            transitionProbabilities[wordPos.ordinal()][neighborPos.ordinal()];
          updatePosProbabilities(posProbabilities, wordPos, prob);
        }
      }
    } else {
      for (Pos neighborPos : neighborPosList) {
        for (Pos wordPos : wordPosList) {
          double prob = transitionProbabilities[neighborPos.ordinal()][wordPos.ordinal()];
          updatePosProbabilities(posProbabilities, wordPos, prob);
        }
      }
    }
  }

  private void updatePosProbabilities(Map<Pos,Double> posProbabilities,
      Pos wordPos, double prob) {
    Double origProb = posProbabilities.get(wordPos);
    if (origProb == null) {
      posProbabilities.put(wordPos, prob);
    } else {
      posProbabilities.put(wordPos, prob + origProb);
    }
  }
}
