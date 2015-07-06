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

package uk.co.modularaudio.util.audio.stft.tools;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.math.MathFormatter;

public class PeakFinder
{
	private static Log log = LogFactory.getLog( PeakFinder.class.getName() );

	// Anything below this threshold is marked as not involved in a peak
	// -60 dB seems a nice level
//	public static final float LOCK_THRESHOLD = AudioMath.FdbToLevel( -60.0f );
//	public static final float LOCK_THRESHOLD = AudioMath.FdbToLevel( -10.0f );
//	public static final float LOCK_THRESHOLD = AudioMath.FdbToLevel( -2.0f );
//	public static final float LOCK_THRESHOLD = 2.0f;
	public static final float LOCK_THRESHOLD = 0.1f;

	public final static boolean DEBUG_PEAKS = false;

//	private static final int LOWER_BIN_INDEX_THRESHOLD = 20;
//	private static final int LOWER_BIN_INDEX_THRESHOLD = 1;
	private static final int LOWER_BIN_INDEX_THRESHOLD = -1;

	private final int numBins;
	private final int lastBinIndex;

	public PeakFinder( final StftParameters params )
	{
		this.numBins = params.getNumBins();
		this.lastBinIndex = numBins - 1;
		if( log.isDebugEnabled() )
		{
			log.debug("Peak threshold set to " + LOCK_THRESHOLD);
		}
	}

	public final void identifyPeaks( final float[] inAmps,
			final int[] peaksBuffer,
			final int[] binToPeakBuffer )
	{
		setupLockIndicatorsNew( inAmps, peaksBuffer, binToPeakBuffer );
		getRegionsOfInfluence( peaksBuffer, binToPeakBuffer, inAmps );

		// Finally knock out all the peaks and regions where amp < tolerance
// 		unlockQuietBins( peaksBuffer, binToPeakBuffer, inAmps );
	}

	protected final void unlockQuietBins(final int[] peaksBuffer, final int[] binToPeakBuffer, final float[] amps)
	{
		for( int i = 0 ; i < lastBinIndex ; i++ )
		{
			final float a = amps[i];
			// Not sure this is smart enough
			if( a <= LOCK_THRESHOLD )
			{
				if( DEBUG_PEAKS )
				{
//					log.debug("Unlocking bin " + i);
				}
				final int peakBinNum = binToPeakBuffer[ i ];
				// Check if we are leaving any orphaned peak bin references
				if( peakBinNum < i && peakBinNum != -1 )
				{
					for( int c = i ; c < numBins ; c++ )
					{
						if( binToPeakBuffer[c] == peakBinNum )
						{
							binToPeakBuffer[c] = -1;
//							amps[c] = 0.0f;
						}
						else
						{
							break;
						}
					}
				}
				else if( peakBinNum > i )
				{
					for( int c = i ; c >= 0 ; c-- )
					{
						if( binToPeakBuffer[c] == peakBinNum )
						{
							binToPeakBuffer[c] = -1;
//							amps[c] = 0.0f;
						}
						else
						{
							break;
						}
					}
				}
				binToPeakBuffer[i] = -1;
//				amps[i] = 0.0f;
			}
//			else if( DEBUG_PEAKS )
//			{
//				log.debug( "Skipping bin with small amp: " + a );
//			}
		}
	}

	private final void getRegionsOfInfluence(final int[] peaksBuffer, final int[] binToPeakBuffer, final float[] amps)
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
					upperBound = findMinimaBetween( lockedBinNum, nextPeakBin, amps );
				}
				// Fill in the bins to be "locked" to this one
				Arrays.fill( binToPeakBuffer, lowerBound, upperBound, lockedBinNum );

				// Now move up lower bound
				lowerBound = upperBound;
			}
		}
	}

	private final void setupLockIndicatorsNew( final float[] amps, final int[] peaksBuffer, final int[] binToPeakBuffer )
	{
//		float maxAmp = 0.0f;
		int sliIndex = 0;

		for( int i = 0 ; i < numBins ; i++ )
		{
			final float curAmp = amps[i];
//			if( curAmp > maxAmp )
//			{
//				maxAmp = curAmp;
//			}
			if( curAmp > LOCK_THRESHOLD )
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
//					int peakBinNum = i;
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
//				if( DEBUG_PEAKS && curAmp > 0.0f )
//				{
//					log.debug("Skipping bin " + i + " with small amp: " + curAmp );
//				}
				binToPeakBuffer[i] = -1;
			}
		}
		peaksBuffer[ sliIndex ] = -1;
