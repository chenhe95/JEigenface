package jef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import util.JEUtils;

public class EigenfaceMatrix {

	private double[][] eigenMatrix = null;
	private double[] averageFace = null;
	private int imageCount = 0;

	private EigenfaceMatrix(double[][] eigenMatrix, double[] averageFace, int imageWidth, int imageHeight, int imageCount) {
		this.eigenMatrix = eigenMatrix;
		this.averageFace = averageFace;
		this.imageCount = imageCount;
	}
	
	public static double probeDistance(double[] probe1, double[] probe2) {
		
		if (probe1 == null || probe2 == null || probe1.length != probe2.length) {
			throw new IllegalArgumentException("probeDistance(): illegal inputs");
		}
		
		double sum = 0;
		for (int i = 0; i < probe1.length; ++i) {
			double dif = probe1[i] - probe2[i];
			sum += dif*dif;
		}
		return Math.sqrt(sum);
	}

	/**
	 * Fetches the array of eigenfaces obtained from computation on the List
	 * <GrayImage>
	 * 
	 * @return
	 */
	public double[][] getEigenMatrix() {
		return eigenMatrix;
	}

	public double[] getFace(int faceID) {
		if (faceID >= eigenMatrix.length || faceID < 0) {
			throw new IllegalArgumentException("getFace(): Face ID invalid: " + faceID);
		}
		return eigenMatrix[faceID];
	}

	/**
	 * Probes the trained face recognition data to see if it is a good match to
	 * any known face
	 * 
	 * @param image
	 * @return the weights of the probe 
	 */
	public double[] probe(double[] imageData) {
		double[] normalized = subtractAverage(imageData);
		double[] omega = new double[eigenMatrix.length];
		for (int i = 0; i < omega.length; ++i) {
			double dot = 0;
			for (int j = 0; j < eigenMatrix[i].length; ++j) {
				dot += eigenMatrix[i][j] * normalized[j];
			}
			omega[i] = dot;
		}
		return omega;
	}
	
	public double[][] selfProbe() {
		double[][] probe = new double[eigenMatrix.length][imageCount];
		for (int i = 0; i < probe.length; ++i) {
			probe[i] = probe(eigenMatrix[i]);
		}
		return probe;
	}

	/**
	 * normalizes the probe image with the average face
	 * @param image
	 * @return
	 */
	private double[] subtractAverage(double[] image) {
		if (image.length != averageFace.length) {
			throw new IllegalArgumentException(
					"normalize(): Illegal image length, expected" + averageFace.length + " but was " + image.length);
		}
		double[] normalized = new double[image.length];
		for (int i = 0; i < image.length; ++i) {
			normalized[i] = (((double) image[i]) - averageFace[i]);
		}
		// PixelDisplay.displayImage(normalized, 220, 220);
		return normalized;
	}
	
	public static double[] normalize(double[] face) {
		double[] copy = new double[face.length];
		double norm = 0;
		for (double d : face) {
			norm += d*d;
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < face.length; ++i) {
			copy[i] = face[i] / norm;
		}
		return copy;
	}

	public static EigenfaceMatrix importData(List<GrayImage> images, int imageWidth, int imageHeight) {
		if (images == null) {
			throw new IllegalArgumentException("EigenfaceMatrix: null input");
		} else if (images.isEmpty()) {
			return new EigenfaceMatrix(new double[0][0], new double[0], 0, 0, 0);
		}

		int m = images.size();
		int nSq = images.get(0).getImage().length;
		int k = m ;

		
		/*
		 * in the format of 
		 * 
		 * 1 1 1 1 1 1 1 1 1 ...
		 * 2 2 2 2 2 2 2 2 2 ...
		 * 3 3 3 3 3 3 3 3 3 ...
		 */
		double[][] A = new double[m][nSq];
		double[] avg = new double[nSq];

		for (int i = 0; i < m; ++i) {
			double[] image = images.get(i).getImage();
			if (i >= 1 && image.length != nSq) {
				throw new IllegalArgumentException("EigenfaceMatrix: image matrix must not be jagged");
			}

			for (int j = 0; j < nSq; ++j) {
				avg[j] += image[j];
			}
		}
		
		for (int i = 0; i < nSq; ++i) {
			avg[i] /= m;
		}
		
		//PixelDisplay.displayImage(avg, imageWidth, imageHeight);
		for (int i = 0; i < m; ++i) {
			double[] phi = new double[nSq];
			double[] image = images.get(i).getImage();
			for (int j = 0; j < nSq; ++j) {
				phi[j] = (-avg[j] + image[j]);
			}
			A[i] = phi;
		}
		
		double[][] covariance = covariance(A);
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
				double[] vectorData = (eigenVector.toArray());
				double[] eigenFace = new double[nSq];

				for (int i = 0; i < nSq; ++i) {
					double dot = 0;
					for (int j = 0; j < m; ++j) {
						dot += vectorData[j] * A[j][i];
					}
					eigenFace[i] = dot;
				}
				
				eigenMatrix[counter++] = (eigenFace);
				if (counter >= k) {
					break eigenface_search;
				}
			}
		}

		return new EigenfaceMatrix(eigenMatrix, avg, imageWidth, imageHeight, m);
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
