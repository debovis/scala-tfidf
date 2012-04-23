package net.sf.jtmt.postaggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardCalculator;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.ViterbiCalculator;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;

/**
 * HMM based POS Tagger.
 * @author Sujit Pal
 * @version $Revision$
 */
public class HmmTagger {


  private static final DecimalFormat OBS_FORMAT = new DecimalFormat("##.#####");
  
  private final Log log = LogFactory.getLog(getClass());

  private String dataDir;
  private String dictionaryLocation;
  private String hmmFileName;
  
  private Map<String,Integer> words = new HashMap<String,Integer>();
  
  public void setDataDir(String brownDataDir) {
    this.dataDir = brownDataDir;
  }

  public void setDictionaryLocation(String dictionaryLocation) {
    this.dictionaryLocation = dictionaryLocation;
  }

  public void setHmmFileName(String hmmFileName) {
    this.hmmFileName = hmmFileName;
  }

  /**
   * Builds up an HMM where states are parts of speech given by the Pos
   * Enum, and the observations are actual words in the tagged Brown
   * corpus. Each integer observation corresponds to the position of 
   * a word found in the Brown corpus.
   * @return an HMM.
   * @throws Exception if one is thrown.
   */
  public Hmm<ObservationInteger> buildFromBrownCorpus() throws Exception {
    BrownCorpusReader brownReader = new BrownCorpusReader();
    brownReader.setDataFilesLocation(dataDir);
    brownReader.setWordDictionaryLocation(dictionaryLocation);
    brownReader.read();
    int nbStates = Pos.values().length;
    OpdfIntegerFactory factory = new OpdfIntegerFactory(nbStates);
    Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(nbStates, factory); 
    double[] pi = brownReader.getPi();
    for (int i = 0; i < nbStates; i++) {
      hmm.setPi(i, pi[i]);
    }
    double[][] a = brownReader.getA();
    for (int i = 0; i < nbStates; i++) {
      for (int j = 0; j < nbStates; j++) {
        hmm.setAij(i, j, a[i][j]);
      }
    }
    double[][] b = brownReader.getB();
    for (int i = 0; i < nbStates; i++) {
      hmm.setOpdf(i, new OpdfInteger(b[i]));
    }
    int seq = 0;
    for (String word : brownReader.getWords()) {
      words.put(word, seq);
      seq++;
    }
    return hmm;
  }
  
  /**
   * Builds an HMM from a formatted file describing the HMM. The format is
   * specified by the Jahmm project, and it has utility methods to read and
   * write HMMs from and to text files. We use this because the builder that
   * builds an HMM from the Brown corpus is computationally intensive and
   * this strategy provides us a way to partition the process.
   * @return a HMM
   * @throws Exception if one is thrown.
   */
  public Hmm<ObservationInteger> buildFromHmmFile() throws Exception {
    File hmmFile = new File(hmmFileName);
    if (! hmmFile.exists()) {
      throw new Exception("HMM File: " + hmmFile.getName() + " does not exist");
    }
    FileReader fileReader = new FileReader(hmmFile);
    OpdfReader<OpdfInteger> opdfReader = new OpdfIntegerReader();
    Hmm<ObservationInteger> hmm = HmmReader.read(fileReader, opdfReader);
    return hmm;
  }
  
  /**
   * Utility method to save an HMM into a formatted text file describing the
   * HMM. The format is specified by the Jahmm project, which also provides
   * utility methods to write a HMM to the text file.
   * @param hmm the HMM to write.
   * @throws Exception if one is thrown.
   */
  public void saveToFile(Hmm<ObservationInteger> hmm) throws Exception {
    FileWriter fileWriter = new FileWriter(hmmFileName);
    // we create our own impl of the OpdfIntegerWriter because we want
    // to control the formatting of the opdf probabilities. With the 
    // default OpdfIntegerWriter, small probabilities get written in 
    // the exponential format, ie 1.234..E-4, which the HmmReader does
    // not recognize.
    OpdfWriter<OpdfInteger> opdfWriter = new OpdfWriter<OpdfInteger>() {
      @Override
      public void write(Writer writer, OpdfInteger opdf) throws IOException {
        String s = "IntegerOPDF [";
        for (int i = 0; i < opdf.nbEntries(); i++)
          s += OBS_FORMAT.format(opdf.probability(new ObservationInteger(i))) + " ";
        writer.write(s + "]\n");
      }
    };
    HmmWriter.write(fileWriter, opdfWriter, hmm);
    fileWriter.flush();
    fileWriter.close();
  }

  /**
   * Given the HMM, returns the probability of observing the sequence of words
   * specified in the sentence. Uses the Forward-Backward algorithm to compute
   * the probability.
   * @param sentence the sentence to check.
   * @param hmm a reference to a prebuilt HMM.
   * @return the probability of observing this sequence.
   * @throws Exception if one is thrown.
   */
  public double getObservationProbability(String sentence, 
      Hmm<ObservationInteger> hmm) throws Exception {
    String[] tokens = tokenizeSentence(sentence);
    List<ObservationInteger> observations = getObservations(tokens);
    ForwardBackwardCalculator fbc = new ForwardBackwardCalculator(observations, hmm);
    return fbc.probability();
  }

