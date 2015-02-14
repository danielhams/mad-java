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

package uk.co.modularaudio.mads.base.mixern.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class LaneProcessor<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D, I>>
{
//	private static final Log log = LogFactory.getLog( LaneProcessor.class.getName() );

	private final I instance;

	private float curValueRatio;
	private float newValueRatio;

	private final int numChannelsPerLane;

	private final int laneNumber;
	private final int[] inputChannelIndexes;
	private final int[] outputChannelIndexes;

	private float desiredAmpMultiplier = 0.5f;

	private float desiredLeftAmpMultiplier = 0.5f;
	private float currentLeftAmpMultiplier = 0.5f;
	private float desiredRightAmpMultiplier = 0.5f;
	private float currentRightAmpMultiplier = 0.5f;

	private float desiredPanValue;

	private boolean desiredActive = true;

	private float currentLeftMeterReading;
	private float currentRightMeterReading;

	public LaneProcessor( final I instance,
			final MixerNInstanceConfiguration channelConfiguration,
			final int laneNumber,
			final float curValueRatio,
			final float newValueRatio )
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

	public void processLaneMixToOutput( final ThreadSpecificTemporaryEventStorage tses,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int position,
			final int length )
	{
		// Only process if something is connected to us!
		boolean inputConnected = false;
		for( int cn = 0 ; !inputConnected && cn < numChannelsPerLane ; cn++ )
		{
			final int indexOfChannel = inputChannelIndexes[ cn ];
			if( channelConnectedFlags.get( indexOfChannel ) )
			{
				inputConnected = true;
				break;
			}
		}

		final float[] firstOutputChannel = channelBuffers[ outputChannelIndexes[ 0 ] ].floatBuffer;
		final float[] secondOutputChannel = channelBuffers[ outputChannelIndexes[ 1 ] ].floatBuffer;

		if( inputConnected )
		{
			final boolean firstConnected = channelConnectedFlags.get( inputChannelIndexes[0] );
			final float[] firstInputChannel = channelBuffers[ inputChannelIndexes[ 0 ] ].floatBuffer;
			final boolean secondConnected = channelConnectedFlags.get( inputChannelIndexes[1] );
			final float[] secondInputChannel = channelBuffers[ inputChannelIndexes[ 1 ] ].floatBuffer;

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
						final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

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
						final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

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

	public void emitLaneMeterReadings( final ThreadSpecificTemporaryEventStorage tses, final long meterTimestamp )
	{
//		log.debug("Emitting one at " + meterTimestamp);
		instance.emitLaneMeterReading( tses, meterTimestamp, laneNumber, currentLeftMeterReading, currentRightMeterReading );
		currentLeftMeterReading = 0.0f;
		currentRightMeterReading = 0.0f;
	}

	public void setLaneAmp( final float ampValue )
	{
//		log.debug("Setting lane " + laneNumber + " to amp " + ampValue );
		desiredAmpMultiplier = ampValue;
		recomputeDesiredChannelAmps();
	}

	public void resetCurNewValues( final float curValueRatio2, final float newValueRatio2 )
	{
		this.curValueRatio = curValueRatio2;
		this.newValueRatio = newValueRatio2;
	}

	public void setLanePan( final float panValue )
	{
		desiredPanValue = panValue;
		recomputeDesiredChannelAmps();
	}

	public void setLaneActive( final boolean active )
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
