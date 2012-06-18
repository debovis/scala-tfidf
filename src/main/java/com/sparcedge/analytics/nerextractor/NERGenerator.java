package com.sparcedge.analytics.nerextractor;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;


public class NERGenerator {

	public static Bag<String> generate(HashMap<String,String> documents) throws IOException {
		String serializedClassifier = "./src/main/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		Bag<String> bagOfStrings = new HashBag<String>();

		for(String key : documents.keySet()){
			List<List<CoreLabel>> out = classifier.classify(documents.get(key));
			for (List<CoreLabel> sentence : out) {
				for (CoreLabel word : sentence) {
					String cat = word.get(AnswerAnnotation.class);
					if(cat.toLowerCase().equals("organization") || cat.toLowerCase().equals("person")){
						bagOfStrings.add(word.word());
					}
				}
			}
		}
		return bagOfStrings;
	}	
}







