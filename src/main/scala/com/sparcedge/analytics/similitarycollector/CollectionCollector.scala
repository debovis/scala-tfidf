package com.sparcedge.analytics.similitarycollector

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.query._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject,ObjectId,BasicDBList}

import java.util.LinkedHashMap

import com.sparcedge.analytics.mongodb.MongoCollectionWrapper
import com.sparcedge.analytics.indexers.matrix.TfGenerator
import com.sparcedge.analytics.indexers.matrix.IdfIndexer
import com.sparcedge.analytics.similarity.matrix.CosineSimilarity

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector

class collectionCollector {

  
  def getQuestions = {
    
	val questions 	= new LinkedHashMap[String,String]
	val questionLimit = 1020
    
	  val connection = new MongoCollectionWrapper("sparcin")
	  val collection = connection.getCollection  
	  
	  collection.find().limit(questionLimit).foreach{ a =>  
		    val obj = a.asDBObject
			var category:List[String] = List()
			if(obj.get("category") != null){
			   category = obj.as[String]("category").split(",").toList
			}
		    var question = ""
			if(obj.get("question") != null){
			   question = obj.as[String]("question")
			}
		    var id = ""
		    if(obj.get("id") != null){
			   id = obj.get("id").toString
			}
		    questions.put(id,question)
		    //questions = new sparcinQuestions(id,question,category) :: questions
    	}
    	questions
	  }
}

case class sparcinQuestions(id: Integer, question: String, category: List[String])

class cachedTF (var documents:LinkedHashMap[String,String]){
  
  var tfMatrix:RealMatrix 		= TfGenerator.generateMatrix(documents)
  var tfIdfMatrix:RealMatrix 	= new IdfIndexer().transform(tfMatrix)
  var words			 			= TfGenerator.getWordArray(documents).toList
  var corpusWordOccurenceVect:RealVector = TfGenerator.corpusWordFreq(tfMatrix)
  
}








