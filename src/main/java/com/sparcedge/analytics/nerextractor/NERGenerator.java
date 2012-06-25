package com.sparcedge.analytics.nerextractor;

import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;

import com.sparcedge.analytics.nerextractor.OpenNlpNer;
import com.sparcedge.analytics.nerextractor.StanfordNlpNer;



public class NERGenerator {

//	public static Bag<String> generate(NERType type, HashMap<String,String> documents) throws Exception {
//		Bag<String> bagOfStrings = new HashBag<String>();
//
//		// Use StandfordNLP tools
//		if(type.equals(NERType.StanfordNLP) && type!=null){
//			StanfordNlpNer ner = new StanfordNlpNer();
//			bagOfStrings = ner.findEntitiesFromHashMap(documents);
//		}
//		// Use OpenNLP
//		else if(type.equals(NERType.OpenNLP) && type!=null){
//			OpenNlpNer ner = new OpenNlpNer();
//			bagOfStrings = ner.findEntitiesFromHashMap(documents);
//		}
//		else throw new Exception("unsupported NERType");
//
//		return bagOfStrings;
//	}

	public static Bag<String> generate(AbstractNer ner, HashMap<String,String> documents) throws Exception {
		return ner.findEntitiesFromHashMap(documents);
	}
	public static Bag<String> generate(AbstractNer ner, String document) throws IOException{
		return ner.findEntities(document);
	}
	
	public static OpenNlpNer getOpenNlpNerClassifier() throws IOException{
		return new OpenNlpNer();
	}
	public static StanfordNlpNer getStanfordNlpNerClassifier() throws IOException{
		return new StanfordNlpNer();
	}

}







