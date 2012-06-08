package com.sparcedge.analytics.tokenizers;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Given a sentence, generates the specified word N-grams from it and
 * returns it as a List of String.
 * @author Sujit Pal
 * @version $Revision$
 */
public class WordNGramTokenizer {

	private final Log log = LogFactory.getLog(getClass());

	public static Bag<String> generate(String input, int minGram, int maxGram) 
			throws IOException {
		Bag<String> wordgrams = new HashBag<String>();
		List<String> tokens = new LinkedList<String>();
		BreakIterator wordBreakIterator = BreakIterator.getWordInstance(Locale.getDefault());
		wordBreakIterator.setText(input);
		int current = 0;
		int gindex = 0;
		for (;;) {
			int end = wordBreakIterator.next();
			if (end == BreakIterator.DONE) {
				// take care of the remaining word grams
				while (tokens.size() >= minGram) {
					wordgrams.add(StringUtils.join(tokens.iterator(), " "));
					tokens.remove(0);
				}
				break;
			}
			String nextWord = input.substring(current, end);
			current = end;
			if ((StringUtils.isBlank(nextWord)) ||
					(nextWord.length() == 1 && nextWord.matches("\\p{Punct}"))) {
				continue;
			}
			gindex++;
			tokens.add(StringUtils.lowerCase(nextWord));
			if (gindex == maxGram) {
				for (int i = minGram; i <= maxGram; i++) {
					wordgrams.add(StringUtils.join(tokens.subList(0, i).iterator(), " "));
				}
				gindex--;
				tokens.remove(0);
			}
		}
		return wordgrams;
	}

}


