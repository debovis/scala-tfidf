package com.sparcedge.analytics.nerextractor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.model.MaxentModel;
import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class NameFinderLocal {

	public static String[] NAME_TYPES = { "person", "organization", "location"};
	private static final String MODELS_LOCATION = "./src/main/resources/models/";
	
	/* , "date", "time", "percentage", "money" */
	public static NamedEntityType[] ENTITY_TYPES = { NamedEntityType.PERSON,NamedEntityType.ORGANIZATION, NamedEntityType.LOCATION };
	
		/*
		 * NamedEntityType.DATE, NamedEntityType.TIME, NamedEntityType.PERCENTAGE,
		 * NamedEntityType.MONEY
		 */

	NameFinderME[] finders = null;
	NameFinderME personFinder = null;
	SentenceDetectorME englishSentenceDetector;	

	public NameFinderLocal() throws IOException {
		englishSentenceDetector = loadSentenceModel(MODELS_LOCATION + "en-sent.bin");
		finders = new NameFinderME[NAME_TYPES.length];
		for (int i = 0; i < finders.length; i++) {
			finders[i] = loadNameFinder(String.format("%s/en-ner-%s.bin",MODELS_LOCATION,NAME_TYPES[i]));
		}
	}
	public String[] getSentences(String paragraph){
		return englishSentenceDetector.sentDetect(paragraph);
	}

	protected void findNamesInSentence(List<NamedEntity> entities, String[] tokens,	NameFinderME finder, NamedEntityType type) {

		Span[] nameSpans = finder.find(tokens);
		if (nameSpans == null || nameSpans.length == 0)
			return;
		for (Span span : nameSpans) {
			StringBuilder buf = new StringBuilder();
			for (int i = span.getStart(); i < span.getEnd(); i++) {
				buf.append(tokens[i]);
				if(i<span.getEnd()-1) buf.append(" ");
			}
			NamedEntity ne = new NamedEntity();
			ne.setType(type);
			ne.setEntityValue(buf.toString());
			entities.add(ne);
		}
	}


	public List<NamedEntity> findNamedEntities(String text) throws IOException{
		String tokenizerModelLocation = "en-token.bin";
		InputStream inputStream = new FileInputStream(MODELS_LOCATION + tokenizerModelLocation);
		Tokenizer tokenizer = new TokenizerME(new TokenizerModel(inputStream));
		List<NamedEntity> entities = new ArrayList<NamedEntity>();
		String[] sentences = englishSentenceDetector.sentDetect(text);
		for (String sentence : sentences) {
			String[] tokens = tokenizer.tokenize(sentence);
			for (int i=0; i<finders.length; i++) {
				findNamesInSentence(entities, tokens, finders[i], ENTITY_TYPES[i]);
			}
		}
		inputStream.close();
		return entities;
	}

	protected SentenceDetectorME loadSentenceModel(String name) throws IOException 
	{
	    return new SentenceDetectorME( new SentenceModel( new FileInputStream(name)));
	}
	
	private NameFinderME loadNameFinder(String name) throws IOException{
		
		InputStream modelIn = new FileInputStream(name);
		TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
		return new NameFinderME(model);
	}

	//NamedEntityType.java
	public enum NamedEntityType {
		PERSON, ORGANIZATION, LOCATION, DATE, TIME, PERCENTAGE, MONEY;	
	}

	//NamedEntity.java
	public class NamedEntity {
		private NamedEntityType type;
		private String entityValue;
		public NamedEntityType getType() {
			return type;
		}
		public void setType(NamedEntityType type) {
			this.type = type;
		}
		public String getEntityValue() {
			return entityValue;
		}
		public void setEntityValue(String entityValue) {
			this.entityValue = entityValue;
		}
		@Override
		public String toString() {
			return String.format("entity type - %s, entity value - %s", type, entityValue);
		}

	}
}


