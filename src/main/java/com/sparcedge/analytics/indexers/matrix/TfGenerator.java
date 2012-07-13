package com.sparcedge.analytics.indexers.matrix;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.StringReader;

import com.sparcedge.analytics.recognizers.BoundaryRecognizer;
import com.sparcedge.analytics.recognizers.ContentWordRecognizer;
import com.sparcedge.analytics.recognizers.IRecognizer;
import com.sparcedge.analytics.recognizers.RecognizerChain;
import com.sparcedge.analytics.recognizers.StopwordRecognizer;
import com.sparcedge.analytics.tokenizers.NGramAnalyzer;
import com.sparcedge.analytics.tokenizers.Token;
import com.sparcedge.analytics.tokenizers.TokenType;
import com.sparcedge.analytics.tokenizers.WordNGramTokenizer;
import com.sparcedge.analytics.tokenizers.WordTokenizer;

import org.apache.lucene.util.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.*;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the word occurence vector for a document collection.
 * @author Sujit Pal
 * @version $Revision: 21 $
 */

public class TfGenerator {
	
	private static Logger log = LoggerFactory.getLogger(TfGenerator.class);


	public static RealMatrix generateMatrix(HashMap<String, String> documents) throws Exception{
		// create needed variables
		RealMatrix matrix;
		Map<Integer,String> 	wordIdValueMap 				= new HashMap<Integer,String>();
		Map<Integer,String> 	documentIdNameMap 			= new HashMap<Integer,String>();
		Map<String,Bag<String>> documentWordFrequencyMap 	= new HashMap<String,Bag<String>>();
		SortedSet<String> 		wordSet 					= new TreeSet<String>();
		Integer 				docId = 0;

		for (String key : documents.keySet()) {
			String text = getText(documents.get(key));			
			Bag<String> wordFrequencies = getWordFrequencies(text);
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
		matrix = new OpenMapRealMatrix(numWords, numDocs);
		for (int i = 0; i < numWords; i++) {
			for (int j = 0; j < numDocs; j++) {
				String docName = documentIdNameMap.get(j);
				Bag<String> wordFrequencies = documentWordFrequencyMap.get(docName);
				String word = wordIdValueMap.get(i);
				int count = wordFrequencies.getCount(word);
				//if(count > 2) System.out.println(word + " " + count);
				matrix.setEntry(i, j, count);
			}
		}
		log.debug("created matrix with dimensions (" + matrix.getRowDimension() + "," + matrix.getColumnDimension() + ")");
		return matrix;
	}
	
	public static Object[] getWordArray(HashMap<String,String> documents) throws Exception{
		SortedSet<String> 		wordSet 					= new TreeSet<String>();
		
		for (String key : documents.keySet()) {
			String text = getText(documents.get(key));			
			Bag<String> wordFrequencies = getWordFrequencies(text);
			wordSet.addAll(wordFrequencies.uniqueSet());
		}
		return wordSet.toArray();
	}

	public static Bag<String> getWordFrequencies(String text) throws Exception {
		Bag<String> wordBag = new HashBag<String>();
		WordTokenizer wordTokenizer = new WordTokenizer();
		wordTokenizer.setText(text);
		List<Token> tokens = new ArrayList<Token>();
		Token token = null;
		while ((token = wordTokenizer.nextToken()) != null) {
			tokens.add(token);
		}
		RecognizerChain recognizerChain = new RecognizerChain(
				Arrays.asList(new IRecognizer[] {
						new BoundaryRecognizer(),
						new StopwordRecognizer(),
						new ContentWordRecognizer()
				}));
		//  new AbbreviationRecognizer(dataSource),
		//  new PhraseRecognizer(dataSource),
		recognizerChain.init();
		List<Token> recognizedTokens = recognizerChain.recognize(tokens);
		for (Token recognizedToken : recognizedTokens) {
			if (	recognizedToken.getType() == TokenType.ABBREVIATION ||  
					recognizedToken.getType() == TokenType.PHRASE ||  
					recognizedToken.getType() == TokenType.WORD ||
					recognizedToken.getType() == TokenType.INTERNET) {
				wordBag.add(StringUtils.lowerCase(recognizedToken.getValue()));
			}
		}
		recognizerChain.close();
		return wordBag;
	}
	
	private static Bag<String> getLuceneWordFrequencies(String text) throws Exception {
		Bag<String> wordBag = new HashBag<String>();
		
		ShingleAnalyzerWrapper ngram = new ShingleAnalyzerWrapper(Version.LUCENE_35,2,2);
		TokenStream tokenStream = ngram.tokenStream("content", new StringReader(text));
		//OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		while (tokenStream.incrementToken()){
			String tok = charTermAttribute.toString();
			wordBag.add(tok);
		}
		return wordBag;
	}
	
	private static Bag<String> getLuceneWordFrequenciesV2(String text) throws Exception {
		Bag<String> wordBag = new HashBag<String>();
		NGramAnalyzer ngram = new NGramAnalyzer();
		TokenStream tokenStream = ngram.tokenStream("content", new StringReader(text));
		//OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		while (tokenStream.incrementToken()){
			String tok = charTermAttribute.toString();
			if(tok.split(" ").length > 1){
				wordBag.add(tok);
			}
		}
		return wordBag;
	}
	
	private static Bag<String> getWordNGramFrequencies(String text) throws Exception {
		return WordNGramTokenizer.generate(text, 2, 2);
	}

	@SuppressWarnings("unused")
	private String getText(Reader reader) throws Exception {
		StringBuilder textBuilder = new StringBuilder();
		char[] cbuf = new char[1024];
		int len = 0;
		while ((len = reader.read(cbuf, 0, 1024)) != -1) {
			textBuilder.append(ArrayUtils.subarray(cbuf, 0, len));
		}
		reader.close();
		return textBuilder.toString();
	}

	private static String getText(String text) throws Exception {
		if(text == null || text.length() ==0){
			throw new Exception("document was invalid");
		} else return text;
	}
	
	public static RealVector corpusWordFreq(RealMatrix matrix){
		// number of words, m x n matrix - words x docs
		int m = matrix.getRowDimension();
		int n = matrix.getColumnDimension();
		RealVector countVector = new ArrayRealVector(m);
		for (int i = 0; i < m; i++) {
			RealVector freqs = matrix.getRowVector(i);
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
}
