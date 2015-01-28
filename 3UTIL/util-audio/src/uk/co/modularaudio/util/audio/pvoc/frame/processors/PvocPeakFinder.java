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

package uk.co.modularaudio.util.audio.pvoc.frame.processors;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.pvoc.PvocParameters;
import uk.co.modularaudio.util.audio.pvoc.support.PvocDataFrame;

public class PvocPeakFinder
{
	private static Log log = LogFactory.getLog( PvocPeakFinder.class.getName() );

	// Anything below this threshold is marked as not involved in a peak
//	public static final float LOCK_THRESHOLD = 2.0f;

//	public static final float LOCK_THRESHOLD = 0.1f;

//	public static final float LOCK_THRESHOLD = ( 0.1f * 0.1f );

	public final static boolean DEBUG_PEAKS = false;

	private static final int LOWER_BIN_INDEX_THRESHOLD = 2;

	private final float peakLockThreshold;

	private final int numBins;
	private final int lastBinIndex;
	private final int lastBinIndexMinusOne;
	private final int lastBinIndexMinusTwo;
	private final int lastBinIndexMinusThree;

	public PvocPeakFinder( final PvocParameters params )
	{
		this.peakLockThreshold = params.getPeakLockThreshold();
		this.numBins = params.getNumBins();
		this.lastBinIndex = numBins - 1;
		this.lastBinIndexMinusOne = lastBinIndex - 1;
		this.lastBinIndexMinusTwo = lastBinIndex - 2;
		this.lastBinIndexMinusThree = lastBinIndex - 3;
		if( log.isDebugEnabled() )
		{
			log.debug("Peak threshold set to " + peakLockThreshold );
		}
	}

	public final void computeAmpsSquared( final PvocDataFrame curAnalFrame, final int c )
	{
		final float[] analComplexFrame = curAnalFrame.complexFrame[ c ];
		final float[] analAmpsSquared = curAnalFrame.ampsSquared[ c ];
		final float[] analAmps = curAnalFrame.amps[ c ];

		final float dcVal = analComplexFrame[ 0 ];
		final int dcSign = (dcVal < 0.0f ? -1 : 1 );
		curAnalFrame.dcSign[ c ] = dcSign;
		analAmps[ 0 ] = dcVal * dcSign;
		analAmpsSquared[0] = 0.0f;

		final float nyVal = analComplexFrame[ lastBinIndex ];
		final int nySign = (nyVal < 0.0f ? -1 : 1 );
		curAnalFrame.nySign[ c ] = nySign;
		analAmps[ lastBinIndex ] = nyVal * nySign;
		analAmpsSquared[ lastBinIndex ] = 0.0f;

		for( int i = 1 ; i < lastBinIndex ; i++ )
		{
			analAmpsSquared[ i ] = (
					(analComplexFrame[ 2*i ] * analComplexFrame[ 2*i] ) +
					(analComplexFrame[ (2*i)+1 ] * analComplexFrame[ (2*i) + 1] )
					);
		}
	}

	public final void computeAmpsFromAmpsSquared( final PvocDataFrame curAnalFrame, final int c )
	{
		final float[] analAmpsSquared = curAnalFrame.ampsSquared[ c ];
		final float[] analAmps = curAnalFrame.amps[ c ];

		for( int i = 1 ; i < lastBinIndex ; i++ )
		{
			analAmps[ i ] = (float)Math.sqrt( analAmpsSquared[i] );
		}
	}

	public final int identifyPeaks( final PvocDataFrame curAnalFrame, final int c )
	{
		final int numPeaksFound = setupLockIndicators( curAnalFrame, c );
		if( numPeaksFound > 0 )
		{
			getRegionsOfInfluence( curAnalFrame, c );
		}

		return numPeaksFound;
	}

	private final int setupLockIndicators( final PvocDataFrame curAnalFrame, final int c )
	{
		final float[] amps = curAnalFrame.ampsSquared[c];
		final int[] peaksBuffer = curAnalFrame.peaksBuffer[c];
		int sliIndex = 0;

		// Do zero bin seperately to avoid branch mispredict in the loop
		if( amps[0] > peakLockThreshold && amps[0] > amps[1] && amps[0] >= amps[2] )
		{
			// Map zero bin to bin 1 so phase computations kind of work.
			peaksBuffer[ sliIndex++ ] = 1;
		}

		for( int i = 1 ; i < LOWER_BIN_INDEX_THRESHOLD ; i++ )
		{
			if( amps[i] > peakLockThreshold &&
					amps[i] > amps[i-1] && amps[i] > amps[i+1] )
			{
				peaksBuffer[ sliIndex++ ] = i ;
				i=i+1;
			}
		}

		for( int i = LOWER_BIN_INDEX_THRESHOLD ; i < lastBinIndexMinusOne ; i++ )
		{
			if( amps[i] > peakLockThreshold &&
					amps[i] > amps[i-1] && amps[i] > amps[i+1] &&
					amps[i] >= amps[i-2] && amps[i] >= amps[ i+2 ] &&
					amps[i-1] >= amps[i-2] &&
					amps[i+1] >= amps[i+2] )
			{
				peaksBuffer[ sliIndex++ ] = i ;
				i=i+2;
			}
		}

		if( amps[ lastBinIndexMinusOne ] > peakLockThreshold &&
				amps[ lastBinIndexMinusOne ] > amps[ lastBinIndexMinusTwo ] &&
				amps[ lastBinIndexMinusOne ] > amps[ lastBinIndex ] &&
				amps[ lastBinIndexMinusOne ] >= amps[ lastBinIndexMinusThree ] )
		{
			peaksBuffer[ sliIndex++ ] = lastBinIndexMinusOne;
		}
		else
		{
			if( amps[ lastBinIndex ] > peakLockThreshold &&
					amps[ lastBinIndex ] >= amps[ lastBinIndexMinusOne ] &&
					amps[ lastBinIndex ] >= amps[ lastBinIndexMinusTwo ] )
			{
				// This is intentionally not the last bin index - as we don't have phase for the nyquist bin.
				peaksBuffer[ sliIndex++ ] = lastBinIndexMinusOne;
			}
		}
		peaksBuffer[ sliIndex ] = -1;
		curAnalFrame.numPeaksInPeaksBuffer[c] = sliIndex;
		return sliIndex ;
	}

