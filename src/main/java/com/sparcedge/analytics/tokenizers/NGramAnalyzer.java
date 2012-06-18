package com.sparcedge.analytics.tokenizers;

import java.io.Reader;

import org.apache.lucene.util.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class NGramAnalyzer extends Analyzer{
	
	private static final int minSingle = 2;
	private static final int maxSingle = 4;

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
//		StopFilter stopFilter = new StopFilter(Version.LUCENE_35, new StandardTokenizer(Version.LUCENE_35,reader),StopAnalyzer.ENGLISH_STOP_WORDS_SET);
//		stopFilter.setEnablePositionIncrements(false);
//		return new LowerCaseFilter(Version.LUCENE_35,new ShingleFilter(stopFilter,minSingle,maxSingle));
		
		StandardTokenizer stopFilter = new StandardTokenizer(Version.LUCENE_35,reader);
		return new LowerCaseFilter(Version.LUCENE_35,new ShingleFilter(stopFilter,minSingle,maxSingle));
	}

}
