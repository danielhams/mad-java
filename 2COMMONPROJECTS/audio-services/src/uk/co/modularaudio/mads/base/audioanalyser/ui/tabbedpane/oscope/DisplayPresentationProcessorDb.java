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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import java.awt.Graphics2D;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AdditionalDataBuffers;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserDataBuffers;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AAColours;

public class DisplayPresentationProcessorDb implements
		DisplayPresentationProcessor
{
//	private static Log log = LogFactory.getLog( DisplayPresentationProcessorDb.class.getName() );
	
	private final float displayHeightMultiplier;

	private float minValue;
	private float maxValue;
	private float previousMinValue;
	private float previousMaxValue;
	
	private AudioAnalyserDataBuffers dataBuffers;
	
	public DisplayPresentationProcessorDb( AudioAnalyserUiBufferState uiBufferState, int displayWidth, int displayHeight )
	{
		this.dataBuffers = uiBufferState.getDataBuffers();

		this.displayHeightMultiplier = (displayHeight / 2.0f );

		resetAll();
	}
	
	@Override
	public void doPreIndex( int preIndexPos, int numSamplesPerPixel )
	{
		resetAll();
		calcMinMaxForSamples( preIndexPos, numSamplesPerPixel );
	}

	@Override
	public void presentPixel( int indexInt, int numSamplesPerPixel, Graphics2D g, int pixelOffset )
	{
		moveToNext();
		resetMinMax();
		calcMinMaxForSamples( indexInt, numSamplesPerPixel );
		extendWithPrevious();
		fillInMinMaxLine( g, pixelOffset );
	}

	private void resetAll()
	{
		resetMinMax();
		resetPrevious();
	}

	private void resetMinMax()
	{
		minValue = Float.MAX_VALUE;
		maxValue = -minValue;
	}

	private void resetPrevious()
	{
		previousMinValue = 0.0f;
		previousMaxValue = 0.0f;
	}

	private void moveToNext()
	{
		previousMinValue = minValue;
		previousMaxValue = maxValue;
	}

	private void extendWithPrevious()
	{
		if( previousMaxValue < minValue )
		{
			minValue = previousMaxValue;
		}
		if( previousMinValue > maxValue )
		{
			maxValue = previousMinValue;
		}
	}

	private void calcMinMaxForSamples( int sampleStartIndex, int numSamplesPerPixel )
	{
		resetMinMax();
		int endIndex = sampleStartIndex + numSamplesPerPixel;
		if( numSamplesPerPixel < 1 )
		{
			endIndex = sampleStartIndex + 1;
		}

		int numFromPos, numFromStart;
		if( endIndex >= dataBuffers.bufferLength )
		{
			endIndex -= dataBuffers.bufferLength;
			numFromPos = (dataBuffers.bufferLength - 1) - sampleStartIndex;
			numFromStart = endIndex;
//			log.debug( numFromPos + " from pos, " + numFromStart + " from start");
		}
		else
		{
			numFromPos = numSamplesPerPixel;
			numFromStart = 0;
//			log.debug("None from start");
		}
//		log.debug("Doing " + numFromPos + " from pos and " + numFromStart  + " from start");
		
		for( int fp = 0 ; fp < numFromPos ; ++fp )
		{
			int sIndex = sampleStartIndex + fp;
			float val = dataBuffers.buffer[ sIndex ];
			if( val > maxValue )
			{
				maxValue = val;
			}
			if( val < minValue )
			{
				minValue = val;
			}
		}
		for( int sp = 0 ; sp < numFromStart ; ++sp )
		{
			float val = dataBuffers.buffer[ sp ];
			if( val > maxValue )
			{
				maxValue = val;
			}
			if( val < minValue )
			{
				minValue = val;
			}
		}
	}
	
	private void fillInMinMaxLine( Graphics2D g, int pixelX )
	{
		float lowerDbBound = 50.0f;
	
		float minAbsVal = Math.abs( minValue );
		float maxAbsVal = Math.abs( maxValue );
		
		float absVal = (minAbsVal > maxAbsVal ? minAbsVal : maxAbsVal );
		
		float logVal = 20.0f * (float)Math.log10( absVal );
		
		float adjustedVal = (Float.isInfinite(logVal) ? -lowerDbBound : (lowerDbBound + logVal) / lowerDbBound);
		adjustedVal = (adjustedVal < 0.0f ? 0.0f : adjustedVal );
		
		int yMinVal = (int)((adjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );
		int yMaxVal = (int)((-adjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );

		g.setColor( AAColours.WAVEDISPLAY_HI_FILL );
		g.drawLine( pixelX,  yMinVal,  pixelX,  yMaxVal);
	}

	@Override
	public AdditionalDataBuffers getAdditionalDataBuffers()
	{
		// There are none
		return null;
	}

	@Override
	public void resetForSwitch()
	{
	}
}
