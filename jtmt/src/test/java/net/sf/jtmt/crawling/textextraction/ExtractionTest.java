// $Id$
package net.sf.jtmt.crawling.textextraction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.aliasi.classify.Classifier;
import com.aliasi.classify.JointClassification;

/**
 * Harness for testing the test extraction algorithm.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ExtractionTest {
  
  @Test
  public void testExtract() throws Exception {
    File[] files = new File("/tmp/testpages").listFiles();
    int numFiles = files.length;
    int trainFiles = (int) Math.floor(numFiles * 0.9);
    DensityFilter df = new DensityFilter();
    BoilerplateClassifier bpc = new BoilerplateClassifier();
    // extract and train on first 90% data
    for (int i = 0; i < trainFiles; i++) {
      File file = files[i];
      String content = FileUtils.readFileToString(file, "UTF-8");
      List<Chunk> densityFilteredChunks = df.filter(content);
      bpc.train(densityFilteredChunks);
    }
    // test on remaining 10% data
    Classifier<CharSequence,JointClassification> classifier = 
      bpc.getClassifier();
    int numTests = 0;
    int numErrors = 0;
    for (int i = trainFiles; i < numFiles; i++) {
      File file = files[i];
      String content = FileUtils.readFileToString(file, "UTF-8");
      List<Chunk> densityFilteredChunks = df.filter(content);
      List<Chunk> boilerplateFilteredChunks = new ArrayList<Chunk>();
      for (Chunk densityFilteredChunk : densityFilteredChunks) {
        String category = bpc.test(classifier, densityFilteredChunk);
        if ("keep".equals(category)) {
          boilerplateFilteredChunks.add(densityFilteredChunk);
          if (! densityFilteredChunk.keep) {
            numErrors++;
          }
        } else {
          if (densityFilteredChunk.keep) {
            numErrors++;
          }
        }
        numTests++;
      }
      // print out to console for manual verification
      System.out.println("==== File: " + file.getName());
      for (Chunk boilerplateFilteredChunk : boilerplateFilteredChunks) {
        System.out.println(boilerplateFilteredChunk.text);
      }
    }
    double errorRate = (double) numErrors * 100.0D / (double) numTests;
    System.out.println("==== Error % = " + errorRate);
  }
}
