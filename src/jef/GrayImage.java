package jef;

public class GrayImage {
	
	private static final int IMAGE_WIDTH = 320;
	private static final int IMAGE_HEIGHT = 320;
	
	private int[] gImage = null;
	private int width = 0;
	private int height = 0;
	
	public GrayImage(int[] imageData, int width, int height) {
		gImage = imageData;
		this.width = width;
		this.height = height;
	}
	
	public GrayImage(int[][] imageData) {
		if (imageData == null || imageData[0] == null) {
			throw new IllegalArgumentException("GrayImage(): imageData is null in constructor");
		}
		int sublength = imageData[0].length;
		gImage = new int[imageData.length * sublength];
		for (int i = 0; i < imageData.length; ++i) {
			if (imageData[i] == null || imageData[i].length != sublength) {
				throw new IllegalArgumentException("GrayImage(): all sub-arrays must be non-null and of equal size");
			}
			for (int j = 0; j < sublength; ++j) {
				gImage[i * sublength + j] = (byte) imageData[i][j];
			}
		}
		height = imageData.length;
		width = imageData[0].length;
	}
	
	public int[] getImage() {
		return gImage;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public GrayImage(String filePath) {
		
	}
}
