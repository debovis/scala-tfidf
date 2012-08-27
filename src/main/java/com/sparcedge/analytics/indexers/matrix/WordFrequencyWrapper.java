package com.sparcedge.analytics.indexers.matrix;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

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

public class WordFrequencyWrapper {

	public static Bag<String> getWordFrequencies(FrequencyType type, String text, String resourceLocation) throws Exception {
		if(type == FrequencyType.WORDNET) return getWordNetWordFrequencies(text,resourceLocation);
		else if(type == FrequencyType.LUCENE_NGRAM) return getLuceneNgramWordFrequencies(text);
		else if(type == FrequencyType.NGRAM) return getWordNGramFrequencies(text);
		else if(type == FrequencyType.LUCENE) return getLuceneWordFrequenciesV2(text);
		else return null;
	}
	
	private static Bag<String> getWordNetWordFrequencies(String text,String resourceLocation) throws Exception {
		Bag<String> wordBag = new HashBag<String>();
		WordTokenizer wordTokenizer = new WordTokenizer(resourceLocation);
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
						new ContentWordRecognizer(resourceLocation)
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
	
	private static Bag<String> getLuceneNgramWordFrequencies(String text) throws Exception {
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
	public enum FrequencyType {
		WORDNET,
		LUCENE,
		NGRAM,
		LUCENE_NGRAM
	}
}
