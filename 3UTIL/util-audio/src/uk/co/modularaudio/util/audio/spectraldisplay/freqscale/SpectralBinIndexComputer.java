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

package uk.co.modularaudio.util.audio.spectraldisplay.freqscale;

import java.util.Arrays;

import uk.co.modularaudio.util.math.NormalisedValuesMapper;


public class SpectralBinIndexComputer
{
//	private final static Log log = LogFactory.getLog( SpectralBinIndexComputer.class.getName() );

	public static void computeBinIndexesForLogSpectrum( final double mRate,
			final int numBins,
			final int numDisplayBins,
			final int[] output )
	{
		final int outputLength = numDisplayBins;

		final double xMin = (mRate / numBins );
		final double xMax = mRate;
		final double xRatio = xMax / xMin;
		double xPos = xMin;

		final double xStep = Math.pow(2.0,
				Math.log( xRatio) / Math.log( 2.0 )  / outputLength );

		final double[] computedFrequenciesForPixels = new double[ outputLength ];

		for( int i = 0 ; i < outputLength ; i++)
		{
			final double computedFrequencyAtPos = xPos * xStep;
//			log.debug("For point " + i + " + computed freq " + computedFrequencyAtPos);
			computedFrequenciesForPixels[ i ] = computedFrequencyAtPos;
			final double doubleBinIndex = (computedFrequencyAtPos * numBins) / mRate;
			output[ i ] = (int)doubleBinIndex;
			xPos *= xStep;
		}
	}

	public static void dhComputeBinIndexesForLogSpectrum( final double sampleRate,
			final int numFrequencyBins,
			final int numOutputPoints,
			final int[] output )
	{
		final double freqMax = sampleRate / 2.0;
		final double freqPerBin = freqMax / numFrequencyBins;
		final double freqMin = freqPerBin / 2;
		final double freqMinLg = Math.log10( freqMin + 10 );
		final double freqMaxLg = Math.log10( freqMax + 10 );
		// Now we divide the freqMinLg -> freqMaxLg into N segments
		final double howMuchPerPoint = (freqMaxLg - freqMinLg) / numOutputPoints;
		for( int i = 0 ; i < numOutputPoints ; i++ )
		{
			final double freqForPoint = Math.pow(10, (freqMinLg + (i * howMuchPerPoint) ) ) - 10.0;
//			log.debug("For point " + i + " + computed freq " + freqForPoint);
			final double doubleBinIndex = freqForPoint / freqPerBin;
			output[ i ] = (int)doubleBinIndex;
		}
	}

	public static void dhComputeBinIndexesForLogSpectrumSmoothBins( final double sampleRate,
			final int numFrequencyBins,
			final int numOutputPoints,
			final int[] output,
			final int numBinsToSmooth)
	{
		dhComputeBinIndexesForLogSpectrum( sampleRate, numFrequencyBins, numOutputPoints, output );
		for( int i = 0 ; i < numOutputPoints ; i += numBinsToSmooth )
		{
			final int from = i + 1;
			int to = i + numBinsToSmooth;
			if( from >= numOutputPoints )
			{
				break;
			}
			if( to >= numOutputPoints )
			{
				to = numOutputPoints - 1;
			}
			Arrays.fill( output, from, to, output[i] );
		}
	}

	public static void computeBinIndexesForTestSpectrum( final double sampleRate,
			final int numFrequencyBins,
			final int numOutputPoints,
			final int[] output )
	{
		final int maxBinIndex = numFrequencyBins - 1;
		final double freqMax = sampleRate / 2.0;
		final double freqPerBin = freqMax / numFrequencyBins;

		for( int i = 0 ; i < numOutputPoints ; i++ )
		{
			final double normalisedPointVal = ((double)i  / (numOutputPoints-1));
			final double normalisedExpPoint = NormalisedValuesMapper.expMinMaxMapD( normalisedPointVal, freqPerBin, freqMax );

			final double freqForPoint = normalisedExpPoint * freqMax;
//			log.debug("For point " + i + " + computed freq " + freqForPoint);
			final double doubleBinIndex = freqForPoint / freqPerBin;
			int intBinIndex = (int)doubleBinIndex;
			intBinIndex = (intBinIndex > maxBinIndex ? maxBinIndex : intBinIndex );
			output[ i ] = intBinIndex;
		}

	}

}
