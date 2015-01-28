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
			final float[] inputAmps,
			final float[] inputPhases,
			final int[] peaks,
			final int[] binToPeak,
			final int width,
			final int height )
	{
		return generateSpectrumAndPeaksUsingColours(inputAmps, inputPhases, peaks, binToPeak, width, height, null );
	}

	public BufferedImage generateSpectrumAndPeaksUsingColours(
			final float[] inputAmps,
			final float[] inputPhases,
			final int[] peaks,
			final int[] binToPeak,
			final int width,
			final int height,
			final Color[] peakColors )
	{
		// Lose 5 pixels in height so we can draw the region indicators
		int specHeight = height - 5;
		if( specHeight < 0 )
		{
			specHeight = 1;
		}
		final int indHeight = height - specHeight;
		final BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		paintMagsIntoBufferedImageUsingColours( inputAmps, inputPhases, bi, 0, indHeight, width, specHeight, binToPeak, peakColors );

		if( indHeight > 0 )
		{
			paintPeakIndicatorsIntoBufferedImage( peaks, binToPeak, bi, width, indHeight );
		}

		return bi;
	}

	private void paintPeakIndicatorsIntoBufferedImage( final int[] peaks, final int[] binToPeak, final BufferedImage outputImage, final int width, final int height )
	{
		final Graphics2D g2d = outputImage.createGraphics();

		int numValuesToDraw = getNumValuesToDraw();
		numValuesToDraw = (numValuesToDraw > binToPeak.length ? binToPeak.length : numValuesToDraw );

		final int outputImageWidth = outputImage.getWidth();
		final int outputImageHeight = outputImage.getHeight();

		int currentPeakNum = -1;

		for( int i = 0 ; i < outputImageWidth; i++ )
		{
			final int actualVal = (outputImageWidth - 1) - i;

			final int logBinIndex = freqScaleComputer.displayBinToSpectraBin( numValuesToDraw, outputImageWidth, actualVal );

			final int pixelXStartIndex = i;

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

				final int xIndex = pixelXStartIndex;
//				int yMiddle = height - 2;
				final int yStart = 0;
				final int yEnd = yStart + (halfLineHeight * 2);

				final int x1 = outputImageWidth - xIndex;
				g2d.drawLine( x1, outputImageHeight - yStart, x1, outputImageHeight - yEnd );
			}
		}
	}


}
