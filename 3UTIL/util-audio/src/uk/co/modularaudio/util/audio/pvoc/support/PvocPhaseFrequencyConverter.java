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

package uk.co.modularaudio.util.audio.pvoc.support;

import uk.co.modularaudio.util.math.MathDefines;


public strictfp class PvocPhaseFrequencyConverter
{
//	private static Log log = LogFactory.getLog( PvocPhaseFrequencyConverter.class.getName() );
	
	private float binCenterFreqs[];
	
	private float twoPiAnalysisStepsOverSampleRate;
	private float sampleRateOverTwoPiAnalysisSteps;
	
	public PvocPhaseFrequencyConverter( float[] binCenterFreqs,
			float twoPiAnalysisStepsOverSampleRate,
			float sampleRateOverTwoPiAnalysisSteps )
	{
		this.binCenterFreqs = binCenterFreqs;
		this.twoPiAnalysisStepsOverSampleRate = twoPiAnalysisStepsOverSampleRate;
		this.sampleRateOverTwoPiAnalysisSteps = sampleRateOverTwoPiAnalysisSteps;
	}

	public strictfp final float phaseToFreq( float phase,
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
		
//		log.debug("Converted bin " + peakBinIndex + " oldphase(" + oldPhase + "->" + phase +") to frequency " + binTrueFreq + " using peak center freq " +
//				newPeakCenterFreq + " and 2pianal/sample " + twoPiAnalysisStepsOverSampleRate );
		return binTrueFreq;
	}

	public strictfp final float crossBinPhaseToFreq( float newPeakPhase,
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
		
//		log.debug( "Converted cross bin (" + deadPeakBinIndex + "->" + newPeakBinIndex + ") to freq " + binTrueFreq );
		return binTrueFreq;
	}

	public strictfp final float freqToPhase( float twoPiSynthStepSizeOverSampleRate,
			float oldSynthPhase,
			float frequency )
	{
		float advanceDueToLocalFreq = frequency * twoPiSynthStepSizeOverSampleRate;

		float phase = oldSynthPhase + advanceDueToLocalFreq;

		phase = adjustRadiansToPlusMinusPiFloat( phase );
		
//		log.debug(  "Converted running freq " + frequency + " to phase " + phase );
		return phase;
	}
	
	public strictfp final float crossBinFreqToPhase( float twoPiSynthStepSizeOverSampleRate,
			float oldSynthPhaseDeadPeak,
			float frequency )
	{
		float advanceDueToLocalFreq = frequency * twoPiSynthStepSizeOverSampleRate;

		float phase = oldSynthPhaseDeadPeak + advanceDueToLocalFreq;
		
		phase = adjustRadiansToPlusMinusPiFloat( phase );

//		log.debug(  "Converted cross bin freq " + frequency + " to phase " + phase );
		return phase;
	}
	
	private strictfp final static float adjustRadiansToPlusMinusPiFloat( float inValue )
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
}
