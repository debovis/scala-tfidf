package com.sparcedge.analytics.nerextractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;


public abstract class AbstractNer {


	public abstract List<NamedEntity> findNamedEntities(String text) throws IOException;
	public abstract Bag<String> findEntities(String text) throws IOException;

	/*
	 * Find all entities in a HashMap of string -> string. This will return a Bag of the 
	 * NE in each document.
	 */
	public HashMap<String, Bag<String>> findEntities(HashMap<String, String> documents) throws IOException {
		HashMap<String,Bag<String>> hashBags = new HashMap<String,Bag<String>>();
		for(String key : documents.keySet()){
			hashBags.put(key,findEntities(documents.get(key)));
		}
		return hashBags;
	}

	/*
	 * Find all entities in a HashMap of string -> strings. This makes the assumption 
	 * that all documents are equal and we want to find all the entities in the documents.
	 */

	public Bag<String> findEntitiesFromHashMap(HashMap<String, String> documents) throws IOException {
		Bag<String> strings = new HashBag<String>();
		for(String key : documents.keySet()){
			strings.addAll(findEntities(documents.get(key)));
		}
		return strings;
	}

	/*
	 * Find all entities in a HashMap of string -> string. This will return a Bag of the 
	 * NE in each document.
	 */
	public HashMap<String, List<NamedEntity>> findNamedEntities(HashMap<String, String> documents) throws IOException {
		HashMap<String,List<NamedEntity>> entities = new HashMap<String,List<NamedEntity>>();
		for(String key : documents.keySet()){
			entities.put(key, findNamedEntities(documents.get(key)));
		}
		return entities;
	}

	/*
	 * Find all entities in a HashMap of string -> strings. This makes the assumption 
	 * that all documents are equal and we want to find all the entities in the documents.
	 */
	public List<NamedEntity> findNamedEntitiesAsWhole(HashMap<String, String> documents) throws IOException {
		List<NamedEntity> entities = new ArrayList<NamedEntity>();
		for(String key : documents.keySet()){
			entities.addAll(findNamedEntities(documents.get(key)));
		}
		return entities;
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
