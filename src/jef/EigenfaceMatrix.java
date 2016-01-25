package jef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;

import util.JEUtils;

public class EigenfaceMatrix {
	private int[][] eigenMatrix = null;

	private EigenfaceMatrix(int[][] data) {
		eigenMatrix = data;
	}

	public static EigenfaceMatrix importData(List<GrayImage> images) {
		if (images == null) {
			return new EigenfaceMatrix(null);
		} else if (images.isEmpty()) {
			return new EigenfaceMatrix(new int[0][0]);
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

		// covariance = m by m matrix whose value is transpose(matrix) *
		// (matrix)
		double[][] covariance = covariance(phis);
		Array2DRowRealMatrix eigenMatrix = new Array2DRowRealMatrix(covariance);
		EigenDecomposition eigenDecomposition = new EigenDecomposition(eigenMatrix);

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
		
		List<RealVector> selected = new ArrayList<>();
		search: for (Entry<Double, List<RealVector>> eigen : eigenMap.entrySet()) {
			for (RealVector eigenVector : eigen.getValue()) {
				selected.add(eigenVector);
				if (--k < 0) {
					break search;
				}
			}
			
		}
		for (RealVector rv : selected) {
			System.out.println(rv.getDimension() + " " + Arrays.toString(rv.toArray()));
		}
		return null;
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
