package com.sparcedge.analytics.similaritytypes;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Implements Cosine Similarity for a term document matrix.
 * A o B = x1*x2 + y1*y2
 * dist(A,0) = sqrt((xa-x0)^2 + (ya-y0)^2) == |A|
 * Therefore:
 * sim(A,B) = cos t = A o B/|A|x|B|  
 * 
 * @author John DeBovis
 * @version $Revision: 21 $
 */
public class CosineSimilarity extends AbstractSimilarity {

	/*
	 * Compute Similarity of targetDoc to all in sourceDoc
	 */
	public RealMatrix similarity(RealMatrix sourceDocuments, RealMatrix targetDoc) {
		RealMatrix res = new OpenMapRealMatrix(sourceDocuments.getRowDimension(),1);
		for(int i=0;i<sourceDocuments.getColumnDimension();i++){
			res.addToEntry(i, 0, computeSimilarity(sourceDocuments.getColumnMatrix(i),targetDoc));
		}
		return res;
	}

	public RealVector similarity(RealMatrix sourceDocuments, RealVector targetDoc){
		RealVector res = new ArrayRealVector(new double[]{});
		try{
			for(int i=0;i<sourceDocuments.getColumnDimension();i++){
				RealVector thisVect = sourceDocuments.getColumnVector(i);
				res = res.append(thisVect.cosine(targetDoc));
			}
		} catch(Exception e){
			System.out.println("zero norm exception");
		}
		return res;
	}


	@Override
	public double computeSimilarity(RealMatrix sourceDoc, RealMatrix targetDoc) {
		if (sourceDoc.getRowDimension() != targetDoc.getRowDimension() || sourceDoc.getColumnDimension() != targetDoc.getColumnDimension() ||
				sourceDoc.getColumnDimension() != 1) {
			throw new IllegalArgumentException("Matrices are not column matrices or not of the same size");
		}
		// max col sum, only 1 col, so...
		double dotProduct = dot(sourceDoc, targetDoc);
		// sqrt of sum of squares of all elements, only one col, so...
		double eucledianDist = sourceDoc.getFrobeniusNorm() * targetDoc.getFrobeniusNorm();
		return dotProduct / eucledianDist;
	}

	private double dot(RealMatrix source, RealMatrix target) {
		int maxRows = source.getRowDimension();
		int maxCols = source.getColumnDimension();
		RealMatrix dotProduct = new OpenMapRealMatrix(maxRows, maxCols);
		for (int row = 0; row < maxRows; row++) {
			for (int col = 0; col < maxCols; col++) {
				dotProduct.setEntry(row, col, source.getEntry(row, col) * target.getEntry(row, col));
			}
		}
		return dotProduct.getNorm();
	}
}
