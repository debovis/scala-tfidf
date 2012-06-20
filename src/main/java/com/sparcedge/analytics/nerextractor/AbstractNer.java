package com.sparcedge.analytics.nerextractor;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections15.Bag;


public abstract class AbstractNer {


	public abstract List<NamedEntity> findNamedEntities(String text) throws IOException;
	public abstract Bag<String> findEntities(String text) throws IOException;

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
