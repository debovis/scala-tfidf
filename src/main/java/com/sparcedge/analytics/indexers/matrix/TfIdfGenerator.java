package com.sparcedge.analytics.indexers.matrix;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sparcedge.analytics.indexers.matrix.IdfIndexer;
import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper;
import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper.FrequencyType;


import org.apache.commons.collections15.Bag;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the term frequency matrix for a set of documents
 * @author John DeBovis
 * @version $Revision: 21 $
 */

public class TfIdfGenerator {
	
	private static Logger log = LoggerFactory.getLogger(TfIdfGenerator.class);
	
	public Map<String,String> 		documents;
	public RealMatrix 				tfmatrix;
	public RealMatrix				tfidfMatrix;
	public RealVector				corpusWordFrequency;
	public Map<Integer,String> 		wordIdValueMap 				= new HashMap<Integer,String>();
	public Map<Integer,String> 		documentIdNameMap 			= new HashMap<Integer,String>();
	public Map<String,Bag<String>> 	documentWordFrequencyMap 	= new HashMap<String,Bag<String>>();
	public SortedSet<String> 		wordSet 					= new TreeSet<String>();
	
	public TfIdfGenerator(Map<String,String> documents) throws Exception{
		this.documents = documents;
		this.generateMatrix();
		this.getTFIDFMatrix();
		this.corpusWordFrequency = getCorpusWordFreq();
	}

	private void generateMatrix() throws Exception{
		Integer 				docId = 0;

		for (String key : documents.keySet()) {
			String text = getText(documents.get(key));			
			Bag<String> wordFrequencies = WordFrequencyWrapper.getWordFrequencies(FrequencyType.WORDNET ,text);
			wordSet.addAll(wordFrequencies.uniqueSet());
			//words.addAll(wordFrequencies);
			documentWordFrequencyMap.put(key, wordFrequencies);
			documentIdNameMap.put(docId, key);
			docId++;
		}	
		
		// create a Map of ids to words from the wordSet
		int wordId = 0;
		for (String word : wordSet) {
			wordIdValueMap.put(wordId, word);
			wordId++;
		}
		// we need a documents.keySet().size() x wordSet.size() matrix to hold this info
		int numDocs = documents.keySet().size();
		int numWords = wordSet.size();
		tfmatrix = new OpenMapRealMatrix(numWords, numDocs);
		for (int i = 0; i < numWords; i++) {
			for (int j = 0; j < numDocs; j++) {
				String docName = documentIdNameMap.get(j);
				Bag<String> wordFrequencies = documentWordFrequencyMap.get(docName);
				String word = wordIdValueMap.get(i);
				int count = wordFrequencies.getCount(word);
				//if(count > 2) System.out.println(word + " " + count);
				tfmatrix.setEntry(i, j, count);
			}
		}
		log.debug("created term frequency matrix with dimensions (" + tfmatrix.getRowDimension() + "," + tfmatrix.getColumnDimension() + ")");
	}
	
	private void getTFIDFMatrix(){
		this.tfidfMatrix = new IdfIndexer().transform(tfmatrix);
		log.debug("created tf-idf matrix with dimensions (" + tfidfMatrix.getRowDimension() + "," + tfidfMatrix.getColumnDimension() + ")");
	}
	
	private RealVector getCorpusWordFreq(){
		// number of words, m x n matrix - words x docs
		int m = tfmatrix.getRowDimension();
		int n = tfmatrix.getColumnDimension();
		RealVector countVector = new ArrayRealVector(m);
		for (int i = 0; i < m; i++) {
			RealVector freqs = tfmatrix.getRowVector(i);
			double numDocs = 0.0D;
			for (int j = 0; j < n; j++) {
				if (freqs.getEntry(j) > 0.0D) {
					numDocs++;
				}
			}
			countVector.setEntry(i, numDocs);
		}
		return countVector;
	}
	
	private static String getText(String text) throws Exception {
		if(text == null || text.length() ==0){
			throw new Exception("document was invalid");
		} else return text;
	}
}
