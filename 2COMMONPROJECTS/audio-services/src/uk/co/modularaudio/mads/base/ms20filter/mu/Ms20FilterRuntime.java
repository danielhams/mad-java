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

package uk.co.modularaudio.mads.base.ms20filter.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;

public class Ms20FilterRuntime
{
	private static Log log = LogFactory.getLog( Ms20FilterRuntime.class.getName() );

	private float previousValuesFrequency = -10.0f;
	private float previousValueK = -1.0f;
	private float previousValueB = -1.0f;
	private float previousValueA = -1.0f;

//	private float lastClipOut = 0.0f;
//	private float delayOneVal = 0.0f;
//	private float delayTwoVal = 0.0f;
//	private float delayThreeVal = 0.0f;
//	private float delayFourVal = 0.0f;
//	private float delayFiveVal = 0.0f;

	private float lastClipRing = 0.0f;
	private float delayOneRing = 0.0f;
	private float delayTwoRing = 0.0f;
	private float delayThreeRing = 0.0f;
	private float delayFourRing = 0.0f;
	private float delayFiveRing = 0.0f;
//	private UnsafeFloatRingBuffer delayFiveRing = new UnsafeFloatRingBuffer( 10 );

	private double onePiDOverSampleRate = 0.0;

	public Ms20FilterRuntime( final int sampleRate )
	{
		this.onePiDOverSampleRate = MathDefines.ONE_PI_D / sampleRate;
		reset();
	}

