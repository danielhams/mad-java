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

package uk.co.modularaudio.util.audio.spectraldisplay.runav;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.MathFormatter;


public class PeakDetectComputer implements RunningAverageComputer
{
	private static Log log = LogFactory.getLog( PeakDetectComputer.class.getName() );

	private static final boolean DEBUG_PEAKS = false;

	private final static int MAX_BINS = 16384;

	private final int[] peaksBuffer = new int[ MAX_BINS ];
	private final int[] binToPeakBuffer = new int[ MAX_BINS ];

	private final static float MIN_NORM_LOCK_DB = -96.0f;
	private final static float MIN_NORM_LOCK_VALUE = AudioMath.dbToLevelF( MIN_NORM_LOCK_DB );

	// Should probably remove this at some point.
	private static final int LOWER_BIN_INDEX_THRESHOLD = 40;

	private float maxExpectedAmplitude = 512.0f;

	private float minLockValue = maxExpectedAmplitude * MIN_NORM_LOCK_VALUE;

	@Override
	public void computeNewRunningAverages( final int currentNumBins, final float[] newValues, final float[] runningValues)
	{
		System.arraycopy( newValues, 0, runningValues, 0, currentNumBins );

		setupLockIndicatorsNew( runningValues, peaksBuffer, binToPeakBuffer, currentNumBins );
		getRegionsOfInfluence( runningValues, peaksBuffer, binToPeakBuffer, currentNumBins );

		zeroNonPeakBins( runningValues, peaksBuffer, binToPeakBuffer, currentNumBins );
	}

	public void setMaxExpectedAmplitude( final float newAmp )
	{
		this.maxExpectedAmplitude = newAmp;
		this.minLockValue = maxExpectedAmplitude * MIN_NORM_LOCK_VALUE;
	}

	private final void setupLockIndicatorsNew( final float[] amps,
			final int[] peaksBuffer,
			final int[] binToPeakBuffer,
			final int numBins )
	{
		int sliIndex = 0;

		for( int i = 0 ; i < numBins ; i++ )
		{
			final float curAmp = amps[i];
			if( curAmp > minLockValue )
			{
				final int indexMinus2 = ( i <3 ? 0 : i - 2 );
				final int indexMinus1 = ( i <2 ? 0 : i - 1 );
				final int indexPlus1 = ( i > numBins - 3 ? numBins - 1 : i + 1 );
				final int indexPlus2 = ( i > numBins - 4 ? numBins - 1 : i + 2 );

				boolean isPeak = false;
				// (i2 > i0) && (i2 >= i1) && (i2 >= i3) && (i2 > i4);
				if( i == 0 )
				{
					if( ( curAmp > amps[indexPlus1]) &&
							( curAmp > amps[indexPlus2]) &&
							( amps[indexPlus1] >= amps[indexPlus2] ) )
					{
						isPeak = true;
					}
				}
				else if( i < LOWER_BIN_INDEX_THRESHOLD )
				{
					if( ( curAmp > amps[indexMinus1]) &&
							( curAmp > amps[indexPlus1]) )
					{
						isPeak = true;
					}
				}
				else
				{
					if( (curAmp >= amps[indexMinus2]) &&
							(curAmp > amps[indexMinus1]) &&
							(curAmp > amps[indexPlus1]) &&
							(curAmp >= amps[indexPlus2]) &&
							amps[indexMinus1] >= amps[indexMinus1] &&
							amps[indexPlus1] >= amps[indexPlus2] )
					{
						isPeak = true;
					}
				}

				if( isPeak )
				{
					final int peakBinNum = ( i == 0 ? 1 : i );
					binToPeakBuffer[ peakBinNum ] = -2;
					peaksBuffer[ sliIndex++ ] = peakBinNum ;
					if( DEBUG_PEAKS )
					{
						if( log.isDebugEnabled() )
						{
							log.debug("Found a peak at index " + peakBinNum + " around " +
									MathFormatter.fastFloatPrint( amps[ indexMinus2 ], 5, true ) + ", " +
									MathFormatter.fastFloatPrint( amps[ indexMinus1 ], 5, true ) + ", " +
									MathFormatter.fastFloatPrint( curAmp, 5, true ) + ", " +
									MathFormatter.fastFloatPrint( amps[ indexPlus1 ], 5, true ) + ", " +
									MathFormatter.fastFloatPrint( amps[ indexPlus2 ], 5, true ) );
						}
					}
				}
			}
			else
			{
				binToPeakBuffer[i] = -1;
			}
		}
		peaksBuffer[ sliIndex ] = -1;
	}