	private final void getRegionsOfInfluence( final PvocDataFrame curAnalFrame, final int c )
	{
		final float[] amps = curAnalFrame.ampsSquared[c];
		final int[] peaksBuffer = curAnalFrame.peaksBuffer[c];
		final int[] peakBoundariesBuffer = curAnalFrame.peakBoundariesBuffer[c];
		final int[] binToPeakBuffer = curAnalFrame.binToPeakBuffer[c];
		int lowerBound = 0;
		int upperBound = -1;

		// Unrolled last index of loop
		final int lastTestIndex = curAnalFrame.numPeaksInPeaksBuffer[c] - 1;

		for( int i = 0 ; i < lastTestIndex ; i++ )
		{
			final int lockedBinNum = peaksBuffer[ i ];
			// Look for where the next peak is by finding the next "lock indicator" if there is one
			// If we don't have one (-1) then set the upper bound to be the final bin (but not nyquist bin)
			final int nextPeakBin = peaksBuffer[ i + 1 ];

			upperBound = findMinimaBetween( lockedBinNum, nextPeakBin, amps );

			// Fill in the bins to be "locked" to this one
			Arrays.fill( binToPeakBuffer, lowerBound, upperBound, lockedBinNum );
			peakBoundariesBuffer[ (lockedBinNum * 2) ] = lowerBound;
			peakBoundariesBuffer[ (lockedBinNum * 2) + 1 ] = upperBound;

			// Now move up lower bound
			lowerBound = upperBound;
		}

		final int lockedBinNum = peaksBuffer[ lastTestIndex ];
		upperBound = numBins;
		Arrays.fill( binToPeakBuffer, lowerBound, upperBound, lockedBinNum );
		peakBoundariesBuffer[ (lockedBinNum * 2) ] = lowerBound;
		peakBoundariesBuffer[ (lockedBinNum * 2) + 1 ] = upperBound;
	}

	private final int findMinimaBetween( final int lockedBinNum,
				final int nextBinNum,
				final float[] amps )
	{
		final int lockedBinNumPlusOne = lockedBinNum + 1;
		int retBin = -1;
		boolean foundIt = false;
		// Pull out first amp (on this maxima) and work out derivative
		// to next bin
		float prevAmp = amps[ lockedBinNum ];
		float curAmp = amps[ lockedBinNumPlusOne ];
		float derivative = prevAmp - curAmp;
//		float prevDerSig = Math.signum( derivative );
		float derivativeSignum = ( derivative < 0.0f ? -1.0f : 1.0f );
//		log.debug("Examining bin " + lockedBinNum + " prevAmp " + prevAmp + " and curAmp " + curAmp + " with der " + derivative );
//		log.debug("PrevDerSig is " + prevDerSig + " and derSig is " + derivativeSignum );

		boolean lastDerivativeWasNegative = false;
		int binAtFirstNegativeDerivative = -1;
		// Basically walk the amps from lockedBinNum to nextBinNum
		// until we hit a sign change in the derivative (e.g goes from pos -> negative )
		for( int i = lockedBinNumPlusOne ; i < nextBinNum ; i++ )
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
			else //if( derivativeSignum < 0 )
			{
				if( lastDerivativeWasNegative )
				{
					retBin = binAtFirstNegativeDerivative;
					foundIt = true;
					break;
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
		if( !foundIt )
		{
			if( binAtFirstNegativeDerivative != -1 )
			{
				return binAtFirstNegativeDerivative;
			}
			else
			{
				return( (lockedBinNum + nextBinNum) / 2 );
			}
		}
		else
		{
			return retBin;
		}
	}

	public final void quickZeroingLeaveBins( final PvocDataFrame curAnalFrame,
			final int channelNum,
			final int binsOnEachSide,
			final boolean cleanPeak,
			final boolean cleanAllBinsAbovePeaks,
			final boolean cleanAllBinsBelowPeaks )
	{
		final float[] amps = curAnalFrame.ampsSquared[channelNum];
		final int[] binToPeakBuffer = curAnalFrame.binToPeakBuffer[channelNum];
		final float[] complexFrame = curAnalFrame.complexFrame[channelNum];

		// Now zero all the bins that aren't marked as a peak (ignore dc and nyquist)
		amps[0] = 0.0f;
		amps[lastBinIndex] = 0.0f;
		for( int i = 1 ; i < lastBinIndex; i++ )
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
