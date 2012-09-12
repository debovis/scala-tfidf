package com.sparcedge.analytics.indexers.matrix;

import org.apache.commons.collections15.bag.HashBag;

public class TfObject {

	public Integer id;
	public String _id;
	public String documentText;
	public boolean hasWordFrequencies = false;
	public HashBag<String> wordFrequencies = null;
	
	public TfObject(Integer id, String _id, String documentText, HashBag<String> wordFrequencies){
		this.id=id;
		this._id = _id;
		this.documentText = documentText;
		if(wordFrequencies != null && wordFrequencies.size()>0){
			this.wordFrequencies = wordFrequencies;
			this.hasWordFrequencies = true;
		}
	}
	public TfObject(int id, String documentText){
		this.id=id;
		this.documentText = documentText;
	}
	public TfObject(int id, String _id, String documentText){
		this.id = id;
		this._id = _id;
		this.documentText = documentText;
	}
	
	//Setters
	public void setId(int id){
		this.id=id;
	}
	public void set_id(String _id){
		this._id=_id;
	}
	public void setDocumentText(String documentText){
		this.documentText = documentText;
	}
	public void setHasWordFrequencies(boolean doesOrNot){
		this.hasWordFrequencies = doesOrNot;
	}
	public void setWordFrequencies(HashBag<String> wf){
		this.wordFrequencies = wf;
	}
}
