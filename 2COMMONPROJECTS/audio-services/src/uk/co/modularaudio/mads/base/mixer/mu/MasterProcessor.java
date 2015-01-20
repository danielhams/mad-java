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

package uk.co.modularaudio.mads.base.mixer.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class MasterProcessor
{
//	private static final Log log = LogFactory.getLog( MasterProcessor.class.getName() );
	
	private MixerMadInstance instance = null;
	
	private float curValueRatio = 0.0f;
	private float newValueRatio = 0.0f;
	
	private int numChannelsPerLane = -1;
	
	private int[] outputChannelIndexes = null;
	
	private float desiredAmpMultiplier = 0.5f;
	
	private float desiredLeftAmpMultiplier = 0.5f;
	private float currentLeftAmpMultiplier = 0.5f;
	private float desiredRightAmpMultiplier = 0.5f;
	private float currentRightAmpMultiplier = 0.5f;
	
	private float desiredPanValue = 0.0f;
	
	private float leftMeterLevel = 0.0f;
	private float rightMeterLevel = 0.0f;
	
	public MasterProcessor( MixerMadInstance instance,
			MixerMadInstanceConfiguration channelConfiguration,
			float curValueRatio,
			float newValueRatio )
	{
		this.instance = instance;

		this.curValueRatio = curValueRatio;
		this.newValueRatio = newValueRatio;
		
		numChannelsPerLane = channelConfiguration.getNumChannelsPerLane();
		outputChannelIndexes = new int[ numChannelsPerLane ];
		
		for( int cn = 0 ; cn < numChannelsPerLane ; cn++ )
		{
			outputChannelIndexes[ cn ] = channelConfiguration.getIndexForOutputChannel( cn );
		}
	}

	public void processMasterOutput( ThreadSpecificTemporaryEventStorage tses,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers,
			int position,
			int length )
	{
		for( int s = position ; s < position + length ; s++ )
		{
			for( int cn = 0 ; cn < numChannelsPerLane ; cn++ )
			{
				if( cn == 0 )
				{
					currentLeftAmpMultiplier = (currentLeftAmpMultiplier * curValueRatio ) + (desiredLeftAmpMultiplier * newValueRatio );
				}
				else if( cn == 1 )
				{
					currentRightAmpMultiplier = (currentRightAmpMultiplier * curValueRatio ) + (desiredRightAmpMultiplier * newValueRatio );
				}
				int outputChannelIndex = outputChannelIndexes[ cn ];
				float[] outputFloats = channelBuffers[ outputChannelIndex ].floatBuffer;
				float outputFloat = outputFloats[ s ] * ( cn == 0 ? currentLeftAmpMultiplier : currentRightAmpMultiplier );
				float absFloat = (outputFloat < 0.0f ? -outputFloat : outputFloat );

				if( absFloat < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
				{
					outputFloat = 0.0f;
				}

				if( cn == 0 )
				{
					if( absFloat > leftMeterLevel )
					{
						leftMeterLevel = absFloat;
					}
				}
				else if( cn == 1 )
				{
					if( absFloat > rightMeterLevel )
					{
						rightMeterLevel = absFloat;
					}
				}
				outputFloats[ s ] = outputFloat;
			}
		}
	}
	
	public void emitMasterMeterReadings( ThreadSpecificTemporaryEventStorage tses, long emitTimestamp )
	{
//			log.debug("Emitting one at " + emitTimestamp);
		instance.emitMasterMeterReading( tses, emitTimestamp, leftMeterLevel, rightMeterLevel );
		leftMeterLevel = 0.0f;
		rightMeterLevel = 0.0f;
	}

	public void setMasterAmp( float ampValue )
	{
		desiredAmpMultiplier = ampValue;
		recomputeDesiredChannelAmps();
	}

	public void resetCurNewValues( float curValueRatio2, float newValueRatio2 )
	{
		this.curValueRatio = curValueRatio2;
		this.newValueRatio = newValueRatio2;
	}

	public void setMasterPan( float panValue )
	{
		desiredPanValue = panValue;
		recomputeDesiredChannelAmps();		
	}
	
	private void recomputeDesiredChannelAmps()
	{
		float leftAmp = (desiredPanValue < 0.0f ? 1.0f : (1.0f - desiredPanValue ) );
		leftAmp = NormalisedValuesMapper.expMapF( leftAmp );
		desiredLeftAmpMultiplier = desiredAmpMultiplier * leftAmp;
		float rightAmp = (desiredPanValue > 0.0f ? 1.0f : (1.0f + desiredPanValue) );
		rightAmp = NormalisedValuesMapper.expMapF( rightAmp );
		desiredRightAmpMultiplier = desiredAmpMultiplier * rightAmp;
	}
}
