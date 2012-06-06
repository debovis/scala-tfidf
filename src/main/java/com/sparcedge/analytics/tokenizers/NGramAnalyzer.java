package com.sparcedge.analytics.tokenizers;

import java.io.Reader;

import org.apache.lucene.util.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ngram.*;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class NGramAnalyzer extends Analyzer{

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		//return new StopFilter(Version.LUCENE_35, new LowerCaseFilter(Version.LUCENE_35, new NGramTokenizer(reader,2,4)),StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		return new NGramTokenizer(reader,2,4);
	}

}
