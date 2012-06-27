package com.sparcedge.analytics.nerextractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

public class StanfordNlpNer extends AbstractNer{

	private final String serializedClassifier = "./src/main/resources/classifiers/english.conll.4class.distsim.crf.ser.gz";
	private AbstractSequenceClassifier<CoreLabel> classifier;
	private String[] allowedEntities;

	@SuppressWarnings("unchecked")
	public StanfordNlpNer(String[] allowedEntities) {
		this.classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		this.allowedEntities = allowedEntities;
	}
	@SuppressWarnings("unchecked")
	public StanfordNlpNer() {
		this.classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		this.allowedEntities = new String[]{"person", "organization", "location", "misc"};
	}

	private List<Triple<String,Integer,Integer>> findNamedEntitiesTriple(String text){
		return classifier.classifyToCharacterOffsets(text);
	}

	@Override
	public List<NamedEntity> findNamedEntities(String text) {
		List<NamedEntity> entities = new ArrayList<NamedEntity>();
		List<Triple<String,Integer,Integer>> labels = findNamedEntitiesTriple(text);
		for(Triple<String,Integer,Integer> l : labels){
			String cat = l.first();
			if(Arrays.asList(allowedEntities).contains(cat.toLowerCase())){
				Integer start = l.second();
				Integer stop = l.third();
				String word = text.substring(start, stop);
				NamedEntity entity = new NamedEntity();
				entity.setEntityValue(word);
				entity.setType(NamedEntityType.valueOf(cat));
				entities.add(entity);
			}
		}
		return entities;
	}
	
	@Override
	public Bag<String> findEntities(String text) {
		Bag<String> entities = new HashBag<String>();
		List<Triple<String,Integer,Integer>> labels = findNamedEntitiesTriple(text);
		for(Triple<String,Integer,Integer> l : labels){
			String cat = l.first();
			if(Arrays.asList(allowedEntities).contains(cat.toLowerCase())){
				Integer start = l.second();
				Integer stop = l.third();
				String word = text.substring(start, stop);
				entities.add(word);
			}
		}
		return entities;
	}

}

//@Override
//public Bag<String> findEntities(String text) {
//	Bag<String> entities = new HashBag<String>();
//	List<List<CoreLabel>> labels = findNamedEntitiesCoreLabel(text);
//	for(List<CoreLabel> sentence : labels){
//		for(CoreLabel word : sentence){
//			
//			String cat = word.get(AnswerAnnotation.class);
//			if(Arrays.asList(allowedEntities).contains(cat.toLowerCase())){
//				entities.add(word.word());
//			}
//		}
//	}
//	return entities;
//}













