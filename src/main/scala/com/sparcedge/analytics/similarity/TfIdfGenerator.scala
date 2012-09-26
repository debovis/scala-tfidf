package com.sparcedge.analytics.similarity

import java.util.{HashMap}
import scala.collection.SortedSet

import com.sparcedge.analytics.indexers.matrix._
import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper._
import com.sparcedge.analytics.similarity._
import collection.JavaConversions._
import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.math3.linear._

import org.slf4j._

/**
 * Generate the tf and tfidf matrix for a set of documents
 * @author John DeBovis
 * @version $Revision: 1 $
 */

class TfIdfGenerator(	var documents: List[TfIdfElement], 
						var generate: Boolean, 
						var configMap: Map[String,String],
						var tfMatrix: RealMatrix,
						var tfidfMatrix: RealMatrix,
						var corpusWordFrequency: RealVector,
						var documentWordFrequencyMap: Map[Int,HashBag[String]] = Map[Int,HashBag[String]](),
						var wordSet: SortedSet[String] =  SortedSet[String]()) {
  
  var log = LoggerFactory.getLogger(getClass)
  
  // constructor for object
  def this() = {
    this(List[TfIdfElement](), false,null,null,null,null)
  }
  
  // Main constructor
  def this(documents:List[TfIdfElement], generate: Boolean,configMap: Map[String,String]) = {
    this(documents,generate,configMap,null,null,null)
    
    // build out docs
     documents.foreach{ document =>
    	var wordFrequencies: HashBag[String] = null
    	if(!document.hasWordFrequencies){
			var text = getText(document.value)
			wordFrequencies = WordFrequencyWrapper.getWordFrequencies(FrequencyType.WORDNET,text,configMap)
			document.words = wordFrequencies
    	}
    	else {
    		wordFrequencies = document.words
    	}
    	wordSet = wordSet ++ wordFrequencies.uniqueSet()
    	documentWordFrequencyMap += (document.id -> wordFrequencies)
    }
  	updateMatricies
  }

  def getText(text:String) = {
    if(text == null || text.length() ==0){
			throw new Exception("document was invalid, check DB import");
	} else text;
  }
  def updateMatricies = {
    generateTfMatrix
    getTfidfMatrix
    corpusWordFrequency = getCorpusWordFreq
  }
  
   def generateTfMatrix = {
     var numDocs = documents.size
     var numWords = wordSet.size
     tfMatrix = new OpenMapRealMatrix(numWords,numDocs)
     var i=0
     var j=0
     for(word <- wordSet){
       for(document <- documents){
         var wordFrequencies: HashBag[String] = documentWordFrequencyMap.get(document.id).get
         var count = wordFrequencies.getCount(word)
         tfMatrix.setEntry(i,j,count)
         j=j+1
       }
       i=i+1
       j=0
     }
     log.debug("created term frequency matrix with dimensions (" + tfMatrix.getRowDimension() + "," + tfMatrix.getColumnDimension() + ")");
   }
   
   def getTfidfMatrix() = {
     tfidfMatrix = new IdfIndexer().transform(tfMatrix)
     log.debug("created tf-idf matrix with dimensions (" + tfidfMatrix.getRowDimension() + "," + tfidfMatrix.getColumnDimension() + ")") 
   }
   
  def getCorpusWordFreq() = {
    log.debug("calculating corpusWordFrequency");
    // number of words, m x n matrix - words x docs
    var m = tfMatrix.getRowDimension()
    var n = tfMatrix.getColumnDimension()
    var countVector: RealVector = new ArrayRealVector(m)
        
    // number of words
    var i=0
    for(i <- 0 until m){
      var freqs = tfMatrix.getRowVector(i)
      var numDocs = 0.0D
      var j=0
      // number of docs
      for(j <- 0 until n){
        if(freqs.getEntry(j) > 0.0D){
          numDocs = numDocs + 1
        }
      }
      countVector.setEntry(i,numDocs)
    }
    countVector
  }
  
}

object TfIdfGenerator {
  def addAndRemoveDocuments(newDocuments: List[TfIdfElement], oldGenerator: TfIdfGenerator) ={
    
    var log = LoggerFactory.getLogger(getClass)
    var newGenerator = new TfIdfGenerator()
    
    var newDocumentsCounter=0
    newDocuments.foreach{ document =>
      var wordFrequencies: HashBag[String]  = null
      if(document.recentlyAdded){
        // TODO: better reference to getText method
        var text = oldGenerator.getText(document.value)
        // we dont have word frequency bag, get it and set it to the obj
        wordFrequencies = WordFrequencyWrapper.getWordFrequencies(FrequencyType.WORDNET,text,oldGenerator.configMap)
        document.words = wordFrequencies
        document.hasWordFrequencies = true
        
        newGenerator.wordSet = newGenerator.wordSet ++ wordFrequencies.uniqueSet()
        newGenerator.documentWordFrequencyMap += (document.id -> wordFrequencies)
        newDocumentsCounter += 1
      } else {
        wordFrequencies = document.words
        newGenerator.wordSet = newGenerator.wordSet ++ wordFrequencies.uniqueSet()
        newGenerator.documentWordFrequencyMap += (document.id -> wordFrequencies)
      }
      document.recentlyAdded = false
      newGenerator.documents = document :: newGenerator.documents
    }
    val docCountDifference = (oldGenerator.documents.size - newGenerator.documents.size)
    if(newDocumentsCounter > 0 || docCountDifference !=0 ){
      log.debug(" adding %d to the matrix, difference is document size is %d".format(newDocumentsCounter, docCountDifference))
      newGenerator.configMap = oldGenerator.configMap
      newGenerator.updateMatricies
      log.debug(" created new tf-idf components")
      newGenerator
    } else {
      log.debug("no new or removed documents, keeping old corpus")
      oldGenerator
    }
  }
}
