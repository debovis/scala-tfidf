// $Id$
// $Source$
package net.sf.jtmt.spelling;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.ConsoleReader;

import org.apache.commons.lang.StringUtils;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;

public class Shell {

  private final int SPELL_CHECK_THRESHOLD = 250;

  /**
   * Wrapper over our SpellingCorrector class.
   * @throws Exception if one is thrown.
   */
  public Shell() throws Exception {
    ConsoleReader reader = new ConsoleReader(System.in, new PrintWriter(System.out));
    SpellingCorrector spellingCorrector = new SpellingCorrector();
    for (;;) {
      String line = reader.readLine("spell-check> ");
      if ("\\q".equals(line)) {
        break;
      }
      System.out.println(spellingCorrector.getSuggestion(line));
    }
  }

  // ============== this is really for exploratory testing purposes ==============
  
  /**
   * Wrapper over Jazzy native spell checking functionality.
   * @param b always true (to differentiate from the new ctor).
   * @throws Exception if one is thrown.
   */
  public Shell(boolean b) throws Exception {
    ConsoleReader reader = new ConsoleReader(System.in, new PrintWriter(System.out));
    SpellDictionary dictionary = new SpellDictionaryHashMap(
      new File("src/main/resources/english.0"));
    for (;;) {
      String line = reader.readLine("jazzy> ");
      if ("\\q".equals(line)) {
        break;
      }
      String suggestions = suggest(dictionary, line);
      System.out.println(suggestions);
    }
  }
  
  /**
   * Looks up single words from Jazzy's English dictionary.
   * @param dictionary the dictionary object to look up.
   * @param incorrect the suspected misspelt word.
   * @return if the incorrect word is correct according to Jazzy's dictionary,
   * then it is returned, else a set of possible corrections is returned. If
   * no possible corrections were found, this method returns (no suggestions).
   */
  @SuppressWarnings("unchecked")
  private String suggest(SpellDictionary dictionary, String incorrect) {
    if (dictionary.isCorrect(incorrect)) {
      // return the entered word
      return incorrect;
    }
    List<Word> words = dictionary.getSuggestions(incorrect, SPELL_CHECK_THRESHOLD);
    List<String> suggestions = new ArrayList<String>();
    final Map<String,Integer> costs = new HashMap<String,Integer>();
    for (Word word : words) {
      costs.put(word.getWord(), word.getCost());
      suggestions.add(word.getWord());
    }
    if (suggestions.size() == 0) {
      return "(no suggestions)";
    }
    Collections.sort(suggestions, new Comparator<String>() {
      public int compare(String s1, String s2) {
        Integer cost1 = costs.get(s1);
        Integer cost2 = costs.get(s2);
        return cost1.compareTo(cost2);
      }
    });
    return StringUtils.join(suggestions.iterator(), ", ");
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length == 1) {
      // word mode
      new Shell(true);
    } else {
      new Shell();
    }
  }
}
