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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.math.MixdownSliderDbToLevelComputer;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class MasterOutProcessor<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D, I>>
	implements LaneProcessor
{
	private static final Log log = LogFactory.getLog( MasterOutProcessor.class.getName() );

	private final I instance;

	private float desiredAmpMultiplier = 0.0f;

	private float desiredLeftAmpMultiplier = 0.0f;
	private float desiredRightAmpMultiplier = 0.0f;

	private float desiredPanValue;

	private float currentLeftMeterReading;
	private float currentRightMeterReading;

	private final SpringAndDamperDouble24Interpolator leftAmpInterpolator = new SpringAndDamperDouble24Interpolator();
	private final SpringAndDamperDouble24Interpolator rightAmpInterpolator = new SpringAndDamperDouble24Interpolator();

	public MasterOutProcessor( final I instance,
			final MixerNInstanceConfiguration channelConfiguration )
	{
		this.instance = instance;
		leftAmpInterpolator.hardSetValue( 0.0f );
		rightAmpInterpolator.hardSetValue( 0.0f );

		final float linearHighestLevel = AudioMath.dbToLevelF( MixdownSliderDbToLevelComputer.LINEAR_HIGHEST_DB );
		leftAmpInterpolator.resetLowerUpperBounds( 0.0f, linearHighestLevel );
		rightAmpInterpolator.resetLowerUpperBounds( 0.0f, linearHighestLevel );
	}

	@Override
	public void processLane( final ThreadSpecificTemporaryEventStorage tses,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final float[] tmpBuffer = tses.temporaryFloatArray;

		final float[] leftOutputFloats = channelBuffers[ 0 ].floatBuffer;

		float leftMeterReading = currentLeftMeterReading;
		float rightMeterReading = currentRightMeterReading;

		if( !leftAmpInterpolator.checkForDenormal() )
		{
			// First left
			leftAmpInterpolator.generateControlValues( tmpBuffer, 0, numFrames );
			for( int s = 0 ; s < numFrames ; ++s )
			{
				final float oneFloat = leftOutputFloats[ frameOffset + s ] * tmpBuffer[s];
				final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

				if( absFloat > leftMeterReading )
				{
					leftMeterReading = absFloat;
				}

				leftOutputFloats[ frameOffset + s ] = oneFloat;
			}
		}
		else
		{
			final float ampToUse = leftAmpInterpolator.getValue();
			for( int s = 0 ; s < numFrames ; ++s )
			{
				final float oneFloat = leftOutputFloats[ frameOffset + s ] * ampToUse;
				final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

				if( absFloat > leftMeterReading )
				{
					leftMeterReading = absFloat;
				}

				leftOutputFloats[ frameOffset + s ] = oneFloat;
			}
		}

		final float[] rightOutputFloats = channelBuffers[ 1 ].floatBuffer;
		if( !rightAmpInterpolator.checkForDenormal() )
		{
			rightAmpInterpolator.generateControlValues( tmpBuffer, 0, numFrames );
			for( int s = 0 ; s < numFrames ; ++s )
			{
				final float oneFloat = rightOutputFloats[ frameOffset + s ] * tmpBuffer[s];
				final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

				if( absFloat > rightMeterReading )
				{
					rightMeterReading = absFloat;
				}

				rightOutputFloats[ frameOffset + s ] = oneFloat;
			}
		}
		else
		{
			final float ampToUse = rightAmpInterpolator.getValue();
			for( int s = 0 ; s < numFrames ; ++s )
			{
				final float oneFloat = rightOutputFloats[ frameOffset + s ] * ampToUse;
				final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

				if( absFloat > rightMeterReading )
				{
					rightMeterReading = absFloat;
				}

				rightOutputFloats[ frameOffset + s ] = oneFloat;
			}
		}

		currentLeftMeterReading = leftMeterReading;
		currentRightMeterReading = rightMeterReading;
	}

	@Override
	public void emitLaneMeterReadings( final ThreadSpecificTemporaryEventStorage tses,
			final long emitTimestamp )
	{
//		log.debug("Emitting master reading at " + emitTimestamp);
		instance.emitMeterReading( tses, emitTimestamp, 0, currentLeftMeterReading, currentRightMeterReading );
		currentLeftMeterReading = 0.0f;
		currentRightMeterReading = 0.0f;
	}

	@Override
	public void setLaneAmp( final float ampValue )
	{
		desiredAmpMultiplier = ampValue;
		recomputeDesiredChannelAmps();
	}

	@Override
	public void setLanePan( final float panValue )
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

		if( desiredLeftAmpMultiplier < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
		{
			desiredLeftAmpMultiplier = 0.0f;
		}

		if( desiredRightAmpMultiplier < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
		{
			desiredRightAmpMultiplier = 0.0f;
		}

		leftAmpInterpolator.notifyOfNewValue( desiredLeftAmpMultiplier );
		rightAmpInterpolator.notifyOfNewValue( desiredRightAmpMultiplier );
	}

	@Override
	public void setSampleRate( final int sampleRate )
	{
		leftAmpInterpolator.reset( sampleRate );
		rightAmpInterpolator.reset( sampleRate );
	}

	@Override
	public void setLaneActive( final boolean active )
	{
		// Doesn't apply to master
	}
}
