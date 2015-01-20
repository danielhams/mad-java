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

public class StftDataFrame
{
	public int numChannels = -1;
	public float[][] complexFrame;
	public float[][] amps;
	public float[][] logAmps;
	public float[][] phases;
	public float[][] freqs;
	public int[] dcSign = null;
	public int[] nySign = null;

	public float inputScal;
	public float inputFac;

	public long position = -1;
	
	public StftDataFrame( int numChannels, int numReals, int complexArraySize, int numBins )
	{
		this.numChannels = numChannels;
		complexFrame = new float[numChannels][ complexArraySize ];
		amps = new float[numChannels][ numBins ];
		logAmps = new float[numChannels][ numBins ];
		phases = new float[numChannels][ numBins ];
		freqs = new float[numChannels][ numBins ];
		dcSign = new int[numChannels];
		nySign = new int[numChannels];
	}

}
