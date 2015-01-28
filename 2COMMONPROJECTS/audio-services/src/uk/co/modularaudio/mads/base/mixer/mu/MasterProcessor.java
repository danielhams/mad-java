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

	private final MixerMadInstance instance;

	private float curValueRatio;
	private float newValueRatio;

	private final int numChannelsPerLane;

	private final int[] outputChannelIndexes;

	private float desiredAmpMultiplier = 0.5f;

	private float desiredLeftAmpMultiplier = 0.5f;
	private float currentLeftAmpMultiplier = 0.5f;
	private float desiredRightAmpMultiplier = 0.5f;
	private float currentRightAmpMultiplier = 0.5f;

	private float desiredPanValue;

	private float leftMeterLevel;
	private float rightMeterLevel;

	public MasterProcessor( final MixerMadInstance instance,
			final MixerMadInstanceConfiguration channelConfiguration,
			final float curValueRatio,
			final float newValueRatio )
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

	public void processMasterOutput( final ThreadSpecificTemporaryEventStorage tses,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int position,
			final int length )
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
				final int outputChannelIndex = outputChannelIndexes[ cn ];
				final float[] outputFloats = channelBuffers[ outputChannelIndex ].floatBuffer;
				float outputFloat = outputFloats[ s ] * ( cn == 0 ? currentLeftAmpMultiplier : currentRightAmpMultiplier );
				final float absFloat = (outputFloat < 0.0f ? -outputFloat : outputFloat );

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

	public void emitMasterMeterReadings( final ThreadSpecificTemporaryEventStorage tses, final long emitTimestamp )
	{
//			log.debug("Emitting one at " + emitTimestamp);
		instance.emitMasterMeterReading( tses, emitTimestamp, leftMeterLevel, rightMeterLevel );
		leftMeterLevel = 0.0f;
		rightMeterLevel = 0.0f;
	}

	public void setMasterAmp( final float ampValue )
	{
		desiredAmpMultiplier = ampValue;
		recomputeDesiredChannelAmps();
	}

	public void resetCurNewValues( final float curValueRatio2, final float newValueRatio2 )
	{
		this.curValueRatio = curValueRatio2;
		this.newValueRatio = newValueRatio2;
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
	}
}
