package util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import jef.EigenfaceMatrix;
import jef.GrayImage;

public class JEUtils {

	private static final double[] LUMINOSITY_WEIGHTS = { 0.21, 0.72, 0.07 };
	private static final int DECIMAL_PLACES = 3;

	public static GrayImage loadTrainingImage(String path) {
		GrayImage gImage = null;
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
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
				int resultPixels[] = new int[rawPixels.length / pixLength];
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
		//GrayImage img = loadTrainingImage("test/004.png");
		ArrayList<GrayImage> trainingSet = new ArrayList<>();
		for (int i = 1; i < 6; ++i) {
			trainingSet.add(loadTrainingImage("test/00" + i + ".png"));
		}
		EigenfaceMatrix.importData(trainingSet);
		
		/*StringBuilder sb = new StringBuilder();
		for (int i : img.getImage()) {
			sb.append(Integer.toHexString(i));
			sb.append(System.lineSeparator());
		}
		System.out.println(sb);*/
	}
	
	
}