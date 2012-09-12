package com.sparcedge.analytics.indexers.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sparcedge.analytics.indexers.matrix.IdfIndexer;
import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper;
import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper.FrequencyType;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.math3.linear.*;

import org.slf4j.*;

/**
 * Generate the term frequency matrix for a set of documents
 * @author John DeBovis
 * @version $Revision: 21 $
 */

public class TfIdfGenerator {

	private static Logger log = LoggerFactory.getLogger(TfIdfGenerator.class);
	private Map<String,String> configMap;
	
	public List<TfObject> 			documents;
	public RealMatrix 				tfmatrix;
	public RealMatrix				tfidfMatrix;
	public RealVector				corpusWordFrequency;
	public Map<String,HashBag<String>> 	documentWordFrequencyMap 	= new HashMap<String,HashBag<String>>();
	public SortedSet<String> 		wordSet 					= new TreeSet<String>();

	public TfIdfGenerator(List<TfObject> documents, boolean generate, Map<String,String> configMap) throws Exception{
		if(generate){
			this.documents = documents;
			this.configMap = configMap;
			this.init();
		}
		else {
			this.documents = documents;
		}
	}
	public TfIdfGenerator(){
		this.documents = new ArrayList<TfObject>();
	}

	private void init() throws Exception {

		for (TfObject obj : documents) {
			HashBag<String> wordFrequencies = null;
			if(!obj.hasWordFrequencies){
				String text = getText(obj.documentText);
				wordFrequencies = WordFrequencyWrapper.getWordFrequencies(FrequencyType.WORDNET ,text, configMap);
				obj.setWordFrequencies(wordFrequencies);
			}
			else wordFrequencies = obj.wordFrequencies;
			
			wordSet.addAll(wordFrequencies.uniqueSet());
			documentWordFrequencyMap.put(obj.id.toString(), wordFrequencies);
		}
		this.updateMatricies();
	}

	public static TfIdfGenerator addAndRemoveDocuments(List<TfObject> newDocuments, TfIdfGenerator oldGenerator) throws Exception{
		TfIdfGenerator newGenerator = new TfIdfGenerator();

		int newDocumentsCounter =0;
		Integer documentId=0;
		for(TfObject obj : newDocuments){
			HashBag<String> wordFrequencies = null;
			if(!obj.hasWordFrequencies){
				String text = getText(obj.documentText);	
				
				// we dont have word frequency bag, get it and set it to the obj
				wordFrequencies = WordFrequencyWrapper.getWordFrequencies(FrequencyType.WORDNET ,text,oldGenerator.configMap);
				obj.setWordFrequencies(wordFrequencies);
				obj.setHasWordFrequencies(true);
				
				newGenerator.wordSet.addAll(wordFrequencies.uniqueSet());
				newGenerator.documentWordFrequencyMap.put(obj.id.toString(), wordFrequencies);
				newDocumentsCounter++;
			}
			else {
				wordFrequencies = obj.wordFrequencies;
				newGenerator.wordSet.addAll(wordFrequencies.uniqueSet());
				newGenerator.documentWordFrequencyMap.put(obj.id.toString(), wordFrequencies);
			}
			newGenerator.documents.add(obj);
			documentId++;
		}

		if(newDocumentsCounter>0){
			log.debug(" adding "+ newDocumentsCounter + " to the matrix");
			newGenerator.configMap = oldGenerator.configMap;
			newGenerator.updateMatricies();
			log.debug("created new tf-idf compenents");
			return newGenerator;
		}
		else{
			log.debug(" no new documents, keeping old corpus");
			return oldGenerator;
		}
	}

	private void updateMatricies() throws Exception{
		this.generateTfMatrix();
		this.getTFIDFMatrix();
		this.corpusWordFrequency = this.getCorpusWordFreq();
	}

	private void generateTfMatrix() throws Exception{
		// we need a documents.keySet().size() x wordSet.size() matrix to hold this info
		int numDocs = documents.size();
		int numWords = wordSet.size();
		tfmatrix = new OpenMapRealMatrix(numWords, numDocs);
		int i=0;
		int j=0;
		for(String word : wordSet){
			for(TfObject obj : documents){
				Bag<String> wordFrequencies = documentWordFrequencyMap.get(obj.id);
				int count = wordFrequencies.getCount(word);
				//if(count > 2) System.out.println(word + " " + count);
				tfmatrix.setEntry(i, j, count);
				j++;
			}
			i++;
			j=0;
		}
		log.debug("\n\ncreated term frequency matrix with dimensions (" + tfmatrix.getRowDimension() + "," + tfmatrix.getColumnDimension() + ")\n\n");

	}

	private void getTFIDFMatrix(){
		this.tfidfMatrix = new IdfIndexer().transform(tfmatrix);
		log.debug("\n \n created tf-idf matrix with dimensions (" + tfidfMatrix.getRowDimension() + "," + tfidfMatrix.getColumnDimension() + ")\n\n");
	}

	private RealVector getCorpusWordFreq(){
		log.debug("\n calculating corpusWordFrequency \n");
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
			throw new Exception("document was invalid, check DB import");
		} else return text;
	}
}
