package com.sparcedge.analytics.nerextractor;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;


/** This is a demo of calling CRFClassifier programmatically.
 *  <p>
 *  Usage: <code> java -mx400m -cp "stanford-ner.jar:." NERDemo [serializedClassifier [fileName]]</code>
 *  <p>
 *  If arguments aren't specified, they default to
 *  ner-eng-ie.crf-3-all2006.ser.gz and some hardcoded sample text.
 *  <p>
 *  To use CRFClassifier from the command line:
 *  java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier
 *      [classifier] -textFile [file]
 *  Or if the file is already tokenized and one word per line, perhaps in
 *  a tab-separated value format with extra columns for part-of-speech tag,
 *  etc., use the version below (note the 's' instead of the 'x'):
 *  java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier
 *      [classifier] -testFile [file]
 *
 *  @author Jenny Finkel
 *  @author Christopher Manning
 */

public class NERGenerator {

	public static void generate(HashMap<String,String> documents) throws IOException {
		String serializedClassifier = "./src/main/resources/classifiers/english.conll.4class.distsim.crf.ser.gz";
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

		System.out.println("docs " + documents.size());
//		List<CoreLabel> words = new ArrayList<CoreLabel>();
//		for(String key: documents.keySet()){
//			String docValue = documents.get(key);
//			CoreLabel label = new CoreLabel();
//			label.setValue(docValue);
//			label.setWord(docValue);
//			words.add(label);			
//		}
		
		List<List<CoreLabel>> out = classifier.classify(documents.get("doc1"));
        for (List<CoreLabel> sentence : out) {
          for (CoreLabel word : sentence) {
            System.out.print(word.word() + '/' + word.get(AnswerAnnotation.class) + ' ');
          }
          System.out.println();
        }
		
		System.out.println(classifier.classifyToString(documents.get("doc1")));
		System.out.println(classifier.classifyToString(documents.get("doc2")));
		System.out.println(classifier.classifyToString(documents.get("doc3")));
		System.out.println(classifier.classifyToString(documents.get("doc4")));
		System.out.println(classifier.classifyToString(documents.get("doc5")));
		System.out.println(classifier.classifyToString(documents.get("doc6")));
//		for (CoreLabel word : out) {
//			if(word.get(AnswerAnnotation.class) != "O"){
//				System.out.print(word.word() + '/' + word.getString(AnswerAnnotation.class) + ' ');
//			}
//		}
	}	
}







