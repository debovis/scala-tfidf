package com.sparcedge.analytics.recognizers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sparcedge.analytics.tokenizers.Token;
import com.sparcedge.analytics.tokenizers.TokenType;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * Recognizes content words (noun, verb, adjective, and adverb) from a
 * List of Token objects. Only TokenType.WORD tokens are considered in
 * this recognizer, and are converted to TokenType.CONTENT_WORD. Words
 * are looked up against the WordNet dictionary.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ContentWordRecognizer implements IRecognizer {

	private IDictionary dictionary;
	private WordnetStemmer stemmer;
	private String resourceLocation;
	private List<POS> allowablePartsOfSpeech = Arrays.asList(new POS[] {
			POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB});
	
	public ContentWordRecognizer(String resourceLocation){
		this.resourceLocation = resourceLocation;
	}

	public void init() throws Exception {
		File dictFiles = new File(resourceLocation);
		//new URL("file", null, "./temp/WordNet-3.0/dict/")
		this.dictionary = new Dictionary(dictFiles);
		dictionary.open();
		this.stemmer = new WordnetStemmer(this.dictionary);
	}
	
	public void close() {
		this.dictionary.close();
	}

	public List<Token> recognize(List<Token> tokens) {
		List<Token> outputTokens = new ArrayList<Token>();
		for (Token token : tokens) {
			Token outputToken = new Token(token.getValue(), token.getType());

			// Filter for only tokentype word
			if (token.getType() == TokenType.WORD) {
				String word = token.getValue();
				if(!StringUtils.isEmpty(word) && word != null){
					// TODO: Change indexWord to stemmed word if available, and use .WORD instead of CONTENT_WORD
					for (POS allowablePartOfSpeech : allowablePartsOfSpeech) {
//						IIndexWord indexWord = dictionary.getIndexWord(word, allowablePartOfSpeech);
//						if(indexWord !=null) {
//							for(IWordID w : indexWord.getWordIDs()){
//								System.out.println(dictionary.getWord(w).toString());
//							}
//						}
						try{
							List<String> stems = this.stemmer.findStems(word, allowablePartOfSpeech);
							if(!stems.isEmpty()){
								outputToken.setValue(stems.get(0));
								break;
							}
						}
						catch (Exception e) {
							System.out.println("stemmer broke on " + word);
						}

						//          if (indexWord != null) {
							//        	System.out.println(indexWord.getLemma());
						//            outputToken.setType(TokenType.CONTENT_WORD);
						//            break;
						//          }
					}
				}
				outputTokens.add(outputToken);
			}
//			else if (outputToken.getType()!= TokenType.WHITESPACE && outputToken.getType()!= TokenType.STOP_WORD) 
//				System.out.println(String.format(" word - %s, tokentype - %s", outputToken.getType(), outputToken.getValue()));
			
		}
		return outputTokens;
	}
}