  /**
   * Given an HMM and an untagged sentence, tags each word with the part of
   * speech it is most likely to belong in. Uses the Viterbi algorithm.
   * @param sentence the sentence to tag.
   * @param hmm the HMM to use.
   * @return a tagged sentence.
   * @throws Exception if one is thrown.
   */
  public String tagSentence(String sentence, Hmm<ObservationInteger> hmm) 
      throws Exception {
    String[] tokens = tokenizeSentence(sentence);
    List<ObservationInteger> observations = getObservations(tokens);
    ViterbiCalculator vc = new ViterbiCalculator(observations, hmm);
    int[] ids = vc.stateSequence();
    StringBuilder tagBuilder = new StringBuilder();
    for (int i = 0; i < ids.length; i++) {
      tagBuilder.append(tokens[i]).
        append("/").
        append((Pos.values()[ids[i]]).name()).
        append(" ");
    }
    return tagBuilder.toString();
  }
  
  /**
   * Given an HMM, a sentence and a word within the sentence which needs to 
   * be disambiguated, returns the most likely Pos for the specified word.
   * @param word the word to find the Pos for.
   * @param sentence the sentence.
   * @param hmm the HMM.
   * @return the most likely POS.
   * @throws Exception if one is thrown.
   */
  public Pos getMostLikelyPos(String word, String sentence, 
      Hmm<ObservationInteger> hmm) throws Exception {
    if (words == null || words.size() == 0) {
      loadWordsFromDictionary();
    }
    String[] tokens = tokenizeSentence(sentence);
    List<ObservationInteger> observations = getObservations(tokens);
    int wordPos = -1;
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].equalsIgnoreCase(word)) {
        wordPos = i;
        break;
      }
    }
    if (wordPos == -1) {
      throw new IllegalArgumentException("Word [" + word + 
        "] does not exist in sentence [" + sentence + "]");
    }
    ViterbiCalculator vc = new ViterbiCalculator(observations, hmm);
    int[] ids = vc.stateSequence();
    return Pos.values()[ids[wordPos]];
  }

  /**
   * Given an existing HMM, this method will send in a List of sentences from
   * a possibly different untagged source, to refine the HMM.
   * @param sentences the List of sentences to teach.
   * @return a HMM that has been taught using the observation sequences.
   * @throws Exception if one is thrown.
   */
  public Hmm<ObservationInteger> teach(List<String> sentences) throws Exception {
    if (words == null || words.size() == 0) {
      loadWordsFromDictionary();
    }
    OpdfIntegerFactory factory = new OpdfIntegerFactory(words.size());
    List<List<ObservationInteger>> sequences = 
      new ArrayList<List<ObservationInteger>>();
    for (String sentence : sentences) {
      List<ObservationInteger> sequence = getObservations(tokenizeSentence(sentence));
      sequences.add(sequence);
    }
    KMeansLearner<ObservationInteger> kml = 
      new KMeansLearner<ObservationInteger>(Pos.values().length, factory, sequences);
    Hmm<ObservationInteger> hmm = kml.iterate();
    // refine it with Baum-Welch Learner
    BaumWelchLearner bwl = new BaumWelchLearner();
    Hmm<ObservationInteger> refinedHmm = bwl.iterate(hmm, sequences);
    return refinedHmm;
  }
  
  /**
   * Convenience method to compute the distance between two HMMs. This can be
   * used to stop the teaching process once more teaching is not producing any
   * appreciable improvement in the HMM, ie, the HMM converges. The caller will
   * need to match the result of this method with a number based on experience.
   * @param hmm1 the original HMM.
   * @param hmm2 the HMM that was most recently taught.
   * @return the difference measure between the two HMMs.
   * @throws Exception if one is thrown.
   */
  public double difference(Hmm<ObservationInteger> hmm1, Hmm<ObservationInteger> hmm2) 
      throws Exception {
    KullbackLeiblerDistanceCalculator kdc = new KullbackLeiblerDistanceCalculator();
    return kdc.distance(hmm1, hmm2);
  }
  
  private String[] tokenizeSentence(String sentence) {
    String[] tokens = StringUtils.split(
      StringUtils.lowerCase(StringUtils.trim(sentence)), " ");
    return tokens;
  }
  
  private List<ObservationInteger> getObservations(String[] tokens) throws Exception {
    if (words == null || words.size() == 0) {
      loadWordsFromDictionary();
    }
    List<ObservationInteger> observations = new ArrayList<ObservationInteger>();
    for (String token : tokens) {
      observations.add(new ObservationInteger(words.get(token)));
    }
    return observations;
  }
  
  private void loadWordsFromDictionary() throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(dictionaryLocation));
    String word;
    int seq = 0;
    while ((word = reader.readLine()) != null) {
      words.put(word, seq);
      seq++;
    }
    reader.close();
  }
}
