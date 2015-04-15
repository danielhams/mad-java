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

import uk.co.modularaudio.util.audio.format.DataRate;

public class ThumbnailGenerator
{
//	private static Log log = LogFactory.getLog( ThumbnailGenerator.class.getName() );

	public ThumbnailGenerator()
	{
	}

	public ThumbnailGenerationRT start( final DataRate dataRate,
			final int numChannels,
			final long totalFloatsLength,
			final float stepsPerPixel,
			final int requiredHeight,
			final Color minMaxColor,
			final Color rmsColor )
	{
		final ThumbnailGenerationRT rt = new ThumbnailGenerationRT( numChannels,
				totalFloatsLength,
				stepsPerPixel,
				requiredHeight,
				minMaxColor,
				rmsColor );
		return rt;
	}

	public void receiveData( final ThumbnailGenerationRT rt, final float[] data, final int numRead )
	{
		for( int i = 0 ; i < numRead ; i += rt.numChannels )
		{
			for( int j = 0 ; j < rt.numChannels ; j++ )
			{
				final float curSample = data[i + j];
				if( curSample > rt.maxValue )
				{
					rt.maxValue = curSample;
				}
				if( curSample < rt.minValue )
				{
					rt.minValue = curSample;
				}
				// The times two just makes the rms more "visible"
				// We only take the first channel for RMS calculations
				if( j == 0 )
				{
//					rt.sumSq = rt.sumSq + ((curSample * curSample) * 2);
					rt.sumSq = rt.sumSq + (curSample * curSample);
				}
			}
			rt.currentIndex += rt.numChannels;

			// Now if we have passed a pixel marker we fill in the thumbnail values with the three cumulative computed values
			// and increment the output index
			final int curIndRatio = (int)(((rt.currentIndex + 0.0f) / rt.numChannels) / (rt.stepsPerPixel + 0.0f));
			if( curIndRatio > rt.pixelOutCount  && rt.outIndex <= (rt.numThumbnailValues - 4))
			{
//				log.debug("Outputting values at current index " + rt.currentIndex + " and rt.outindex " + rt.outIndex + " when ratio is " + curIndRatio );
				rt.thumbnailValues[ rt.outIndex++ ] = rt.minValue;
				rt.thumbnailValues[ rt.outIndex++ ] = rt.maxValue;
				final float midPoint = (rt.maxValue + rt.minValue) / 2;
				final float rmsMidFactor = (midPoint * midPoint) * rt.stepsPerPixel;
				final double normalisedRmsValue = Math.abs((rt.sumSq - rmsMidFactor)) / (double)rt.stepsPerPixel;
				rt.thumbnailValues[ rt.outIndex++ ] = (float)Math.sqrt( normalisedRmsValue );
				rt.sumSq = 0.0f;
				rt.minValue = 1.0f;
				rt.maxValue = -1.0f;
				rt.pixelOutCount++;
			}
		}
	}

	public void end( final ThumbnailGenerationRT rt )
	{
		final int width = rt.requiredWidth;
		final int height = rt.requiredHeight;

		final Graphics2D g2d = (Graphics2D)rt.g2d.create( 0, 0, width, height );

//		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.setColor( Color.BLACK );
		g2d.fillRect( 0, 0, width, height );

		drawRms(rt, width, height, g2d);

		drawMinMax(rt, width, height, g2d);

	}

	private void drawMinMax(final ThumbnailGenerationRT rt, final int width, final int height, final Graphics2D g2d)
	{
		g2d.setColor( rt.minMaxColor );

		int curDataCount = 0;

		for( int i = 0 ; i < width ; i++ )
		{
			final float minSample = rt.thumbnailValues[ curDataCount++ ];
			final float maxSample = rt.thumbnailValues[ curDataCount++ ];
			curDataCount++;
//			float rms = thumbnailValues[ curDataCount++ ];

			// lSample goes from +1.0 to -1.0
			// We need it to go from 0 to height
			final int curX = i;
			final int startY = (int)( ((minSample + 1.0) / 2.0) * height);
			final int endY = (int)( ((maxSample + 1.0)  / 2.0) * height);
			g2d.drawLine( curX, startY, curX, endY );
		}
	}

	private void drawRms(final ThumbnailGenerationRT rt, final int width, final int height, final Graphics2D g2d)
	{
		int curDataCount;
		g2d.setColor( rt.rmsColor );

		curDataCount = 0;
		for( int i = 0 ; i < width ; i++ )
		{
			final float minSample = rt.thumbnailValues[ curDataCount++ ];
			final float maxSample = rt.thumbnailValues[ curDataCount++ ];
			final float mid = (minSample + maxSample ) / 2;
			final float midPoint = ((mid + 1.0f) / 2.0f);
//			curDataCount+=2;
			final float rms = rt.thumbnailValues[ curDataCount++ ];

			// lSample goes from +1.0 to -1.0
			// We need it to go from 0 to height
			final int curX = i;
//			float startRms = ( (rms + 1.0f) / 2.0f);
//			float endRms = ( (-rms + 1.0f)  / 2.0f);
			final int startY = (int)( (midPoint + rms) * (double)height);
			final int endY = (int)( (midPoint - rms) * (double)height);
			g2d.drawLine( curX, startY, curX,  endY );
//			g2d.drawLine( curX, midY, curX,  midY + 1 );
		}
	}

}
