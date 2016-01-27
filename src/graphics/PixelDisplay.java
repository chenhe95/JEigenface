package graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author He
 */
public class PixelDisplay extends JFrame implements Runnable {
	
	public static void displayImage(int[] data, int imageWidth, int imageHeight) {
		SwingUtilities.invokeLater(new PixelDisplay(data, imageWidth, imageHeight));
	}
	
	public static void displayImage(double[] data, int imageWidth, int imageHeight) {
		int[] copy = new int[data.length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = (int) data[i];
		}
		displayImage(copy, imageWidth, imageHeight);
	}
	
	@Override
	public void run() {
		setVisible(true);
	}
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates new form PixelDisplay
	 */
	private PixelDisplay(int[] data, int imageWidth, int imageHeight) {

		pixelPanel = new PixelPanel(data, imageWidth, imageHeight);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(pixelPanel);
		pixelPanel.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, imageWidth + 20, Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, imageHeight + 20, Short.MAX_VALUE));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(pixelPanel,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(pixelPanel,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		setTitle("PixelDisplay " + imageWidth + "x" + imageHeight);
		pack();
	}

	private PixelPanel pixelPanel;

	private static class PixelPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private BufferedImage generatedImage = null;

		private PixelPanel(int[] data, int width, int height) {

			generatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int shadeGray = data[y * width + x] & 0xFF;
					int rgb = shadeGray | (shadeGray << 8) | (shadeGray << 16);
					generatedImage.setRGB(x, y, rgb);
				}
			}
		}

		@Override
		public void paintComponent(Graphics g2) {
			super.paintComponent(g2);
			Graphics2D g = (Graphics2D) g2;
			g.drawImage(generatedImage, 10, 10, null);
		}
	}
}
