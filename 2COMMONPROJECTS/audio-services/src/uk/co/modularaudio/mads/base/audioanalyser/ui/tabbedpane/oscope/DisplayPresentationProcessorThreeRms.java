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

public abstract class DisplayPresentationProcessorThreeRms implements
		DisplayPresentationProcessor
{
//	private static Log log = LogFactory.getLog( DisplayPresentationProcessorThreeRms.class.getName() );
	
	private final float displayHeightMultiplier;

	private float lowValue;
	private float midValue;
	private float highValue;
	
	private AudioAnalyserDataBuffers dataBuffers;
	
	private final RmsDataBuffers additionalBuffers;
	
	public DisplayPresentationProcessorThreeRms( AudioAnalyserUiBufferState uiBufferState, int displayWidth, int displayHeight, RmsDataBuffers rmsDataBuffers )
	{
		this.dataBuffers = uiBufferState.getDataBuffers();

		this.displayHeightMultiplier = (displayHeight / 2.0f );
		
		additionalBuffers = rmsDataBuffers;

		resetMinMax();
	}
	
	@Override
	public void doPreIndex( int preIndexPos, int numSamplesPerPixel )
	{
		resetMinMax();
		calcMinMaxForSamples( preIndexPos, numSamplesPerPixel );
	}

	@Override
	public void presentPixel( int indexInt, int numSamplesPerPixel, Graphics2D g, int pixelOffset )
	{
		resetMinMax();
		calcMinMaxForSamples( indexInt, numSamplesPerPixel );
		boolean doLinear = false;
		if( doLinear )
		{
			fillInMinMaxLineLinear( g, pixelOffset );
		}
		else
		{
			fillInMinMaxLineDb( g, pixelOffset );
		}
	}

	private void resetMinMax()
	{
		lowValue = -Float.MAX_VALUE;
		midValue = lowValue;
		highValue = lowValue;
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
			float lowS = additionalBuffers.lowRmsBuffer[ sIndex ];
			if( lowS > lowValue )
			{
				lowValue = lowS;
			}
			float midS = additionalBuffers.midRmsBuffer[ sIndex ];
			if( midS > midValue )
			{
				midValue = midS;
			}
			float highS = additionalBuffers.hiRmsBuffer[ sIndex ];
			if( highS > highValue )
			{
				highValue = highS;
			}
		}
		for( int sp = 0 ; sp < numFromStart ; ++sp )
		{
			float lowS = additionalBuffers.lowRmsBuffer[ sp ];
			if( lowS > lowValue )
			{
				lowValue = lowS;
			}
			float midS = additionalBuffers.midRmsBuffer[ sp ];
			if( midS > midValue )
			{
				midValue = midS;
			}
			float highS = additionalBuffers.hiRmsBuffer[ sp ];
			if( highS > highValue )
			{
				highValue = highS;
			}
		}
	}
	
	private float linToLog( float inValue, float lowerDbBound )
	{
		float logValue = 20.0f * (float)Math.log10( inValue );
		
		float lowAdjustedVal = (Float.isInfinite(logValue) ? -lowerDbBound : logValue );
		lowAdjustedVal = (lowerDbBound + lowAdjustedVal) / lowerDbBound;
		lowAdjustedVal = (lowAdjustedVal < 0.0f ? 0.0f : lowAdjustedVal );
		return lowAdjustedVal;
	}
	
	private void fillInMinMaxLineDb( Graphics2D g, int pixelX )
	{
//		float lowAdjustedVal = linToLog( lowValue, 40.0f );
		float lowAdjustedVal = linToLog( lowValue, 50.0f );
//		float lowAdjustedVal = linToLog( lowValue, 96.0f );
		
		int yLowMinVal = (int)((lowAdjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );
		int yLowMaxVal = (int)((-lowAdjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );

		g.setColor( AAColours.WAVEDISPLAY_LOW_FILL );
		g.drawLine( pixelX,  yLowMinVal,  pixelX,  yLowMaxVal);
		
//		float midAdjustedVal = linToLog( midValue, 40.0f );
		float midAdjustedVal = linToLog( midValue, 50.0f );
//		float midAdjustedVal = linToLog( midValue, 96.0f );
		midAdjustedVal *= 0.75f;

		int yMidMinVal = (int)((midAdjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );
		int yMidMaxVal = (int)((-midAdjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );

		g.setColor( AAColours.WAVEDISPLAY_MED_FILL );
		g.drawLine( pixelX,  yMidMinVal,  pixelX,  yMidMaxVal);
		
//		float hiAdjustedVal = linToLog( highValue, 55.0f );
		float hiAdjustedVal = linToLog( highValue, 65.0f );
//		float hiAdjustedVal = linToLog( highValue, 96.0f );
		hiAdjustedVal *= 0.5f;
		
		int yHiMinVal = (int)((hiAdjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );
		int yHiMaxVal = (int)((-hiAdjustedVal * displayHeightMultiplier ) + displayHeightMultiplier );

		g.setColor( AAColours.WAVEDISPLAY_HI_FILL );
		g.drawLine( pixelX,  yHiMinVal,  pixelX,  yHiMaxVal);
	}

	private void fillInMinMaxLineLinear( Graphics2D g, int pixelX )
	{
		float globalMultiplier = 1.56f;
		float lowMultiplier = 1.0f * globalMultiplier;
		float medMultiplier = 0.9f * globalMultiplier;
		float hiMultiplier = 0.85f * globalMultiplier;
		
		float lv = lowValue * lowMultiplier;
				
		int yLowMinVal = (int)((lv * displayHeightMultiplier ) + displayHeightMultiplier );
		int yLowMaxVal = (int)((-lv * displayHeightMultiplier ) + displayHeightMultiplier );

		g.setColor( AAColours.WAVEDISPLAY_LOW_FILL );
		g.drawLine( pixelX,  yLowMinVal,  pixelX,  yLowMaxVal);
		
		float mv = midValue * medMultiplier;

		int yMidMinVal = (int)((mv * displayHeightMultiplier ) + displayHeightMultiplier );
		int yMidMaxVal = (int)((-mv * displayHeightMultiplier ) + displayHeightMultiplier );

		g.setColor( AAColours.WAVEDISPLAY_MED_FILL );
		g.drawLine( pixelX,  yMidMinVal,  pixelX,  yMidMaxVal);
		
		float hv = highValue * hiMultiplier;

		int yHighMinVal = (int)((hv * displayHeightMultiplier ) + displayHeightMultiplier );
		int yHighMaxVal = (int)((-hv * displayHeightMultiplier ) + displayHeightMultiplier );

		g.setColor( AAColours.WAVEDISPLAY_HI_FILL );
		g.drawLine( pixelX,  yHighMinVal,  pixelX,  yHighMaxVal);
	}

	@Override
	public AdditionalDataBuffers getAdditionalDataBuffers()
	{
		return additionalBuffers;
	}

	@Override
	public void resetForSwitch()
	{
		additionalBuffers.reset();
	}
}
