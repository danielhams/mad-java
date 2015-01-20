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
	
//	private StftParameters parameters;

	private int numBins;
	private float binCenterFreqs[];
	private float freqPerBin;
	
	private float twoPiAnalysisStepsOverSampleRate;
	private float sampleRateOverTwoPiAnalysisSteps;
	
	public PhaseFrequencyConverter( StftParameters parameters )
	{
		numBins = parameters.getNumBins();
		
		freqPerBin = parameters.getFreqPerBin();
		binCenterFreqs = parameters.getBinCenterFreqs();
		
		twoPiAnalysisStepsOverSampleRate = parameters.getTwoPiAnalysisStepsOverSampleRate();
		sampleRateOverTwoPiAnalysisSteps = parameters.getSampleRateOverTwoPiAnalysisSteps();
	}

	public final float phaseToFreq( float phase,
			float oldPhase,
			int peakBinIndex )
	{
		float newPeakCenterFreq = binCenterFreqs[ peakBinIndex ];
		float expectedPhaseDiff = newPeakCenterFreq * twoPiAnalysisStepsOverSampleRate;
		float calculatedPhaseDiff = phase - oldPhase;
		
		float deltaPhaseDiff = expectedPhaseDiff - calculatedPhaseDiff;
		
		// Now normalise the deviation from the center to within +- PI
		deltaPhaseDiff = adjustRadiansToPlusMinusPiFloat( deltaPhaseDiff );
		
		float binTrueFreq = newPeakCenterFreq - (deltaPhaseDiff * sampleRateOverTwoPiAnalysisSteps);

		return binTrueFreq;
	}

	public final float crossBinPhaseToFreq( float newPeakPhase,
			float deadPeakOldPhase,
			int newPeakBinIndex,
			int deadPeakBinIndex )
	{
		float newPeakCenterFreq = binCenterFreqs[ newPeakBinIndex ];
		float expectedPhaseDiff = newPeakCenterFreq * twoPiAnalysisStepsOverSampleRate;
		float calculatedPhaseDiff = newPeakPhase - deadPeakOldPhase;
		
		float deltaPhaseDiff = expectedPhaseDiff - calculatedPhaseDiff;
		
		// Now normalise the deviation from the center to within +- PI
		deltaPhaseDiff = adjustRadiansToPlusMinusPiFloat( deltaPhaseDiff );

		float binTrueFreq = newPeakCenterFreq - (deltaPhaseDiff * sampleRateOverTwoPiAnalysisSteps);

		return binTrueFreq;
	}

	public final float freqToPhase(
			int synthStepSize,
			float twoPiSynthStepSizeOverSampleRate,
			float oldSynthPhase,
			float trueFrequency )
	{
		float advanceDueToLocalFreq = trueFrequency * twoPiSynthStepSizeOverSampleRate;

		float phase = oldSynthPhase + advanceDueToLocalFreq;

		phase = adjustRadiansToPlusMinusPiFloat( phase );

		return phase;
	}
	
	public final float crossBinFreqToPhase( int synthStepSize,
			float oldSynthPhaseDeadPeak,
			float trueFrequencyNewPeak,
			float twoPiSynthStepSizeOverSampleRate )
	{
		float advanceDueToLocalFreq = trueFrequencyNewPeak * twoPiSynthStepSizeOverSampleRate;

		float phase = oldSynthPhaseDeadPeak + advanceDueToLocalFreq;
		
		phase = adjustRadiansToPlusMinusPiFloat( phase );

		return phase;
	}
	
	public final void allPhaseToFreqArr(float[][] phasesArr,
			float[][] analPhasesArr,
			float[][] freqsArr )
	{
		for( int i = 0 ; i < phasesArr.length ; i++ )
		{
			allPhaseToFreq( phasesArr[i],
					analPhasesArr[i],
					freqsArr[i] );
		}
	}
	
	public final void allPhaseToFreq(float[] phases,
			float[] analPhases,
			float[] freqs )
	{
		for( int i = 1 ; i < numBins - 1 ; i++ )
		{
			freqs[ i ] = phaseToFreq( phases[i], analPhases[i], i );
		}
	}
	
	public final void allFreqToPhaseArr(
			int synthStepSize,
			float twoPiSynthStepSizeOverSampleRate,
			float[][] oldPhasesArr,
			float[][] phasesArr,
			float[][] freqsArr )
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
			int synthStepSize,
			float twoPiSynthStepSizeOverSampleRate,
			float[] oldPhases,
			float[] phases,
			float[] freqs )
	{
		for( int i = 1 ; i < numBins - 1 ; i++ )
		{
			phases[ i ] = freqToPhase( synthStepSize, twoPiSynthStepSizeOverSampleRate, oldPhases[ i ], freqs[ i ] );
		}
	}
	
	public final static float adjustRadiansToPlusMinusPiFloat( float inValue )
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

	public final static double adjustRadiansToPlusMinusPiDouble( double inValue )
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
	
	public final double radiansPmPiToDegrees( double radians )
	{
		return (radians / MathDefines.ONE_PI_D) * 180.0;
	}
	
	public final double radiansToDegrees( double radians )
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
