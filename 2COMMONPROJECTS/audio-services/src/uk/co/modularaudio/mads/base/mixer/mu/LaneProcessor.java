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

public class LaneProcessor
{
//	private static final Log log = LogFactory.getLog( LaneProcessor.class.getName() );

	private MixerMadInstance instance = null;
	
	private float curValueRatio = 0.0f;
	private float newValueRatio = 0.0f;
	
	private int numChannelsPerLane = -1;
	
	private int laneNumber = -1;
	private int[] inputChannelIndexes = null;
	private int[] outputChannelIndexes = null;
	
	private float desiredAmpMultiplier = 0.5f;
	
	private float desiredLeftAmpMultiplier = 0.5f;
	private float currentLeftAmpMultiplier = 0.5f;
	private float desiredRightAmpMultiplier = 0.5f;
	private float currentRightAmpMultiplier = 0.5f;
	
	private float desiredPanValue = 0.0f;
	
	private boolean desiredActive = true;

	private float currentLeftMeterReading = 0.0f;
	private float currentRightMeterReading = 0.0f;

	public LaneProcessor( MixerMadInstance instance,
			MixerMadInstanceConfiguration channelConfiguration,
			int laneNumber,
			float curValueRatio,
			float newValueRatio )
	{
		this.instance = instance;

		this.curValueRatio = curValueRatio;
		this.newValueRatio = newValueRatio;
		this.laneNumber = laneNumber;
		
		numChannelsPerLane = channelConfiguration.getNumChannelsPerLane();
		inputChannelIndexes = new int[ numChannelsPerLane ];
		outputChannelIndexes = new int[ numChannelsPerLane ];
		
		for( int cn = 0 ; cn < numChannelsPerLane ; cn++ )
		{
			inputChannelIndexes[ cn ] = channelConfiguration.getIndexForInputLaneChannel( laneNumber, cn );
			outputChannelIndexes[ cn ] = channelConfiguration.getIndexForOutputChannel( cn );
		}
	}

	public void processLaneMixToOutput( ThreadSpecificTemporaryEventStorage tses,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers,
			int position,
			int length )
	{
		// Only process if something is connected to us!
		boolean inputConnected = false;
		for( int cn = 0 ; !inputConnected && cn < numChannelsPerLane ; cn++ )
		{
			int indexOfChannel = inputChannelIndexes[ cn ];
			if( channelConnectedFlags.get( indexOfChannel ) )
			{
				inputConnected = true;
				break;
			}
		}
		
		float[] firstOutputChannel = channelBuffers[ outputChannelIndexes[ 0 ] ].floatBuffer;
		float[] secondOutputChannel = channelBuffers[ outputChannelIndexes[ 1 ] ].floatBuffer;
		
		if( inputConnected )
		{
			boolean firstConnected = channelConnectedFlags.get( inputChannelIndexes[0] );
			float[] firstInputChannel = channelBuffers[ inputChannelIndexes[ 0 ] ].floatBuffer;
			boolean secondConnected = channelConnectedFlags.get( inputChannelIndexes[1] );
			float[] secondInputChannel = channelBuffers[ inputChannelIndexes[ 1 ] ].floatBuffer;
			
			if( firstConnected )
			{
				if( currentLeftAmpMultiplier < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
				{
					currentLeftAmpMultiplier = 0.0f;
				}
				
				if( currentLeftAmpMultiplier == 0.0f && desiredLeftAmpMultiplier == 0.0f )
				{
				}
				else
				{
					for( int s = position ; s < position + length ; s++ )
					{
						currentLeftAmpMultiplier = ( currentLeftAmpMultiplier * curValueRatio ) + ( desiredLeftAmpMultiplier * newValueRatio );
						float oneFloat = firstInputChannel[s] * currentLeftAmpMultiplier;
						float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );
						
						if( absFloat < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
						{
							oneFloat = 0.0f;
						}
						else if( absFloat > currentLeftMeterReading )
						{
							currentLeftMeterReading = absFloat;
						}
						firstOutputChannel[ s ] += oneFloat;
						
						if( !secondConnected )
						{
							secondOutputChannel[ s ] += oneFloat;
						}
					}
				}
			}
			
			if( secondConnected )
			{
				if( currentRightAmpMultiplier < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
				{
					currentRightAmpMultiplier = 0.0f;
				}

				if( currentRightAmpMultiplier == 0.0f && desiredRightAmpMultiplier == 0.0f )
				{
				}
				else
				{
					for( int s = position ; s < position + length ; s++ )
					{
						currentRightAmpMultiplier = ( currentRightAmpMultiplier * curValueRatio ) + ( desiredRightAmpMultiplier * newValueRatio );
						float oneFloat = secondInputChannel[s] * currentRightAmpMultiplier;
						float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );
						
						if( absFloat < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
						{
							oneFloat = 0.0f;
						}
						else if( absFloat > currentRightMeterReading )
						{
							currentRightMeterReading = absFloat;
						}
						secondOutputChannel[ s ] += oneFloat;
					}
				}
			}
		}
		else
		{
			currentLeftMeterReading = 0.0f;
			currentRightMeterReading = 0.0f;
		}
	}
	
	public void emitLaneMeterReadings( ThreadSpecificTemporaryEventStorage tses, long meterTimestamp )
	{
//		log.debug("Emitting one at " + meterTimestamp);
		instance.emitLaneMeterReading( tses, meterTimestamp, laneNumber, currentLeftMeterReading, currentRightMeterReading );
		currentLeftMeterReading = 0.0f;
		currentRightMeterReading = 0.0f;
	}

	public void setLaneAmp( float ampValue )
	{
//		log.debug("Setting lane " + laneNumber + " to amp " + ampValue );
		desiredAmpMultiplier = ampValue;		
		recomputeDesiredChannelAmps();
	}

	public void resetCurNewValues( float curValueRatio2, float newValueRatio2 )
	{
		this.curValueRatio = curValueRatio2;
		this.newValueRatio = newValueRatio2;
	}

	public void setLanePan( float panValue )
	{
		desiredPanValue = panValue;
		recomputeDesiredChannelAmps();
	}
	
	public void setLaneActive( boolean active )
	{
		desiredActive = active;
		recomputeDesiredChannelAmps();
	}
	
	private void recomputeDesiredChannelAmps()
	{
		if( desiredActive )
		{
			float leftAmp = (desiredPanValue < 0.0f ? 1.0f : (1.0f - desiredPanValue ) );
			leftAmp = NormalisedValuesMapper.expMapF( leftAmp );
			desiredLeftAmpMultiplier = desiredAmpMultiplier * leftAmp;
			float rightAmp = (desiredPanValue > 0.0f ? 1.0f : (1.0f + desiredPanValue) );
			rightAmp = NormalisedValuesMapper.expMapF( rightAmp );
			desiredRightAmpMultiplier = desiredAmpMultiplier * rightAmp;
			
			if( desiredLeftAmpMultiplier < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				desiredLeftAmpMultiplier = 0.0f;
			}
			if( desiredRightAmpMultiplier < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				desiredRightAmpMultiplier = 0.0f;
			}
		}
		else
		{
			desiredLeftAmpMultiplier = 0.0f;
			desiredRightAmpMultiplier = 0.0f;
		}
	}
}
