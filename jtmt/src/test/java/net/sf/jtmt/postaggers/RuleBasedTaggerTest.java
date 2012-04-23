package net.sf.jtmt.postaggers;

import org.junit.Test;

/**
 * Test for a rule-based POS tagger.
 * @author Sujit Pal
 * @version $Revision$
 */
public class RuleBasedTaggerTest {

  private String[] INPUT_TEXTS = {
    "The growing popularity of Linux in Asia, Europe, and the US is a major concern " +
    "for Microsoft.",
    "Jaguar will sell its new XJ-6 model in the US for a small fortune.",
    "The union is in a sad state.",
    "Please do not state the obvious.",
    "I am looking forward to the state of the union address.",
    "I have a bad cold today.",
    "The cold war was over long ago."
  };

  @Test
  public void testTagSentence() throws Exception {
    for (String sentence : INPUT_TEXTS) {
      RuleBasedTagger tagger = new RuleBasedTagger();
      tagger.setWordnetDictLocation("/opt/wordnet-3.0/dict");
      tagger.setSuffixMappingLocation("src/main/resources/pos_suffixes.txt");
      tagger.setTransitionProbabilityDatafile("src/main/resources/pos_trans_prob.txt");
      String taggedSentence = tagger.tagSentence(sentence);
      System.out.println("Original: " + sentence);
      System.out.println("Tagged:   " + taggedSentence);
    }
  }
}