	private void reset()
	{

//		delayOneVal = 0.0f;
//		delayTwoVal = 0.0f;
//		delayThreeVal = 0.0f;
//		delayFourVal = 0.0f;
//		delayFiveVal = 0.0f;

//		lastClipRing.clear();
//		delayOneRing.clear();
//		delayTwoRing.clear();
//		delayThreeRing.clear();
//		delayFourRing.clear();
//		delayFiveRing.clear();
		lastClipRing = 0.0f;
		delayOneRing = 0.0f;
		delayTwoRing = 0.0f;
		delayThreeRing = 0.0f;
		delayFourRing = 0.0f;
		delayFiveRing = 0.0f;

//		lastClipOut = 0.0f;

//		if( lastClipRing.getNumReadable() == 0 )
//		{
//			for( int i = 0 ; i < numToDelay ; i++ )
//			{
//				lastClipRing.writeOne( 0.0f );
//				delayOneRing.writeOne( 0.0f );
//				delayTwoRing.writeOne( 0.0f );
//				delayThreeRing.writeOne( 0.0f );
//				delayFourRing.writeOne( 0.0f );
//				delayFiveRing.writeOne(  0.0f );
//			}
//		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float frequency,
			final float filterResonance,
			final float filterThreshold )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}
		recomputeFrequencyDepVals( frequency );

		for( int i = 0 ; i < length ; i++ )
		{
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], filterResonance, filterThreshold );
		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float[] frequencyFloats,
			final float filterResonance,
			final float filterThreshold )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}

		for( int i = 0 ; i < length ; i++ )
		{
			recomputeFrequencyDepVals( frequencyFloats[ i ] );
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], filterResonance, filterThreshold );
		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float[] frequencyFloats,
			final float[] resonanceFloats,
			final float filterThreshold )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}

		for( int i = 0 ; i < length ; i++ )
		{
			recomputeFrequencyDepVals( frequencyFloats[ i ] );
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], resonanceFloats[i], filterThreshold );
		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float[] frequencyFloats,
			final float resonance,
			final float[] thresholdFloats )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}

		for( int i = 0 ; i < length ; i++ )
		{
			recomputeFrequencyDepVals( frequencyFloats[ i ] );
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], resonance, thresholdFloats[i] );
		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float[] frequencyFloats,
			final float[] resonanceFloats,
			final float[] thresholdFloats )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}

		for( int i = 0 ; i < length ; i++ )
		{
			recomputeFrequencyDepVals( frequencyFloats[ i ] );
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], resonanceFloats[i], thresholdFloats[i] );
		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float frequency,
			final float[] resonanceFloats,
			final float[] thresholdFloats )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}

		recomputeFrequencyDepVals( frequency );

		for( int i = 0 ; i < length ; i++ )
		{
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], resonanceFloats[i], thresholdFloats[i] );
		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float frequency,
			final float resonance,
			final float[] thresholdFloats )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}

		recomputeFrequencyDepVals( frequency );

		for( int i = 0 ; i < length ; i++ )
		{
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], resonance, thresholdFloats[i] );
		}
	}

	public final void filterFloats( final FrequencyFilterMode filterMode,
			final float[] inputFloats,
			final int pos,
			final int length,
			final float frequency,
			final float[] resonanceFloats,
			final float threshold )
	{
		if( filterMode == FrequencyFilterMode.NONE )
		{
			return;
		}

		recomputeFrequencyDepVals( frequency );

		for( int i = 0 ; i < length ; i++ )
		{
			inputFloats[ i ] = innerLoopFilterCalc( filterMode, inputFloats[i], resonanceFloats[i], threshold );
		}
	}

	private void recomputeFrequencyDepVals( final float frequency )
	{
		if( frequency != previousValuesFrequency )
		{
			previousValuesFrequency = frequency;
//			previousValueK = (float)FastMath.tan( MathDefines.ONE_PI_D * (frequency / sampleRate ) );
			previousValueK = (float)FastMath.tan( onePiDOverSampleRate  * frequency );

			previousValueB = ( previousValueK / (previousValueK + 1 ) );
			previousValueA = ( (previousValueK - 1 ) / (previousValueK +  1 ) );
		}
	}

	private float innerLoopFilterCalc( final FrequencyFilterMode mode, final float inValue, final float filterResonance, final float filterThreshold )
	{
//		inValue = checkVal( inValue );
		float lastClipOut = lastClipRing;
		float firstSumResult;
		switch( mode )
		{
			case LP:
			{
				firstSumResult = inValue - lastClipOut;
				break;
			}
			case HP:
			{
				firstSumResult = -lastClipOut;
				break;
			}
			case BP:
			case BR:
			{
				firstSumResult = -(lastClipOut + inValue);
				break;
			}
			default:
			{
				firstSumResult = 0.0f;
				if( log.isWarnEnabled() )
				{
					log.warn("Unknown filter mode: " + mode );
				}
				break;
			}
		}

		final float firstBResult = previousValueB * firstSumResult;

		float delayTwoVal = delayTwoRing;
		final float firstAResult = previousValueA * delayTwoVal;

		final float delayOneVal = delayOneRing;
		float secondSumResult;
		switch( mode )
		{
			case LP:
			{
				secondSumResult = firstBResult + delayOneVal - firstAResult;
				break;
			}
			case HP:
			{
				secondSumResult = firstBResult + delayOneVal - firstAResult;
				break;
			}
			case BP:
			case BR:
			{
				secondSumResult = firstBResult + delayOneVal - firstAResult;
				break;
			}
			default:
			{
				secondSumResult = 0.0f;
				if( log.isWarnEnabled() )
				{
					log.warn("Unknown filter mode: " + mode );
				}
			}
		}

		float thirdSumResult;
		switch( mode )
		{
			case LP:
			{
				thirdSumResult = secondSumResult + lastClipOut;
				break;
			}
			case HP:
			{
				thirdSumResult = (secondSumResult + lastClipOut) - inValue;
				break;
			}
			case BP:
			case BR:
			{
				thirdSumResult = secondSumResult + lastClipOut + inValue;
				break;
			}
			default:
			{
				thirdSumResult = 0.0f;
				if( log.isWarnEnabled() )
				{
					log.warn("Unknown filter mode: " + mode );
				}
				break;
			}
		}

		final float secondBResult = previousValueB * thirdSumResult;

		float delayFourVal = delayFourRing;
		final float secondAResult = previousValueA * delayFourVal;

		float delayThreeVal = delayThreeRing;
		float fourthSumResult;
		switch( mode )
		{
			case LP:
			{
				fourthSumResult = secondBResult + delayThreeVal - secondAResult;
				break;
			}
			case HP:
			{
				fourthSumResult = secondBResult + delayThreeVal - secondAResult;
				break;
			}
			case BP:
			case BR:
			{
				fourthSumResult = secondBResult + delayThreeVal - secondAResult;
				break;
			}
			default:
			{
				fourthSumResult = 0.0f;
				if( log.isWarnEnabled() )
				{
					log.warn("Unknown filter mode: " + mode );
				}
				break;
			}
		}

		float fifthSumResult;
		switch( mode )
		{
			case LP:
			{
				fifthSumResult = fourthSumResult;
				break;
			}
			case HP:
			{
				fifthSumResult = fourthSumResult + inValue;
				break;
			}
			case BP:
			case BR:
			{
				fifthSumResult = fourthSumResult;
				break;
			}
			default:
			{
				if( log.isWarnEnabled() )
				{
					log.warn( "Unknown filter mode: " + mode );
				}
				fifthSumResult = 0.0f;
				break;
			}
		}

		float delayFiveVal = delayFiveRing;

		final float firstKResult = filterResonance * delayFiveVal;

//		float saturatedVal = saturator.processValue( filterThreshold, firstKResult );
		float saturatedVal;
		{
			final int sign = ( firstKResult < 0.0f ? -1 : 1 );
			final float absValue = sign * firstKResult;
			if( absValue > filterThreshold )
			{
				float amountOver = absValue - filterThreshold;
				amountOver = (amountOver > filterThreshold ? filterThreshold : amountOver );
				float haveWe = filterThreshold - amountOver;
				haveWe = (haveWe < 0.0f ? 0.0f : haveWe );
				saturatedVal = sign * haveWe;
			}
			else
			{
				saturatedVal = firstKResult;
			}
		}

		final float clippedVal = (0.25f * firstKResult) + (0.75f * saturatedVal );

		float clipSumVal;
		switch( mode )
		{
			case LP:
			{
				clipSumVal = clippedVal;
				break;
			}
			case HP:
			{
				clipSumVal = clippedVal;
				break;
			}
			case BP:
			case BR:
			{
				clipSumVal = clippedVal + inValue;
				break;
			}
			default:
			{
				if( log.isWarnEnabled() )
				{
					log.warn( "Unknown filter mode: " + mode );
				}
				clipSumVal = 0.0f;
			}
		}

//		lastClipOut = checkVal( clipSumVal );
		lastClipOut = clipSumVal;

		lastClipRing = lastClipOut;

//		if( firstBResult == Float.NaN || firstBResult == Float.NEGATIVE_INFINITY ||
//				firstBResult == Float.POSITIVE_INFINITY )
//		{
//			delayOneVal = 0.0f;
//		}
//		else
//		{
//			delayOneVal = firstBResult;
//		}
//		delayOneRing.writeOne( delayOneVal );
		delayOneRing = delayOneVal;
//		delayTwoVal = checkVal( secondSumResult );
		delayTwoVal = secondSumResult;
		delayTwoRing = delayTwoVal;
//		delayThreeVal = checkVal( secondBResult );
		delayThreeVal = secondBResult;
		delayThreeRing = delayThreeVal;
//		delayFourVal = checkVal( fourthSumResult );
		delayFourVal = fourthSumResult;
		delayFourRing = delayFourVal;
//		delayFiveVal = checkVal( fifthSumResult );
		delayFiveVal = fifthSumResult;
		delayFiveRing = delayFiveVal;

		final float filteredVal = ( mode == FrequencyFilterMode.BR ? inValue - fifthSumResult : fifthSumResult );

		return filteredVal;
	}

//	private final float checkVal( float inVal )
//	{
//		if( inVal == 0.0f )
//		{
//			return 0.0f;
//		}
//		float absVal = ( inVal < 0.0f ? -inVal : inVal );
//		if( absVal < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
//		{
//			return 0.0f;
//		}
//		else
//		{
//			return inVal;
//		}
//	}
}
