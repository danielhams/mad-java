/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.audio.gui.spectrumgeneration;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class SpectrumAndPeaksGenerator extends SpectrumGenerator
{
	
	public BufferedImage generateSpectrumAndPeaks(
			float[] inputAmps,
			float[] inputPhases,
			int[] peaks,
			int[] binToPeak,
			int width,
			int height )
	{
		return generateSpectrumAndPeaksUsingColours(inputAmps, inputPhases, peaks, binToPeak, width, height, null );
	}
	
	public BufferedImage generateSpectrumAndPeaksUsingColours(
			float[] inputAmps,
			float[] inputPhases,
			int[] peaks,
			int[] binToPeak,
			int width,
			int height,
			Color[] peakColors )
	{
		// Lose 5 pixels in height so we can draw the region indicators
		int specHeight = height - 5;
		if( specHeight < 0 )
		{
			specHeight = 1;
		}
		int indHeight = height - specHeight;
		BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		paintMagsIntoBufferedImageUsingColours( inputAmps, inputPhases, bi, 0, indHeight, width, specHeight, binToPeak, peakColors );

		if( indHeight > 0 )
		{
			paintPeakIndicatorsIntoBufferedImage( peaks, binToPeak, bi, width, indHeight );
		}

		return bi;
	}
	
	private void paintPeakIndicatorsIntoBufferedImage( int[] peaks, int[] binToPeak, BufferedImage outputImage, int width, int height )
	{
		Graphics2D g2d = outputImage.createGraphics();
		
		int numValuesToDraw = getNumValuesToDraw();
		numValuesToDraw = (numValuesToDraw > binToPeak.length ? binToPeak.length : numValuesToDraw );

		int outputImageWidth = outputImage.getWidth();
		int outputImageHeight = outputImage.getHeight();

		int currentPeakNum = -1;
		
		for( int i = 0 ; i < outputImageWidth; i++ )
		{
			int actualVal = (outputImageWidth - 1) - i;

			int logBinIndex = freqScaleComputer.displayBinToSpectraBin( numValuesToDraw, outputImageWidth, actualVal );

			int pixelXStartIndex = i;

			int curVal = -1;
			if( logBinIndex < peaks.length )
			{
				curVal = binToPeak[ logBinIndex ];
			}
			
			int halfLineHeight = -1;
			
			if( curVal != currentPeakNum )
			{
				// We've hit a boundary, draw a half height line
				halfLineHeight = height / 4;
				g2d.setColor( Color.red );
				currentPeakNum = curVal;
			}
			else if( curVal == logBinIndex )
			{
				// It's the peak itself, draw a full height line
				halfLineHeight = height / 2;
				g2d.setColor( Color.green );
			}
			else if( curVal != -1 )
			{
				// Just draw a dot 
				halfLineHeight = 1;
				g2d.setColor( Color.white );
			}
			else
			{
				// Leave empty, not marked as a peak
				halfLineHeight = -1;
			}
			
			if( halfLineHeight > 0 )
			{
				
				int xIndex = pixelXStartIndex;
//				int yMiddle = height - 2;
				int yStart = 0;
				int yEnd = yStart + (halfLineHeight * 2);
			
				int x1 = outputImageWidth - xIndex;
				g2d.drawLine( x1, outputImageHeight - yStart, x1, outputImageHeight - yEnd );
			}
		}	
	}


}
