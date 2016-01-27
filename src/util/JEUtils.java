package util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import graphics.PixelDisplay;
import jef.EigenfaceMatrix;
import jef.GrayImage;

public class JEUtils {

	private static final double[] LUMINOSITY_WEIGHTS = { 0.21, 0.72, 0.07 };
	private static final int DECIMAL_PLACES = 3;

	public static GrayImage loadTrainingImage(String path) {
		GrayImage gImage = null;
		BufferedImage img = null;
		try {
			File imageFile = new File(path);
			if (!imageFile.exists()) {
				throw new IllegalArgumentException("loadTrainingImage(): non-existant file");
			}
			img = ImageIO.read(imageFile);
			if (img != null) {
				WritableRaster raster = img.getRaster();
				byte rawPixels[] = ((DataBufferByte) raster.getDataBuffer()).getData();
				int pixLength = 3;
				int pixOffset = 0;
				if (img.getAlphaRaster() != null) {
					pixLength = 4;
					pixOffset = 1;
				}
				int imgWidth = img.getWidth();
				int imgHeight = img.getHeight();
				double resultPixels[] = new double[rawPixels.length / pixLength];
				for (int i = 0, j = 0; i < rawPixels.length - pixOffset - 2; i += pixLength, ++j) {
					resultPixels[j] = averagedGrayscale(rawPixels[i + pixOffset] & 0xFF,
							rawPixels[i + pixOffset + 1] & 0xFF, rawPixels[i + pixOffset + 2] & 0xFF);
				}
				gImage = new GrayImage(resultPixels, imgWidth, imgHeight);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gImage;
	}

	public static int luminosityGrayscale(int r, int g, int b) {
		return (int) ((LUMINOSITY_WEIGHTS[0] * r) + (LUMINOSITY_WEIGHTS[1] * g) + (LUMINOSITY_WEIGHTS[2] * b));
	}

	public static int averagedGrayscale(int r, int g, int b) {
		return (r + g + b) / 3;
	}

	/**
	 * Rounds the given 'number' to decimalPlaces number of decimal places
	 * 
	 * If the decimal places given is illegal, then it uses the default
	 * 
	 * @param number
	 * @param decimalPlaces
	 * @return
	 */
	public static double roundDouble(double number, int decimalPlaces) {
		if (decimalPlaces < 0) {
			decimalPlaces = DECIMAL_PLACES;
		}
		return new BigDecimal(number).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
	}

	public static void main(String[] args) {
		// GrayImage img = loadTrainingImage("test/004.png");
		ArrayList<GrayImage> trainingSet = new ArrayList<>();
		for (int i = 0; i <= 4; ++i) {
			trainingSet.add(loadTrainingImage("test/jason" + i + ".jpg"));
		}
		EigenfaceMatrix mat = EigenfaceMatrix.importData(trainingSet, GrayImage.IMAGE_WIDTH, GrayImage.IMAGE_HEIGHT);
		
		double[] face0 = mat.getFace(0);
		int[] image = new int[face0.length];

		
		for (int i = 0; i < image.length; i++) {
			image[i] = ((int) face0[i]) & 0xff;
		}
		PixelDisplay.displayImage(image, 220, 220);
		System.out.println("me vs jason img comparison " + EigenfaceMatrix.probeDistance(mat.selfProbe()[0], mat.probe(loadTrainingImage("test/000.jpg").getImage())));
		GrayImage jasonfull = loadTrainingImage("test/jason0.jpg");
		double minres = Double.MAX_VALUE;
		double[] imageData = jasonfull.getImage();
		int mini = 0, minj = 0;
		for (int i = 0; i + 219 < jasonfull.getHeight(); i++) {
			for (int j = 0; j + 219 < jasonfull.getWidth(); j++) {
				
				double[] imageprobe = new double[220*220];
				
				// PixelDisplay.displayImage(imageData, jasonfull.getWidth(), jasonfull.getHeight());
				
				for (int m = i; m < i + 220; m++) {
					for (int n = j; n < j + 220; n++) {
						imageprobe[(m-i) * 220 + (n-j)] = imageData[(m) * jasonfull.getWidth() + n];
					}
				}
				int[] dat = new int[220*220];
				for (int h = 0; h < 220*220; h++) {
					dat[h] = (int) imageprobe[h];
				}
				PixelDisplay.displayImage(dat, 220, 220);
				double res = EigenfaceMatrix.probeDistance(mat.selfProbe()[0], mat.probe(imageprobe));
				System.out.println(res);
				//System.out.println(EigenfaceMatrix.probeDistance(mat.probe(imageData), probe2))
				if (res < minres) {
					minres = res;
					mini = i;
					minj = j;
				} else {
					j += 100;
				}
			}
		}
		System.out.println("imageprobe " + minres + " " + mini + " " + minj);
		/*
		 * StringBuilder sb = new StringBuilder(); for (int i : img.getImage())
		 * { sb.append(Integer.toHexString(i));
		 * sb.append(System.lineSeparator()); } System.out.println(sb);
		 */
	}

}
