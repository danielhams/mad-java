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


public class LogarithmicFreqScaleComputer implements FrequencyScaleComputer
{
	private int curNumBins;
	private int curNumDisplayPoints;
	private int[] displayPointToBinIndex ;

	private boolean useSmoother;

	public LogarithmicFreqScaleComputer()
	{
	}

	public LogarithmicFreqScaleComputer( final boolean useSmoother )
	{
		this.useSmoother = useSmoother;
	}

	@Override
	public int displayBinToSpectraBin( final int numBins, final int numDisplayPoints, final int currentDisplayPoint)
	{
		checkScalerCorrect( numBins, numDisplayPoints, useSmoother );

		return displayPointToBinIndex[ currentDisplayPoint ];
	}

	private void checkScalerCorrect( final int numBins, final int numDisplayPoints, final boolean useSmoother )
	{
		if( curNumBins != numBins || curNumDisplayPoints != numDisplayPoints )
		{
			displayPointToBinIndex = new int[ numDisplayPoints ];
//			SpectralBinIndexComputer.computeBinIndexesForLogSpectrum( 44100.0, numBins, numDisplayPoints, displayPointToBinIndex );
			if( !useSmoother )
			{
				SpectralBinIndexComputer.dhComputeBinIndexesForLogSpectrum( 44100.0, numBins, numDisplayPoints, displayPointToBinIndex );
			}
			else
			{
				SpectralBinIndexComputer.dhComputeBinIndexesForLogSpectrumSmoothBins( 44100.0, numBins, numDisplayPoints, displayPointToBinIndex, 3 );
			}
			curNumBins = numBins;
			curNumDisplayPoints = numDisplayPoints;
		}
	}

	@Override
	public int spectraBinToDisplayBin( final int numBins, final int numDisplayPoints, final int currentSpectraPoint )
	{
		checkScalerCorrect( numBins, numDisplayPoints, useSmoother );

		// Now loop around until the currentSpectraPoint >= index
		boolean foundIt = false;
		int sp = 0;
		for( ; !foundIt && sp < numDisplayPoints ; sp++ )
		{
			if( displayPointToBinIndex[ sp ] >= currentSpectraPoint )
			{
				foundIt = true;
			}
		}

		return sp;
	}

}