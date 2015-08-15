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

public class StftDataFrameDouble
{
	public final int numChannels;
	public final double[][] complexFrame;
	public final double[][] amps;
	public final double[][] logAmps;
	public final double[][] phases;
	public final double[][] freqs;
	public final int[] dcSign;
	public final int[] nySign;

	public double inputScal;
	public double inputFac;

	public long position;

	public StftDataFrameDouble( final int numChannels, final int numReals,
			final int complexArraySize,
			final int numBins )
	{
		this.numChannels = numChannels;
		complexFrame = new double[numChannels][ complexArraySize ];
		amps = new double[numChannels][ numBins ];
		logAmps = new double[numChannels][ numBins ];
		phases = new double[numChannels][ numBins ];
		freqs = new double[numChannels][ numBins ];
		dcSign = new int[numChannels];
		nySign = new int[numChannels];
	}

}
