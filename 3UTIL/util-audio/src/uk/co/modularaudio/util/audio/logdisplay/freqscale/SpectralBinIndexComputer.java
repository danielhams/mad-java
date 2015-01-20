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

package uk.co.modularaudio.util.audio.logdisplay.freqscale;

import java.util.Arrays;

import uk.co.modularaudio.util.math.NormalisedValuesMapper;


public class SpectralBinIndexComputer
{
//	private final static Log log = LogFactory.getLog( SpectralBinIndexComputer.class.getName() );
	
	public static void computeBinIndexesForLogSpectrum( double mRate,
			int numBins,
			int numDisplayBins,
			int[] output )
	{
		int outputLength = numDisplayBins;
		
		double xMin = (mRate / numBins );
		double xMax = mRate;
		double xRatio = xMax / xMin;
		double xPos = xMin;
		
		double xStep = Math.pow(2.0, 
				Math.log( xRatio) / Math.log( 2.0 )  / outputLength );
		
		double[] computedFrequenciesForPixels = new double[ outputLength ];

		for( int i = 0 ; i < outputLength ; i++)
		{
			double computedFrequencyAtPos = xPos * xStep;
//			log.debug("For point " + i + " + computed freq " + computedFrequencyAtPos);
			computedFrequenciesForPixels[ i ] = computedFrequencyAtPos;
			double doubleBinIndex = (computedFrequencyAtPos * numBins) / mRate;
			output[ i ] = (int)doubleBinIndex;
			xPos *= xStep;
		}
	}
	
	public static void dhComputeBinIndexesForLogSpectrum( double sampleRate,
			int numFrequencyBins,
			int numOutputPoints,
			int[] output )
	{
		double freqMax = sampleRate / 2.0;
		double freqPerBin = freqMax / numFrequencyBins;
		double freqMin = freqPerBin / 2;
		double freqMinLg = Math.log10( freqMin + 10 );
		double freqMaxLg = Math.log10( freqMax + 10 );
		// Now we divide the freqMinLg -> freqMaxLg into N segments
		double howMuchPerPoint = (freqMaxLg - freqMinLg) / numOutputPoints;
		for( int i = 0 ; i < numOutputPoints ; i++ )
		{
			double freqForPoint = Math.pow(10, (freqMinLg + (i * howMuchPerPoint) ) ) - 10.0;
//			log.debug("For point " + i + " + computed freq " + freqForPoint);
			double doubleBinIndex = freqForPoint / freqPerBin;
			output[ i ] = (int)doubleBinIndex;
		}
	}
	
	public static void dhComputeBinIndexesForLogSpectrumSmoothBins( double sampleRate,
			int numFrequencyBins,
			int numOutputPoints,
			int[] output,
			int numBinsToSmooth)
	{
		dhComputeBinIndexesForLogSpectrum( sampleRate, numFrequencyBins, numOutputPoints, output );
		for( int i = 0 ; i < numOutputPoints ; i += numBinsToSmooth )
		{
			int from = i + 1;
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

	public static void computeBinIndexesForTestSpectrum( double sampleRate,
			int numFrequencyBins,
			int numOutputPoints,
			int[] output )
	{
		int maxBinIndex = numFrequencyBins - 1;
		double freqMax = sampleRate / 2.0;
		double freqPerBin = freqMax / numFrequencyBins;

		for( int i = 0 ; i < numOutputPoints ; i++ )
		{
			double normalisedPointVal = ((double)i  / (numOutputPoints-1));
			double normalisedExpPoint = NormalisedValuesMapper.expMinMaxMapD( normalisedPointVal, freqPerBin, freqMax );
			
			double freqForPoint = normalisedExpPoint * freqMax;
//			log.debug("For point " + i + " + computed freq " + freqForPoint);
			double doubleBinIndex = freqForPoint / freqPerBin;
			int intBinIndex = (int)doubleBinIndex;
			intBinIndex = (intBinIndex > maxBinIndex ? maxBinIndex : intBinIndex );
			output[ i ] = intBinIndex;
		}
		
	}
	
}
