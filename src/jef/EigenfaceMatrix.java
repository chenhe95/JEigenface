package jef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;

import util.JEUtils;

public class EigenfaceMatrix {
	
	private double[][] eigenMatrix = null;

	private EigenfaceMatrix(double[][] eigenMatrix2) {
		eigenMatrix = eigenMatrix2;
	}
	
	/**
	 * Fetches the array of eigenfaces obtained from 
	 * computation on the List<GrayImage> 
	 * @return
	 */
	public double[][] getEigenMatrix() {
		return eigenMatrix;
	}

	public static EigenfaceMatrix importData(List<GrayImage> images) {
		if (images == null) {
			return new EigenfaceMatrix(null);
		} else if (images.isEmpty()) {
			return new EigenfaceMatrix(new double[0][0]);
		}
		
		int m = images.size();
		int nSq = images.get(0).getImage().length;
		int k = m / 2; 
		
		double[][] phis = new double[m][nSq];
		double[] averageMatrix = new double[nSq];
		
		
		for (int i = 0; i < m; ++i) {
			int[] image = images.get(i).getImage();
			if (i >= 1 && image.length != nSq) {
				throw new IllegalArgumentException("EigenfaceMatrix: image matrix must not be jagged");
			}
			
			for (int j = 0; j < nSq; ++j) {
				averageMatrix[j] += image[j];
			}
		}
		
		for (int i = 0; i < nSq; ++i) {
			averageMatrix[i] /= m;
		}
		
		for (int i = 0; i < m; ++i) {
			double[] phi = new double[nSq];
			int[] image = images.get(i).getImage();
			for (int j = 0; j < nSq; ++j) {
				phi[j] = -averageMatrix[j] + image[j];
			}
			phis[i] = phi;
		}

		double[][] covariance = covariance(phis);
		EigenDecomposition eigenDecomposition = new EigenDecomposition(new Array2DRowRealMatrix(covariance));
		
		TreeMap<Double, List<RealVector>> eigenMap = new TreeMap<>(Collections.reverseOrder());

		for (int i = 0; i < m; ++i) {
			double eigenValue = eigenDecomposition.getRealEigenvalue(i);
			// eigenValue = Math.sqrt(Math.pow(eigenValue, 2) +
			// Math.pow(eigenDecomposition.getImagEigenvalue(i), 2));
			// we will skip the imaginary value for now
			eigenValue = JEUtils.roundDouble(eigenValue, 3);
			if (!eigenMap.containsKey(eigenValue)) {
				eigenMap.put(eigenValue, new ArrayList<RealVector>());
			}
			eigenMap.get(eigenValue).add(eigenDecomposition.getEigenvector(i));
		}
		
		double[][] eigenMatrix = new double[k][nSq];
		int counter = 0;
		eigenface_search: for (List<RealVector> vectorList : eigenMap.values()) {
			for (RealVector eigenVector : vectorList) {
				double[] vectorData = eigenVector.toArray();
				double[] eigenFace = new double[nSq];
				for (int i = 0; i < nSq; ++i) {
					double dot = 0;
					for (int j = 0; j < m; ++j) {
						dot += vectorData[j] * phis[j][i];
					}
					eigenFace[i] = dot;
				}
				eigenMatrix[counter++] = eigenFace;
				if (counter >= k) {
					break eigenface_search;
				}
			}
		}
		
		return new EigenfaceMatrix(eigenMatrix);
	}

	private static double[][] covariance(double[][] matrix) {
		double[][] covariance = new double[matrix.length][matrix.length];

		for (int i = 0; i < matrix.length; ++i) {
			for (int j = 0; j < matrix.length; ++j) {
				int dot = 0;
				// dot product of ith column with jth column
				for (int k = 0; k < matrix[0].length; k++) {
					dot += matrix[i][k] * matrix[j][k];
				}
				covariance[i][j] = dot;
			}
		}

		return covariance;
	}
}
