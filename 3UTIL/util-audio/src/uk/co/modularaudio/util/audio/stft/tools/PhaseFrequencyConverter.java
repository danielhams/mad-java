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

import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.math.MathDefines;


public class PhaseFrequencyConverter
{
//	private static Log log = LogFactory.getLog( PhaseFrequencyConverter.class.getName() );

	private final int numBins;
	private final float binCenterFreqs[];
	private final float freqPerBin;

	private final float twoPiAnalysisStepsOverSampleRate;
	private final float sampleRateOverTwoPiAnalysisSteps;

	public PhaseFrequencyConverter( final StftParameters parameters )
	{
		numBins = parameters.getNumBins();

		freqPerBin = parameters.getFreqPerBin();
		binCenterFreqs = parameters.getBinCenterFreqs();

		twoPiAnalysisStepsOverSampleRate = parameters.getTwoPiAnalysisStepsOverSampleRate();
		sampleRateOverTwoPiAnalysisSteps = parameters.getSampleRateOverTwoPiAnalysisSteps();
	}

	public final float phaseToFreq( final float phase,
			final float oldPhase,
			final int peakBinIndex )
	{
		final float newPeakCenterFreq = binCenterFreqs[ peakBinIndex ];
		final float expectedPhaseDiff = newPeakCenterFreq * twoPiAnalysisStepsOverSampleRate;
		final float calculatedPhaseDiff = phase - oldPhase;

		float deltaPhaseDiff = expectedPhaseDiff - calculatedPhaseDiff;

		// Now normalise the deviation from the center to within +- PI
		deltaPhaseDiff = adjustRadiansToPlusMinusPiFloat( deltaPhaseDiff );

		final float binTrueFreq = newPeakCenterFreq - (deltaPhaseDiff * sampleRateOverTwoPiAnalysisSteps);

		return binTrueFreq;
	}

	public final float crossBinPhaseToFreq( final float newPeakPhase,
			final float deadPeakOldPhase,
			final int newPeakBinIndex,
			final int deadPeakBinIndex )
	{
		final float newPeakCenterFreq = binCenterFreqs[ newPeakBinIndex ];
		final float expectedPhaseDiff = newPeakCenterFreq * twoPiAnalysisStepsOverSampleRate;
		final float calculatedPhaseDiff = newPeakPhase - deadPeakOldPhase;

		float deltaPhaseDiff = expectedPhaseDiff - calculatedPhaseDiff;

		// Now normalise the deviation from the center to within +- PI
		deltaPhaseDiff = adjustRadiansToPlusMinusPiFloat( deltaPhaseDiff );

		final float binTrueFreq = newPeakCenterFreq - (deltaPhaseDiff * sampleRateOverTwoPiAnalysisSteps);

		return binTrueFreq;
	}

	public final float freqToPhase(
			final int synthStepSize,
			final float twoPiSynthStepSizeOverSampleRate,
			final float oldSynthPhase,
			final float trueFrequency )
	{
		final float advanceDueToLocalFreq = trueFrequency * twoPiSynthStepSizeOverSampleRate;

		float phase = oldSynthPhase + advanceDueToLocalFreq;

		phase = adjustRadiansToPlusMinusPiFloat( phase );

		return phase;
	}

	public final float crossBinFreqToPhase( final int synthStepSize,
			final float oldSynthPhaseDeadPeak,
			final float trueFrequencyNewPeak,
			final float twoPiSynthStepSizeOverSampleRate )
	{
		final float advanceDueToLocalFreq = trueFrequencyNewPeak * twoPiSynthStepSizeOverSampleRate;

		float phase = oldSynthPhaseDeadPeak + advanceDueToLocalFreq;

		phase = adjustRadiansToPlusMinusPiFloat( phase );

		return phase;
	}

	public final void allPhaseToFreqArr(final float[][] phasesArr,
			final float[][] analPhasesArr,
			final float[][] freqsArr )
	{
		for( int i = 0 ; i < phasesArr.length ; i++ )
		{
			allPhaseToFreq( phasesArr[i],
					analPhasesArr[i],
					freqsArr[i] );
		}
	}

	public final void allPhaseToFreq(final float[] phases,
			final float[] analPhases,
			final float[] freqs )
	{
		for( int i = 1 ; i < numBins - 1 ; i++ )
		{
			freqs[ i ] = phaseToFreq( phases[i], analPhases[i], i );
		}
	}

	public final void allFreqToPhaseArr(
			final int synthStepSize,
			final float twoPiSynthStepSizeOverSampleRate,
			final float[][] oldPhasesArr,
			final float[][] phasesArr,
			final float[][] freqsArr )
	{
		for( int i = 0 ; i < oldPhasesArr.length ; i++ )
		{
			allFreqToPhase( synthStepSize,
					twoPiSynthStepSizeOverSampleRate,
					oldPhasesArr[i],
					phasesArr[i],
					freqsArr[i] );
		}
	}

	public final void allFreqToPhase(
			final int synthStepSize,
			final float twoPiSynthStepSizeOverSampleRate,
			final float[] oldPhases,
			final float[] phases,
			final float[] freqs )
	{
		for( int i = 1 ; i < numBins - 1 ; i++ )
		{
			phases[ i ] = freqToPhase( synthStepSize, twoPiSynthStepSizeOverSampleRate, oldPhases[ i ], freqs[ i ] );
		}
	}

	public final static float adjustRadiansToPlusMinusPiFloat( final float inValue )
	{
		int qpd = (int)(inValue / MathDefines.ONE_PI_F );
		if( qpd >= 0 )
		{
			qpd += qpd & 1;
		}
		else
		{
			qpd -= qpd & 1;
		}
		return inValue - (MathDefines.ONE_PI_F * qpd );
	}

	public final static double adjustRadiansToPlusMinusPiDouble( final double inValue )
	{
		int qpd = (int)(inValue / MathDefines.ONE_PI_D );
		if( qpd >= 0 )
		{
			qpd += qpd & 1;
		}
		else
		{
			qpd -= qpd & 1;
		}
		return inValue - (MathDefines.ONE_PI_D * qpd );
	}

	public final double radiansPmPiToDegrees( final double radians )
	{
		return (radians / MathDefines.ONE_PI_D) * 180.0;
	}

	public final double radiansToDegrees( final double radians )
	{
		return ( radians / MathDefines.TWO_PI_D ) * 360.0;
	}

	public float[] getBinCenterFreqs()
	{
		return binCenterFreqs;
	}

	public float getFreqPerBin()
	{
		return freqPerBin;
	}
}