//		log.debug("MaxAmp is " + MathFormatter.slowFloatPrint( maxAmp, 10, true ) );
	}

	private final int findMinimaBetween( final int lockedBinNum,
			final int nextBinNum,
			final float[] amps )
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
//			derivativeSignum = Math.signum( derivative );
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

	public final void quickHackZeroQuietBins( final float[] amps, final float[] complexFrame )
	{
		for( int i = 1 ; i < numBins - 1 ; i++ )
		{
			if( amps[ i ] < LOCK_THRESHOLD )
			{
				amps [ i ] = 0.0f;
				complexFrame[ (2*i) ] = 0.0f;
				complexFrame[ (2*i) + 1 ] = 0.0f;
			}
		}
	}

	public final void quickHackBinZeroing( final int[] binToPeakBuffer, final float[] amps )
	{
		// Now zero all the bins that aren't marked as a peak (ignore dc and nyquist)
		final float[] synthAmps = amps;

		synthAmps[0] = 0.0f;
		synthAmps[synthAmps.length - 1] = 0.0f;
		for( int i = 1 ; i < lastBinIndex ; i++ )
		{
			if( binToPeakBuffer[ i ] != i )
			{
				synthAmps[ i ] = 0.0f;
			}
		}

		// Overamping peaks
		for( int i = 1 ; i < numBins - 1 ; i++ )
		{
			if( binToPeakBuffer [ i ] == i )
			{
				final float newAmp = synthAmps[ i ]; // * 2;
				synthAmps[ i ] = newAmp;
				// Make two surrounding bins 2/3 of the power with inverted phase
				final float surroundBinAmp = newAmp * ((float)2/3);
				if( i > 0 )
				{
					synthAmps[ i - 1] = surroundBinAmp;
				}
				if( i < numBins - 2 )
				{
					synthAmps[ i + 1 ] = surroundBinAmp;
				}
			}
		}
	}

	public final void quickZeroingLeaveBins( final int[] binToPeakBuffer,
			final float[] amps,
			final float[] complexFrame,
			final int binsOnEachSide,
			final boolean cleanPeak,
			final boolean cleanAllBinsAbovePeaks,
			final boolean cleanAllBinsBelowPeaks )
	{
		// Now zero all the bins that aren't marked as a peak (ignore dc and nyquist)

		amps[0] = 0.0f;
		amps[amps.length - 1] = 0.0f;
		for( int i = 1 ; i < lastBinIndex ; i++ )
		{
			final int currentBinPeakPointer = binToPeakBuffer[ i ];

			final int distanceToPeak = currentBinPeakPointer - i;

			if( distanceToPeak == 0 )
			{
				if( cleanPeak )
				{
					amps[ i ] = 0.0f;
					complexFrame[ 2*i ] = 0.0f;
					complexFrame[ (2*i)+1 ] = 0.0f;
				}
			}
			else if( distanceToPeak > 0 )
			{
				if( cleanAllBinsBelowPeaks || distanceToPeak > binsOnEachSide )
				{
					amps[ i ] = 0.0f;
					complexFrame[ 2*i ] = 0.0f;
					complexFrame[ (2*i)+1 ] = 0.0f;
				}
			}
			else if( distanceToPeak < 0 )
			{
				if( cleanAllBinsAbovePeaks || -distanceToPeak > binsOnEachSide )
				{
					amps[ i ] = 0.0f;
					complexFrame[ 2*i ] = 0.0f;
					complexFrame[ (2*i)+1 ] = 0.0f;
				}
			}
		}
	}
}
