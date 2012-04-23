// $Id$
// $Source$
package net.sf.jtmt.postaggers.lucene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.jtmt.clustering.ByValueComparator;
import net.sf.jtmt.postaggers.Pos;

import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;

/**
 * Sets the POS tag for each token in the token stream. Uses Wordnet and some
 * grammar rules to make an initial determination. If Wordnet returns multiple
 * POS possibilities, we use the context surrounding the word (previous and
 * next characters) and a table of pre-calculated probabilities (from the
 * Brown corpus) to determine the most likely POS. If Wordnet returns a single 
 * POS, that is accepted. If Wordnet cannot determine the POS, then word 
 * suffix rules are used to guess the POS.
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public class PosTaggingFilter extends TokenFilter {

  private String prevTerm = null;
//  private String currentTerm = null;
  private String nextTerm = null;

  private Pos prevPos = null;

  private TokenStream suffixStream;
  private IDictionary wordnetDictionary;
  private Map<String,Pos> suffixPosMap;
  private Map<MultiKey<Pos>,Double> transitionProbabilities;
  
  private PosAttribute posAttr;
  private TermAttribute termAttr;
  private TermAttribute suffixTermAttr;
  
  protected PosTaggingFilter(TokenStream input, 
      TokenStream suffixStream,
      String wordnetDictionaryPath, String suffixRulesPath, 
      String posTransitionProbabilitiesPath) throws Exception {
    super(input);
    // declare the POS attribute for both streams
    this.posAttr = (PosAttribute) addAttribute(PosAttribute.class);
    this.termAttr = (TermAttribute) addAttribute(TermAttribute.class);
    this.suffixTermAttr = 
      (TermAttribute) suffixStream.addAttribute(TermAttribute.class);
    this.prevTerm = null;
    this.suffixStream = suffixStream;
    this.input.reset();
    this.suffixStream.reset();
    // advance the pointer to the next token for the suffix stream
    if (this.suffixStream.incrementToken()) {
      this.nextTerm = suffixTermAttr.term();
    }
    // create artifacts for doing the POS tagging
    this.wordnetDictionary = initWordnetDictionary(wordnetDictionaryPath);
    this.suffixPosMap = initSuffixPosMap(suffixRulesPath); 
    this.transitionProbabilities = initTransitionProbabilities(
      posTransitionProbabilitiesPath);
  }

  public final boolean incrementToken() throws IOException {
    String currentTerm = null;
    if (input.incrementToken()) {
      currentTerm = termAttr.term();
    } else {
      return false;
    }
    if (suffixStream.incrementToken()) {
      this.nextTerm = suffixTermAttr.term();
    } else {
      this.nextTerm = null;
    }
    termAttr.setTermBuffer(currentTerm);
    // find the POS of the current word from Wordnet
    List<Pos> currentPoss = getPosFromWordnet(currentTerm);
    if (currentPoss.size() == 1) {
      // unambiguous match, look no further
      posAttr.setPos(currentPoss.get(0));
    } else if (currentPoss.size() == 0) {
      // wordnet could not find a POS, use suffix rules to find
      if (prevTerm != null) {
        // this is not thr first word, check for capitalization for Noun
        if (currentTerm.charAt(0) == Character.UPPERCASE_LETTER) {
          posAttr.setPos(Pos.NOUN);
        }
      }
      if (posAttr.getPos() != null) {
        Pos pos = getPosFromSuffixRules(currentTerm);
        posAttr.setPos(pos);
      }
    } else {
      // wordnet reported multiple POS, find the best one
      Pos pos = getMostProbablePos(currentPoss, nextTerm, prevPos);
      posAttr.setPos(pos);
    }
    this.prevTerm = currentTerm;
    this.prevPos = posAttr.getPos();
    return true;
  }
  
  private IDictionary initWordnetDictionary(String wordnetDictionaryPath) 
      throws Exception {
    IDictionary wordnetDictionary = new Dictionary(
      new URL("file", null, wordnetDictionaryPath));
    wordnetDictionary.open();
    return wordnetDictionary;
  }

  private Map<String, Pos> initSuffixPosMap(String suffixRulesPath) 
      throws Exception {
    Map<String,Pos> suffixPosMap = new TreeMap<String, Pos>(
      new Comparator<String>() {
        public int compare(String s1, String s2) {
          int l1 = s1 == null ? 0 : s1.length();
          int l2 = s2 == null ? 0 : s2.length();
          if (l1 == l2) {
            return 0;
          } else {
            return (l2 > l1 ? 1 : -1);
          }
        }
    });
    BufferedReader reader = new BufferedReader(new FileReader(suffixRulesPath));
    String line = null;
    while ((line = reader.readLine()) != null) {
      if (StringUtils.isEmpty(line) || line.startsWith("#")) {
        continue;
      }
      String[] suffixPosPair = StringUtils.split(line, "\t");
      suffixPosMap.put(suffixPosPair[0], Pos.valueOf(suffixPosPair[1]));
    }
    reader.close();
    return suffixPosMap;
  }

  private Map<MultiKey<Pos>,Double> initTransitionProbabilities(
      String transitionProbabilitiesPath) throws Exception {
    Map<MultiKey<Pos>,Double> transitionProbabilities = 
      new HashMap<MultiKey<Pos>,Double>();
    BufferedReader reader = new BufferedReader(
      new FileReader(transitionProbabilitiesPath));
    String line = null;
    int row = 0;
    while ((line = reader.readLine()) != null) {
      if (StringUtils.isEmpty(line) || line.startsWith("#")) {
        continue;
      }
      String[] cols = StringUtils.split(line, "\t");
      for (int col = 0; col < cols.length; col++) {
        MultiKey<Pos> key = new MultiKey<Pos>(Pos.values()[row], Pos.values()[col]);
        transitionProbabilities.put(key, Double.valueOf(cols[col]));
      }
      row++;
    }
    reader.close();
    return transitionProbabilities;
  }

  private List<Pos> getPosFromWordnet(String currentTerm) {
    List<Pos> poss = new ArrayList<Pos>();
    for (Pos pos : Pos.values()) {
      try {
        IIndexWord indexWord = wordnetDictionary.getIndexWord(
          currentTerm, Pos.toWordnetPos(pos));
        if (indexWord != null) {
          poss.add(pos);
        }
      } catch (NullPointerException e) {
        // JWI throws NPE if it cannot find a word in dictionary
        continue;
      }
    }
    return poss;
  }

  private Pos getPosFromSuffixRules(String currentTerm) {
    for (String suffix : suffixPosMap.keySet()) {
      if (StringUtils.lowerCase(currentTerm).endsWith(suffix)) {
        return suffixPosMap.get(suffix);
      }
    }
    return Pos.OTHER;
  }

  private Pos getMostProbablePos(List<Pos> currentPoss, String nextTerm,
      Pos prevPos) {
    Map<Pos,Double> posProbs = new HashMap<Pos,Double>();
    // find the possible POS values for the previous and current term
    if (prevPos != null) {
      for (Pos currentPos : currentPoss) {
        MultiKey<Pos> key = new MultiKey<Pos>(prevPos, currentPos);
        double prob = transitionProbabilities.get(key);
        if (posProbs.containsKey(currentPos)) {
          posProbs.put(currentPos, posProbs.get(currentPos) + prob);
        } else {
          posProbs.put(currentPos, prob);
        }
      }
    }
    // find the possible POS values for the current and previous term
    if (nextTerm != null) {
      List<Pos> nextPoss = getPosFromWordnet(nextTerm);
      if (nextPoss.size() == 0) {
        nextPoss.add(Pos.OTHER);
      }
      for (Pos currentPos : currentPoss) {
        for (Pos nextPos : nextPoss) {
          MultiKey<Pos> key = new MultiKey<Pos>(currentPos, nextPos);
          double prob = transitionProbabilities.get(key);
          if (posProbs.containsKey(currentPos)) {
            posProbs.put(currentPos, posProbs.get(currentPos) + prob);
          } else {
            posProbs.put(currentPos, prob);
          }
        }
      }
    }
    // now find the current Pos with the maximum probability
    if (posProbs.size() == 0) {
      return Pos.OTHER;
    } else {
      ByValueComparator<Pos,Double> bvc = 
        new ByValueComparator<Pos,Double>(posProbs);
      List<Pos> posList = new ArrayList<Pos>();
      posList.addAll(posProbs.keySet());
      Collections.sort(posList, Collections.reverseOrder(bvc));
      return posList.get(0);
    }
  }
}
