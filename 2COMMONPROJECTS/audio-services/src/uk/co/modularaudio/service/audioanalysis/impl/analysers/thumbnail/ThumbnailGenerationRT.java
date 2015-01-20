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

package uk.co.modularaudio.service.audioanalysis.impl.analysers.thumbnail;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ThumbnailGenerationRT
{
//	private static Log log = LogFactory.getLog( ThumbnailGenerationRT.class.getName() );
	
	// three values per pixel - min, max, rms
	int numThumbnailValues = -1;
	float[] thumbnailValues = null;
	int outIndex = 0;
	int numChannels = -1;
	long totalNumFloats = -1;
	float stepsPerPixel = 0.0f;
	long currentIndex = 0;
	
	// Current running accumulators
	float minValue = 1.0f;
	float maxValue = -1.0f;
	float sumSq = 0.0f;
	
	// Output image variables
	int requiredWidth = -1;
	int requiredHeight = -1;
	BufferedImage outputImage = null;
	Graphics2D g2d = null;
	Color minMaxColor;
	Color rmsColor;

	public int pixelOutCount = 0;
	
	public ThumbnailGenerationRT( int numChannels,
			long totalNumFloats,
			float stepsPerPixel,
			int requiredHeight,
			Color minMaxColor,
			Color rmsColor )
	{
		this.numChannels = numChannels;
		this.totalNumFloats = totalNumFloats;
		this.stepsPerPixel = stepsPerPixel;
		this.requiredHeight = requiredHeight;
		this.requiredWidth = (int)(totalNumFloats / (numChannels * stepsPerPixel));
//		log.debug("Output image will be " + requiredWidth + " pixels for spp " + stepsPerPixel + " with total num floats " + totalNumFloats +
//				" and numChannels " + numChannels );
		
		// The colors we draw in
		this.minMaxColor = minMaxColor;
		this.rmsColor = rmsColor;
		
		// Initialise the array we will store the values in
		numThumbnailValues = (int)(requiredWidth) * 3;
		thumbnailValues = new float[ numThumbnailValues ];
//		log.debug("Should generate " + numThumbnailValues + " thumbnail values to process");
		
		// Finally create the output image we will write into
		outputImage = new BufferedImage( requiredWidth, requiredHeight, BufferedImage.TYPE_INT_ARGB );
		g2d = outputImage.createGraphics();
	}

	public BufferedImage getBufferedImage()
	{
		return outputImage;
	}
}
