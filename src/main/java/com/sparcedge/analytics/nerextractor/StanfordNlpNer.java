package com.sparcedge.analytics.nerextractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;

import scala.actors.threadpool.Arrays;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;

public class StanfordNlpNer extends AbstractNer{

	private final String serializedClassifier = "./src/main/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
	private AbstractSequenceClassifier<CoreLabel> classifier;
	private String[] allowedEntities;

	public StanfordNlpNer(String[] allowedEntities) {
		this.classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		this.allowedEntities = allowedEntities;
	}
	public StanfordNlpNer() {
		this.classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		this.allowedEntities = new String[]{"person", "organization", "location"};
	}

	private List<List<CoreLabel>> findNamedEntitiesCoreLabel(String text){
		return classifier.classify(text);
	}

	@Override
	public List<NamedEntity> findNamedEntities(String text) {
		List<NamedEntity> entities = new ArrayList<NamedEntity>();
		List<List<CoreLabel>> labels = findNamedEntitiesCoreLabel(text);
		for(List<CoreLabel> sentence : labels){
			for(CoreLabel word : sentence){
				String type = word.get(AnswerAnnotation.class);
				NamedEntity entity = new NamedEntity();
				entity.setEntityValue(word.word());
				entity.setType(NamedEntityType.valueOf(type));
				entities.add(entity);
			}
		}
		return entities;
	}
	
	@Override
	public Bag<String> findEntities(String text) {
		Bag<String> entities = new HashBag<String>();
		List<List<CoreLabel>> labels = findNamedEntitiesCoreLabel(text);
		for(List<CoreLabel> sentence : labels){
			for(CoreLabel word : sentence){
				String cat = word.get(AnswerAnnotation.class);
				if(Arrays.asList(allowedEntities).contains(cat.toLowerCase())){
					entities.add(word.word());
				}
			}
		}
		return entities;
	}

}
