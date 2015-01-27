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

package uk.co.modularaudio.util.audio.stft;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.math.MathDefines;

public class StftParameters
{
	private static Log log = LogFactory.getLog( StftParameters.class.getName() );

	private final DataRate outputRate;
	private final int sampleRate;
	private final int numChannels;
	private final int windowLength;
	private final int numOverlaps;
	private final int numReals;
	private final FftWindow fftWindow;

	// Calculated parameters
	private final int stepSize;
	private final int numBins;
	private final int complexArraySize;

	private final float freqPerBin;
	private final float[] binCenterFreqs;

	private final float twoPiAnalysisStepsOverSampleRate;
	private final float sampleRateOverTwoPiAnalysisSteps;

	public StftParameters( final DataRate outputRate,
			final int numChannels,
			final int windowLength,
			final int numOverlaps,
			final int numReals,
			final FftWindow fftWindow )
					throws StftException
	{
		// Window length must be even for correct phase alignment
		final int windowLengthTwosRemainder = windowLength % 2;
		boolean paramError = false;
		if( windowLengthTwosRemainder != 0 )
		{
			paramError = true;
		}
		if( windowLength > numReals )
		{
			paramError = true;
		}

		if( paramError )
		{
			throw new StftException( "Bad parameters to stft" );
		}

		this.outputRate = outputRate;
		this.sampleRate = outputRate.getValue();
		this.numChannels = numChannels;
		this.windowLength = windowLength;
		this.numOverlaps = numOverlaps;
		this.numReals = numReals;
		this.fftWindow = fftWindow;

		this.stepSize = windowLength / numOverlaps;
		this.numBins = (numReals / 2 ) + 1;
		this.complexArraySize = numBins * 2;

		freqPerBin = ((float)sampleRate)/numReals;
		binCenterFreqs = new float[ numBins ];
		for( int b = 0 ; b < numBins ; b++ )
		{
			binCenterFreqs[ b ] = b * freqPerBin;
//			log.debug("Bin " + b + " center frequency is " + binCenterFreqs[b] );
		}
//		log.debug("Last bin center freq is " + binCenterFreqs[ numBins - 1] );

		twoPiAnalysisStepsOverSampleRate = (MathDefines.TWO_PI_F * stepSize) / sampleRate;
		sampleRateOverTwoPiAnalysisSteps = sampleRate / (MathDefines.TWO_PI_F * stepSize);
	}

	public static Log getLog()
	{
		return log;
	}

	public DataRate getOutputRate()
	{
		return outputRate;
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

	public int getComplexArraySize()
	{
		return complexArraySize;
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

	public float getFreqPerBin()
	{
		return freqPerBin;
	}

	public float[] getBinCenterFreqs()
	{
		return binCenterFreqs;
	}

	public float getTwoPiAnalysisStepsOverSampleRate()
	{
		return twoPiAnalysisStepsOverSampleRate;
	}

	public float getSampleRateOverTwoPiAnalysisSteps()
	{
		return sampleRateOverTwoPiAnalysisSteps;
	}
}
