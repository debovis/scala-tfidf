package com.sparcedge.analytics.nerextractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class OpenNlpNer extends AbstractNer{

	public static String[] NAME_TYPES = { "person", "organization", "location"};
	private static final String MODELS_LOCATION = "./src/main/resources/models/";
	
	/* , "date", "time", "percentage", "money" */
	public static NamedEntityType[] ENTITY_TYPES = { NamedEntityType.PERSON,NamedEntityType.ORGANIZATION, NamedEntityType.LOCATION };
					// NamedEntityType.DATE, NamedEntityType.TIME, NamedEntityType.PERCENTAGE, NamedEntityType.MONEY

	NameFinderME[] finders = null;
	NameFinderME personFinder = null;
	SentenceDetectorME englishSentenceDetector;	
	String tokenizerModelLocation = "en-token.bin";

	public OpenNlpNer() throws IOException {
		englishSentenceDetector = loadSentenceModel(MODELS_LOCATION + "en-sent.bin");
		finders = new NameFinderME[NAME_TYPES.length];
		for (int i = 0; i < finders.length; i++) {
			finders[i] = loadNameFinder(String.format("%s/en-ner-%s.bin",MODELS_LOCATION,NAME_TYPES[i]));
		}
	}
	public String[] getSentences(String paragraph){
		return englishSentenceDetector.sentDetect(paragraph);
	}

	protected SentenceDetectorME loadSentenceModel(String name) throws IOException {
	    return new SentenceDetectorME( new SentenceModel( new FileInputStream(name)));
	}
	
	private NameFinderME loadNameFinder(String name) throws IOException{
		InputStream modelIn = new FileInputStream(name);
		TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
		return new NameFinderME(model);
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

	@Override
	public List<NamedEntity> findNamedEntities(String text) throws IOException{
		InputStream inputStream = new FileInputStream(MODELS_LOCATION + tokenizerModelLocation);
		Tokenizer tokenizer = new TokenizerME(new TokenizerModel(inputStream));
		List<NamedEntity> entities = new ArrayList<NamedEntity>();
		String[] sentences = englishSentenceDetector.sentDetect(text);
		for (String sentence : sentences) {
			String[] tokens = tokenizer.tokenize(sentence);
			for (int i=0; i<finders.length; i++) {				
				Span[] nameSpans = finders[i].find(tokens);
				for (Span span : nameSpans) {
					StringBuilder buf = new StringBuilder();
					for (int j = span.getStart(); j < span.getEnd(); j++) {
						buf.append(tokens[j]);
						if(j<span.getEnd()-1) buf.append(" ");
					}
					NamedEntity ne = new NamedEntity();
					ne.setType(ENTITY_TYPES[i]);
					ne.setEntityValue(buf.toString());
					entities.add(ne);
				}
				
			}
		}
		inputStream.close();
		return entities;
	}
	@Override
	public Bag<String> findEntities(String text) throws IOException {
		Bag<String> entities = new HashBag<String>();
		InputStream inputStream = new FileInputStream(MODELS_LOCATION + tokenizerModelLocation);
		Tokenizer tokenizer = new TokenizerME(new TokenizerModel(inputStream));
		String[] sentences = englishSentenceDetector.sentDetect(text);
		for (String sentence : sentences) {
			String[] tokens = tokenizer.tokenize(sentence);
			for (int i=0; i<finders.length; i++) {				
				Span[] nameSpans = finders[i].find(tokens);
				for (Span span : nameSpans) {
					StringBuilder buf = new StringBuilder();
					for (int j = span.getStart(); j < span.getEnd(); j++) {
						buf.append(tokens[j]);
						if(j<span.getEnd()-1) buf.append(" ");
					}
					entities.add(buf.toString());
				}
				
			}
		}
		inputStream.close();
		return entities;
	}
	
}


