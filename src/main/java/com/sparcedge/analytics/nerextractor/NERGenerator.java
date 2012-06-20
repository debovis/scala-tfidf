package com.sparcedge.analytics.nerextractor;

import java.util.HashMap;
import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;

import com.sparcedge.analytics.nerextractor.OpenNlpNer;
import com.sparcedge.analytics.nerextractor.StanfordNlpNer;



public class NERGenerator {

	public static Bag<String> generate(NERType type, HashMap<String,String> documents) throws Exception {
		Bag<String> bagOfStrings = new HashBag<String>();

		// Use StandfordNLP tools
		if(type.equals(NERType.StanfordNLP) && type!=null){
			StanfordNlpNer ner = new StanfordNlpNer();
			bagOfStrings = ner.findEntitiesFromHashMap(documents);
		}
		// Use OpenNLP
		else if(type.equals(NERType.OpenNLP) && type!=null){
			OpenNlpNer ner = new OpenNlpNer();
			bagOfStrings = ner.findEntitiesFromHashMap(documents);
		}
		else throw new Exception("unsupported NERType");

		return bagOfStrings;
	}

	public enum NERType {
		OpenNLP, StanfordNLP;
	}

}







