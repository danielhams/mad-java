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

package uk.co.modularaudio.util.audio.pvoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.pvoc.frame.PvocFrameRotatorPaddedTimeDomain;
import uk.co.modularaudio.util.audio.pvoc.support.PvocComplexPolarConverter;
import uk.co.modularaudio.util.audio.pvoc.support.PvocPhaseFrequencyConverter;
import uk.co.modularaudio.util.math.MathDefines;

public class PvocParameters
{
	private static Log log = LogFactory.getLog( PvocParameters.class.getName() );
	
	private int sampleRate;
	private int numChannels;
	private int windowLength;
	private int numOverlaps;
	private int numReals;
	private float peakLockThreshold;
	private FftWindow fftWindow;
	private PvocDebugger debugger;
	
	// Calculated parameters
	private int stepSize;
	private int numBins;
	private int fftSize;
	private int fftComplexArraySize;
	
	private PvocFrameRotatorPaddedTimeDomain frameRotator;
	private PvocFftComputer fftComputer;
	
	private PvocComplexPolarConverter complexPolarConverter;
	private PvocPhaseFrequencyConverter phaseFrequencyConverter;
	
	private float freqPerBin;
	private float[] binCenterFreqs;
	
	private float twoPiAnalysisStepsOverSampleRate;
	private float sampleRateOverTwoPiAnalysisSteps;
	
	public PvocParameters( DataRate outputRate,
			int numChannels,
			int windowLength,
			int numOverlaps,
			int numReals,
			float peakLockThreshold,
			FftWindow fftWindow,
			PvocDebugger pvocDebugger )
	{
		// Window length must be even for correct phase alignment
		assert( windowLength%2 == 0 );
		assert( windowLength <= numReals );

		this.sampleRate = outputRate.getValue();
		this.numChannels = numChannels;
		this.windowLength = windowLength;
		this.numOverlaps = numOverlaps;
		this.numReals = numReals;
		this.peakLockThreshold = peakLockThreshold;
		this.numBins = (numReals / 2 ) + 1;
		this.fftSize = numReals;
		this.fftWindow = fftWindow;
		this.fftComplexArraySize = numBins * 2;

		this.stepSize = windowLength / numOverlaps;
		
		freqPerBin = ((float)sampleRate)/numReals;
		binCenterFreqs = new float[ numBins ];
		for( int b = 0 ; b < numBins ; b++ )
		{
			binCenterFreqs[ b ] = b * freqPerBin;
		}
		log.debug("Last bin center freq is " + binCenterFreqs[ numBins - 1] );
		
		twoPiAnalysisStepsOverSampleRate = (MathDefines.TWO_PI_F * stepSize) / sampleRate;
		sampleRateOverTwoPiAnalysisSteps = sampleRate / (MathDefines.TWO_PI_F * stepSize);

		frameRotator = new PvocFrameRotatorPaddedTimeDomain( windowLength, fftSize );
		complexPolarConverter = new PvocComplexPolarConverter();
		fftComputer = new PvocFftComputer( fftSize,
				fftComplexArraySize );
		phaseFrequencyConverter = new PvocPhaseFrequencyConverter( binCenterFreqs,
				twoPiAnalysisStepsOverSampleRate,
				sampleRateOverTwoPiAnalysisSteps );
		this.debugger = pvocDebugger;
		
	}
	
	public static Log getLog()
	{
		return log;
	}

	public int getWindowLength()
	{
		return windowLength;
	}

	public int getNumOverlaps()
	{
		return numOverlaps;
	}

	public int getNumReals()
	{
		return numReals;
	}
	
	public int getFftSize()
	{
		return fftSize;
	}

	public FftWindow getFftWindow()
	{
		return fftWindow;
	}

	public int getStepSize()
	{
		return stepSize;
	}

	public int getNumBins()
	{
		return numBins;
	}

	public int getSampleRate()
	{
		return sampleRate;
	}

	public int getNumChannels()
	{
		return numChannels;
	}
	
	public PvocFftComputer getFftComputer()
	{
		return fftComputer;
	}
	
	public PvocFrameRotatorPaddedTimeDomain getFrameRotator()
	{
		return frameRotator;
	}
	
	public PvocComplexPolarConverter getComplexPolarConverter()
	{
		return complexPolarConverter;
	}
	
	public PvocPhaseFrequencyConverter getPhaseFrequencyConverter()
	{
		return phaseFrequencyConverter;
	}
	
	public PvocDebugger getDebugger()
	{
		return debugger;
	}
	
	public int getFftComplexArraySize()
	{
		return fftComplexArraySize;
	}

	public float getPeakLockThreshold()
	{
		return peakLockThreshold;
	}
}
