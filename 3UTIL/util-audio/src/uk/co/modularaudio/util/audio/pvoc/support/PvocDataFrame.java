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

import java.util.Arrays;

public class PvocDataFrame
{
	public int numChannels = -1;
	public float[][] complexFrame;
	public float[][] amps;
	public float[][] ampsSquared;
	public float[][] phases;
	public int[] dcSign = null;
	public int[] nySign = null;
	
	// For peak peaking / history processors
	public int[][] peaksBuffer = null;
	public int[] numPeaksInPeaksBuffer = null;
	public int[][] binToPeakBuffer = null;
	public int[][] peakBoundariesBuffer = null;

	public PvocDataFrame( int numChannels, int numReals, int complexArraySize, int numBins )
	{
		this.numChannels = numChannels;
		
		complexFrame = new float[numChannels][ complexArraySize ];
		
		amps = new float[ numChannels ][ numBins ];
		ampsSquared = new float[ numChannels ][ numBins ];
		dcSign = new int[ numChannels ];
		nySign = new int[ numChannels ];
		phases = new float[ numChannels ][ numBins ];
		
		peaksBuffer = new int[ numChannels ][ numBins ];
		numPeaksInPeaksBuffer = new int[ numChannels ];
		Arrays.fill( numPeaksInPeaksBuffer, 0 );
		binToPeakBuffer = new int[ numChannels ][ numBins ];
		for( int c = 0 ; c < numChannels ; c++ )
		{
			Arrays.fill( peaksBuffer[ c ], -1 );
			Arrays.fill( binToPeakBuffer[ c ], -1 );
		}
		peakBoundariesBuffer = new int[ numChannels ][ numBins * 2 ];
	}

}
