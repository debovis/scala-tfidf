// $Id$
package net.sf.jtmt.crawling.textextraction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.CollectionUtils;

import com.aliasi.classify.Classifier;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;

/**
 * A classifier to classify incoming text chunks into keep and discard 
 * categories. The train() method builds a classifier out of training
 * data and serializes it out to a location on disk. The test() method
 * will categorize a single Chunk and return the String "keep" or 
 * "discard". The getClassifier() deserializes a serialized classifier
 * model from disk if it exists.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BoilerplateClassifier {

  private static final String[] CATEGORIES = {"keep", "discard"};
  private static final int NGRAM_SIZE = 6;
  private static final String MODEL_FILE = 
    "src/test/resources/lingpipe-models/html-textextract-model.ser";
  
  @SuppressWarnings("unchecked")
  public void train(List<Chunk> chunks) throws Exception {
    final DynamicLMClassifier classifier = 
      DynamicLMClassifier.createNGramProcess(CATEGORIES, NGRAM_SIZE);
    CollectionUtils.forAllDo(chunks, new Closure<Chunk>() {
      public void execute(Chunk chunk) {
        if (chunk.keep) {
          classifier.train("keep", chunk.text);
        } else {
          classifier.train("discard", chunk.text);
        }
      }
    });
    ObjectOutputStream oos = new ObjectOutputStream(
       new FileOutputStream(new File(MODEL_FILE)));
    classifier.compileTo(oos);
    oos.close();
  }

  @SuppressWarnings("unchecked")
  public Classifier<CharSequence,JointClassification> getClassifier() 
      throws Exception {
    ObjectInputStream ois = new ObjectInputStream(
      new FileInputStream(new File(MODEL_FILE)));
    Classifier<CharSequence,JointClassification> compiledClassifier = 
      (Classifier<CharSequence,JointClassification>) ois.readObject();
    ois.close();
    return compiledClassifier;
  }

  public String test(Classifier<CharSequence,JointClassification> classifier, 
      Chunk chunk) throws Exception {
    JointClassification jc = classifier.classify(chunk.text);
    String bestCategory = jc.bestCategory();
    return bestCategory;
  }
}
