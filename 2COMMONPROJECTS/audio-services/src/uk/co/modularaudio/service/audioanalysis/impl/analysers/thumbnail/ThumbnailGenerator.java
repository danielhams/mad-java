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
	
	public ThumbnailGenerationRT start( DataRate dataRate,
			int numChannels,
			long totalFloatsLength,
			float stepsPerPixel,
			int requiredHeight,
			Color minMaxColor,
			Color rmsColor )
	{
		ThumbnailGenerationRT rt = new ThumbnailGenerationRT( numChannels,
				totalFloatsLength,
				stepsPerPixel,
				requiredHeight,
				minMaxColor,
				rmsColor );
		return rt;
	}
	
	public void receiveData( ThumbnailGenerationRT rt, float[] data, int numRead )
	{
		for( int i = 0 ; i < numRead ; i += rt.numChannels )
		{
			for( int j = 0 ; j < rt.numChannels ; j++ )
			{
				float curSample = data[i + j];
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
			int curIndRatio = (int)(((rt.currentIndex + 0.0f) / rt.numChannels) / (rt.stepsPerPixel + 0.0f));
			if( curIndRatio > rt.pixelOutCount  && rt.outIndex <= (rt.numThumbnailValues - 4))
			{
//				log.debug("Outputting values at current index " + rt.currentIndex + " and rt.outindex " + rt.outIndex + " when ratio is " + curIndRatio );
				rt.thumbnailValues[ rt.outIndex++ ] = rt.minValue;
				rt.thumbnailValues[ rt.outIndex++ ] = rt.maxValue;
				float midPoint = (rt.maxValue + rt.minValue) / 2;
				float rmsMidFactor = (midPoint * midPoint) * rt.stepsPerPixel;
				double normalisedRmsValue = Math.abs((rt.sumSq - rmsMidFactor)) / (double)rt.stepsPerPixel;
				rt.thumbnailValues[ rt.outIndex++ ] = (float)Math.sqrt( (double)normalisedRmsValue );
				rt.sumSq = 0.0f;
				rt.minValue = 1.0f;
				rt.maxValue = -1.0f;
				rt.pixelOutCount++;
			}
		}
	}
	
	public void end( ThumbnailGenerationRT rt )
	{
		int width = rt.requiredWidth;
		int height = rt.requiredHeight;
		
		Graphics2D g2d = (Graphics2D)rt.g2d.create( 0, 0, width, height );

//		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.setColor( Color.BLACK );
		g2d.fillRect( 0, 0, width, height );
		
		drawRms(rt, width, height, g2d);

		drawMinMax(rt, width, height, g2d);
		
	}

	private void drawMinMax(ThumbnailGenerationRT rt, int width, int height, Graphics2D g2d)
	{
		g2d.setColor( rt.minMaxColor );

		int curDataCount = 0;
		
		for( int i = 0 ; i < width ; i++ )
		{
			float minSample = rt.thumbnailValues[ curDataCount++ ];
			float maxSample = rt.thumbnailValues[ curDataCount++ ];
			curDataCount++;
//			float rms = thumbnailValues[ curDataCount++ ];

			// lSample goes from +1.0 to -1.0
			// We need it to go from 0 to height
			int curX = i;
			int startY = (int)( ((minSample + 1.0) / 2.0) * (double)height);
			int endY = (int)( ((maxSample + 1.0)  / 2.0) * (double)height);
			g2d.drawLine( curX, startY, curX, endY );
		}
	}

	private void drawRms(ThumbnailGenerationRT rt, int width, int height, Graphics2D g2d)
	{
		int curDataCount;
		g2d.setColor( rt.rmsColor );
		
		curDataCount = 0;
		for( int i = 0 ; i < width ; i++ )
		{
			float minSample = rt.thumbnailValues[ curDataCount++ ];
			float maxSample = rt.thumbnailValues[ curDataCount++ ];
			float mid = (minSample + maxSample ) / 2;
			float midPoint = ((mid + 1.0f) / 2.0f);
//			curDataCount+=2;
			float rms = rt.thumbnailValues[ curDataCount++ ];

			// lSample goes from +1.0 to -1.0
			// We need it to go from 0 to height
			int curX = i;
//			float startRms = ( (rms + 1.0f) / 2.0f);
//			float endRms = ( (-rms + 1.0f)  / 2.0f);
			int startY = (int)( (midPoint + rms) * (double)height);
			int endY = (int)( (midPoint - rms) * (double)height);
			g2d.drawLine( curX, startY, curX,  endY );
//			g2d.drawLine( curX, midY, curX,  midY + 1 );
		}
	}

}
