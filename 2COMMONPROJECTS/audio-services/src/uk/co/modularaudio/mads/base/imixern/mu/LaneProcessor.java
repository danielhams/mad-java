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

package uk.co.modularaudio.mads.base.imixern.mu;

import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.math.MixdownSliderDbToLevelComputer;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class LaneProcessor<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D, I>>
{
//	private static final Log log = LogFactory.getLog( LaneProcessor.class.getName() );

	private final I instance;

	private final int laneNumber;

	private final int leftIndex;
	private final int rightIndex;

	private float desiredAmpMultiplier = 0.0f;

	private float desiredLeftAmpMultiplier = 0.0f;
	private float desiredRightAmpMultiplier = 0.0f;

	private float desiredPanValue;

	private boolean desiredActive = true;

	private float currentLeftMeterReading;
	private float currentRightMeterReading;

	private final SpringAndDamperDoubleInterpolator leftAmpInterpolator = new SpringAndDamperDoubleInterpolator(
			0.0f,
			AudioMath.dbToLevelF( MixdownSliderDbToLevelComputer.LINEAR_HIGHEST_DB ) );
	private final SpringAndDamperDoubleInterpolator rightAmpInterpolator = new SpringAndDamperDoubleInterpolator(
			0.0f,
			AudioMath.dbToLevelF( MixdownSliderDbToLevelComputer.LINEAR_HIGHEST_DB ) );

	public LaneProcessor( final I instance,
			final MixerNInstanceConfiguration channelConfiguration,
			final int laneNumber )
	{
		this.instance = instance;

		this.laneNumber = laneNumber;
		leftIndex = (laneNumber + 1) * 2;
		rightIndex = leftIndex + 1;

		leftAmpInterpolator.hardSetValue( 0.0f );
		rightAmpInterpolator.hardSetValue( 0.0f );
	}

	public void processLaneMixToOutput( final ThreadSpecificTemporaryEventStorage tses,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		// Only process if something is connected to us!
		final boolean leftConnected = channelConnectedFlags.get( leftIndex );
		final boolean rightConnected = channelConnectedFlags.get( rightIndex );
		final boolean inputConnected = ( leftConnected | rightConnected );

		final float[] leftMasterOutputChannel = channelBuffers[ 0 ].floatBuffer;
		final float[] rightMasterOutputChannel = channelBuffers[ 1 ].floatBuffer;

		if( inputConnected )
		{
			final float[] tmpBuffer = tses.temporaryFloatArray;

			final float[] leftInputChannel = channelBuffers[ leftIndex ].floatBuffer;
			final float[] rightInputChannel = channelBuffers[ rightIndex ].floatBuffer;

			if( leftConnected )
			{
				if( !leftAmpInterpolator.checkForDenormal() )
				{
					leftAmpInterpolator.generateControlValues( tmpBuffer, 0, numFrames );

					for( int s = 0 ; s < numFrames ; ++s )
					{
						final float oneFloat = leftInputChannel[frameOffset + s] * tmpBuffer[s];
						final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

						if( absFloat > currentLeftMeterReading )
						{
							currentLeftMeterReading = absFloat;
						}

						leftMasterOutputChannel[frameOffset + s] += oneFloat;
					}
				}
				else
				{
					for( int s = 0 ; s < numFrames ; ++s )
					{
						final float oneFloat = leftInputChannel[frameOffset + s] * desiredLeftAmpMultiplier;
						final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

						if( absFloat > currentLeftMeterReading )
						{
							currentLeftMeterReading = absFloat;
						}

						leftMasterOutputChannel[frameOffset + s] += oneFloat;
					}
				}
			}

			if( rightConnected )
			{
				if( !rightAmpInterpolator.checkForDenormal() )
				{
					rightAmpInterpolator.generateControlValues( tmpBuffer, 0, numFrames );

					for( int s = 0 ; s < numFrames ; ++s )
					{
						final float oneFloat = rightInputChannel[frameOffset + s] * tmpBuffer[s];
						final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

						if( absFloat > currentRightMeterReading )
						{
							currentRightMeterReading = absFloat;
						}

						rightMasterOutputChannel[frameOffset + s] += oneFloat;
					}
				}
				else
				{
					for( int s = 0 ; s < numFrames ; ++s )
					{
						final float oneFloat = rightInputChannel[frameOffset + s] * desiredRightAmpMultiplier;
						final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

						if( absFloat > currentRightMeterReading )
						{
							currentRightMeterReading = absFloat;
						}

						rightMasterOutputChannel[frameOffset + s] += oneFloat;
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

		leftAmpInterpolator.notifyOfNewValue( desiredLeftAmpMultiplier );
		rightAmpInterpolator.notifyOfNewValue( desiredRightAmpMultiplier );
	}

	public void setSampleRate( final int sampleRate )
	{
		leftAmpInterpolator.reset( sampleRate );
		rightAmpInterpolator.reset( sampleRate );
	}
}
