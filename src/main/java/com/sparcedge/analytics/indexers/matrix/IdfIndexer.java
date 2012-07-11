package com.sparcedge.analytics.indexers.matrix;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Reduces the weight of words which are commonly found (ie in more
 * documents). The factor by which it is reduced is chosen from the book
 * as:
 * f(m) = 1 + log(N/d(m))
 * where N = total number of docs in collection
 *       d(m) = number of docs containing word m
 * so where a word is more frequent (ie d(m) is high, f(m) would be low.
 * 
 * @author Sujit Pal
 * @version $Revision: 44 $
 */
public class IdfIndexer implements Transformer<RealMatrix,RealMatrix> {

	public RealMatrix transform(RealMatrix matrix) {
		RealVector wordFreq = corpusWordFreq(matrix);
		// Phase 1: apply IDF weight to the raw word frequencies
		int n = matrix.getColumnDimension();
		for (int j = 0; j < matrix.getColumnDimension(); j++) {
			for (int i = 0; i < matrix.getRowDimension(); i++) {
				double matrixElement = matrix.getEntry(i, j);
				if (matrixElement > 0.0D) {
					// get number of documents that contain this word
					double dm = wordFreq.getEntry(i);
					// set matrix entry to f(m) = 1 + log(N/d(m))
					matrix.setEntry(i, j, matrix.getEntry(i,j) * (1 + Math.log(n) - Math.log(dm)));
				}
			}
		}
		// Phase 2: normalize the word scores for a single document
		for (int j = 0; j < matrix.getColumnDimension(); j++) {
			double sum = sum(matrix.getSubMatrix(0, matrix.getRowDimension() -1, j, j));
			for (int i = 0; i < matrix.getRowDimension(); i++) {
				if (sum > 0.0D) {
					matrix.setEntry(i, j, (matrix.getEntry(i, j) / sum));
				} else {
					matrix.setEntry(i, j, 0.0D);
				}
			}
		}
		return matrix;
	}

	private static double sum(RealMatrix colMatrix) {
		double sum = 0.0D;
		for (int i = 0; i < colMatrix.getRowDimension(); i++) {
			sum += colMatrix.getEntry(i, 0);
		}
		return sum;
	}

	@SuppressWarnings("unused")
	private static double countDocsWithWord(RealMatrix rowMatrix) {
		double numDocs = 0.0D;
		for (int j = 0; j < rowMatrix.getColumnDimension(); j++) {
			if (rowMatrix.getEntry(0, j) > 0.0D) {
				numDocs++;
			}
		}
		return numDocs;
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
