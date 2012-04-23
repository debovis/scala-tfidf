package net.sf.jtmt.postaggers;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;

/**
 * Test for the HMM based tagger.
 * @author Sujit Pal
 * @version $Revision$
 */
public class HmmTaggerTest {

//  @Test
  public void testBuildFromBrownAndWrite() throws Exception {
    HmmTagger hmmTagger = new HmmTagger();
    hmmTagger.setDataDir("/opt/brown-2.0");
    hmmTagger.setDictionaryLocation("src/test/resources/brown_dict.txt");
    hmmTagger.setHmmFileName("src/test/resources/hmm_tagger.dat");
    Hmm<ObservationInteger> hmm = hmmTagger.buildFromBrownCorpus();
    hmmTagger.saveToFile(hmm);
  }
  
//  @Test
  public void testTagging() throws Exception {
    HmmTagger hmmTagger = new HmmTagger();
    hmmTagger.setDataDir("/opt/brown-2.0");
    hmmTagger.setDictionaryLocation("src/test/resources/brown_dict.txt");
    hmmTagger.setHmmFileName("src/test/resources/hmm_tagger.dat");
    Hmm<ObservationInteger> hmm = hmmTagger.buildFromHmmFile();
    // POS tagging
    String[] testSentences = new String[] {
      "The dog ran after the cat .",
      "Failure dogs his path .",
      "The cold steel cuts through the flesh .",
      "He had a bad cold .",
      "He will catch the ball .",
      "Salmon is the catch of the day ."
    };
    String[] testWords = new String[] {
      "dog",
      "dogs",
      "cold",
      "cold",
      "catch",
      "catch"
    };
    for (int i = 0; i < testSentences.length; i++) {
      System.out.println("Original sentence: " + testSentences[i]);
      System.out.println("Tagged sentence: " + 
        hmmTagger.tagSentence(testSentences[i], hmm));
      Pos wordPos = hmmTagger.getMostLikelyPos(testWords[i], testSentences[i], hmm); 
      System.out.println("Pos(" + testWords[i] + ")=" + wordPos);
    }
    // observation probability for various sequences
    System.out.println("P(I am worried)=" + 
      hmmTagger.getObservationProbability("I am worried", hmm));
    System.out.println("P(Worried I am)=" +  
      hmmTagger.getObservationProbability("Worried I am", hmm));
  }
  
  @Test
  public void testLearning() throws Exception {
    // list of yoda quotes copied from http://www.quotemountain.com/quotes/yoda_quotes/
    List<String> sentences = Arrays.asList(new String[] {
      "Powerful you have become .",
      "The dark side I sense in you .",
      "Grave danger you are in .",
      "Impatient you are .",
      "Try not .",
      "Do or do not there is no try .",
      "Consume you the dark path will .",
      "Always in motion is the future .",
      "Around the survivors a perimeter create .",
      "Size matters not .",
      "Blind we are if creation of this army we could not see .",
      "Help you I can yes .",
      "Strong am I with the force .",
      "Agree with you the council does .",
      "Your apprentice he will be .",
      "Worried I am .",
      "Always two there are .",
      "When 900 years you reach look as good you will not ."
    });
    HmmTagger hmmTagger = new HmmTagger();
    hmmTagger.setDataDir("/opt/brown-2.0");
    hmmTagger.setDictionaryLocation("src/test/resources/brown_dict.txt");
    Hmm<ObservationInteger> learnedHmm = hmmTagger.teach(sentences);
    System.out.println("P(Worried I am)=" +  
      hmmTagger.getObservationProbability("Worried I am", learnedHmm));
    System.out.println("P(I am worried)=" + 
      hmmTagger.getObservationProbability("I am worried", learnedHmm));
  }
}