	private final void getRegionsOfInfluence( final float[] amps,
			final int[] peaksBuffer,
			final int[] binToPeakBuffer,
			final int numBins )
	{
		int lowerBound = 0;
		int upperBound = -1;

		final int maxPeaksBufferIndex = peaksBuffer.length - 1;

		for( int i = 0 ; i <= maxPeaksBufferIndex ; i++ )
		{
			final int lockedBinNum = peaksBuffer[ i ];
			if( lockedBinNum == -1 )
			{
				break;
			}
			else
			{
				// Look for where the next peak is by finding the next "lock indicator" if there is one
				int nextPeakBin = -1;
				if( (i + 1) < maxPeaksBufferIndex )
				{
					nextPeakBin = peaksBuffer[ i + 1 ];
				}

				// If we don't have one (-1) then set the upper bound to be the final bin (but not nyquist bin)
				if( nextPeakBin == -1 )
				{
					upperBound = numBins - 1;
				}
				else
				{
					// Find the first minima between the current bin and the next one
					upperBound = findMinimaBetween( amps, lockedBinNum, nextPeakBin );
				}
				// Fill in the bins to be "locked" to this one
				Arrays.fill( binToPeakBuffer, lowerBound, upperBound, lockedBinNum );

				// Now move up lower bound
				lowerBound = upperBound;
			}
		}
	}

	private final int findMinimaBetween(
			final float[] amps,
			final int lockedBinNum,
			final int nextBinNum )
	{
		int retBin = -1;
		boolean foundIt = false;
		// Pull out first amp (on this maxima) and work out derivative
		// to next bin
		float prevAmp = amps[ lockedBinNum ];
		float curAmp = amps[ lockedBinNum + 1];
		float derivative = prevAmp - curAmp;
//		float prevDerSig = Math.signum( derivative );
		float derivativeSignum = ( derivative < 0.0f ? -1.0f : 1.0f );
//		log.debug("Examining bin " + lockedBinNum + " prevAmp " + prevAmp + " and curAmp " + curAmp + " with der " + derivative );
//		log.debug("PrevDerSig is " + prevDerSig + " and derSig is " + derivativeSignum );

		boolean lastDerivativeWasNegative = false;
		int binAtFirstNegativeDerivative = -1;
		// Basically walk the amps from lockedBinNum to nextBinNum
		// until we hit a sign change in the derivative (e.g goes from pos -> negative )
		for( int i = lockedBinNum + 1 ; !foundIt && i < nextBinNum ; i++ )
		{
			curAmp = amps[ i ];
			derivative = prevAmp - curAmp;
			derivativeSignum = ( derivative < 0.0f ? -1.0f : 1.0f );
//			log.debug("Examining bin " + i + " prevAmp " + prevAmp + " and curAmp " + curAmp + " with der " + derivative );
//			log.debug("PrevDerSig is " + prevDerSig + " and derSig is " + derivativeSignum );

			// derivative sign is positive, we are decreasing
			// if negative, we are increasing in amplitude.
			// for the sake of avoiding lobes being detected, we look for two negative derivatives
			// and use the first bin number
			if( derivativeSignum >= 0 )
			{
				lastDerivativeWasNegative = false;
			}
			else if( derivativeSignum < 0 )
			{
				if( lastDerivativeWasNegative )
				{
					retBin = binAtFirstNegativeDerivative;
					foundIt = true;
				}
				else
				{
					binAtFirstNegativeDerivative = i;
					lastDerivativeWasNegative = true;
				}
			}

			prevAmp = curAmp;
//			prevDerSig = derivativeSignum;
		}
		if( !foundIt && binAtFirstNegativeDerivative != -1 )
		{
			retBin = binAtFirstNegativeDerivative;
			foundIt = true;
		}
		if( !foundIt )
		{
//			log.warn("Couldn't find local minima - using half way between the two bins " + lockedBinNum + " and " + nextBinNum );
			return( (lockedBinNum + nextBinNum) / 2 );
		}
		return retBin;
	}

	private void zeroNonPeakBins( final float[] amps,
			final int[] peaksBuffer,
			final int[] binToPeakBuffer,
			final int currentNumBins )
	{
		for( int i = 0 ; i < currentNumBins ; ++i )
		{
			final int binToPeak = binToPeakBuffer[i];
			if( binToPeak != i )
			{
				amps[i] = 0.0f;
			}
		}

	}
}
