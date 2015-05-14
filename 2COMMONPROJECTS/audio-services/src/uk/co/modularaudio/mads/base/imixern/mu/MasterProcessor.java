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

public class MasterProcessor<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D, I>>
{
//	private static final Log log = LogFactory.getLog( MasterProcessor.class.getName() );

	private final I instance;

	private float desiredAmpMultiplier = 0.0f;

	private float desiredLeftAmpMultiplier = 0.0f;
	private float desiredRightAmpMultiplier = 0.0f;

	private float desiredPanValue;

	private float leftMeterLevel;
	private float rightMeterLevel;

	private final SpringAndDamperDoubleInterpolator leftAmpInterpolator = new SpringAndDamperDoubleInterpolator(
			0.0f,
			AudioMath.dbToLevelF( MixdownSliderDbToLevelComputer.LINEAR_HIGHEST_DB ) );
	private final SpringAndDamperDoubleInterpolator rightAmpInterpolator = new SpringAndDamperDoubleInterpolator(
			0.0f,
			AudioMath.dbToLevelF( MixdownSliderDbToLevelComputer.LINEAR_HIGHEST_DB ) );

	public MasterProcessor( final I instance,
			final MixerNInstanceConfiguration channelConfiguration )
	{
		this.instance = instance;
		leftAmpInterpolator.hardSetValue( 0.0f );
		rightAmpInterpolator.hardSetValue( 0.0f );
	}

	public void processMasterOutput( final ThreadSpecificTemporaryEventStorage tses,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final float[] tmpBuffer = tses.temporaryFloatArray;

		final float[] leftOutputFloats = channelBuffers[ 0 ].floatBuffer;
		if( !leftAmpInterpolator.checkForDenormal() )
		{
			// First left
			leftAmpInterpolator.generateControlValues( tmpBuffer, 0, numFrames );
			for( int s = 0 ; s < numFrames ; ++s )
			{
				final float oneFloat = leftOutputFloats[ frameOffset + s ] * tmpBuffer[s];
				final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

				if( absFloat > leftMeterLevel )
				{
					leftMeterLevel = absFloat;
				}

				leftOutputFloats[ frameOffset + s ] = oneFloat;
			}
		}
		else
		{
			for( int s = 0 ; s < numFrames ; ++s )
			{
				final float oneFloat = leftOutputFloats[ frameOffset + s ] * desiredLeftAmpMultiplier;
				final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

				if( absFloat > leftMeterLevel )
				{
					leftMeterLevel = absFloat;
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

				if( absFloat > rightMeterLevel )
				{
					rightMeterLevel = absFloat;
				}

				rightOutputFloats[ frameOffset + s ] = oneFloat;
			}
		}
		else
		{
			for( int s = 0 ; s < numFrames ; ++s )
			{
				final float oneFloat = rightOutputFloats[ frameOffset + s ] * desiredRightAmpMultiplier;
				final float absFloat = (oneFloat < 0.0f ? -oneFloat : oneFloat );

				if( absFloat > rightMeterLevel )
				{
					rightMeterLevel = absFloat;
				}

				rightOutputFloats[ frameOffset + s ] = oneFloat;
			}
		}
	}

	public void emitMasterMeterReadings( final ThreadSpecificTemporaryEventStorage tses, final long emitTimestamp )
	{
//		log.debug("Emitting one at " + emitTimestamp);
		instance.emitMasterMeterReading( tses, emitTimestamp, leftMeterLevel, rightMeterLevel );
		leftMeterLevel = 0.0f;
		rightMeterLevel = 0.0f;
	}

	public void setMasterAmp( final float ampValue )
	{
		desiredAmpMultiplier = ampValue;
		recomputeDesiredChannelAmps();
	}

	public void setMasterPan( final float panValue )
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

		if( desiredLeftAmpMultiplier < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
		{
			desiredLeftAmpMultiplier = 0.0f;
		}

		if( desiredRightAmpMultiplier < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
		{
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
