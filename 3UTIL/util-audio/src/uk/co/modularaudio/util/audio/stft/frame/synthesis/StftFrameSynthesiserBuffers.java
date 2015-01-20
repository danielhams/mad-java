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

package uk.co.modularaudio.util.audio.stft.frame.synthesis;

public class StftFrameSynthesiserBuffers
{

	public float[][] ampsBuffer;
	public float[][] freqsBuffer;
	public float[][] phasesBuffer;
	
	public float[][] fftBuffer;
	
	public float[][] outputWindowBuffer;
	public float[][] outputStepBuffer;
	
	public StftFrameSynthesiserBuffers( int numChannels, int windowLength, int numReals, int complexArraySize, int numBins, int stepSize )
	{
		ampsBuffer = new float[ numChannels ][];
		freqsBuffer = new float[ numChannels ][];
		phasesBuffer = new float[ numChannels ][];
		fftBuffer = new float[ numChannels ][];
		outputWindowBuffer = new float[ numChannels ][];
		outputStepBuffer = new float[ numChannels ][];
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			ampsBuffer[chan] = new float[ numBins ];
			freqsBuffer[chan] = new float[ numBins ];
			phasesBuffer[chan] = new float[ numBins ];
			
			fftBuffer[chan] = new float[ complexArraySize ];
			
			outputWindowBuffer[chan] = new float[ windowLength ];
			// Must be large enough for a 10 times slow down given we might be resampling, too (pitch shifting)
			outputStepBuffer[chan] = new float[ stepSize * 10 ];
		}

	}

}
