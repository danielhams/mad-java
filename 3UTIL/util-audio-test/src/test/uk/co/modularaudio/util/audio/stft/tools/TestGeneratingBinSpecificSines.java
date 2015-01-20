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

package test.uk.co.modularaudio.util.audio.stft.tools;

import java.util.Arrays;

import junit.framework.TestCase;

import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;

public class TestGeneratingBinSpecificSines extends TestCase
{
//	private static Log log = LogFactory.getLog(  TestGeneratingBinSpecificSines.class.getName() );

	protected void setUp() throws Exception
	{
	}

	protected void tearDown() throws Exception
	{
	}
	
	public void testGenerateEm() throws Exception
	{
		int numReals = 2048;
		FloatFFT_1D outputFft = new FloatFFT_1D( numReals );
		
		float[] fftArray = new float[ numReals ];
		
		int[] binsToGenerate = new int[] { 2, 7, 17, 97, 98, 99, 100, 257 };
		
		for( int bin : binsToGenerate )
		{
			Arrays.fill( fftArray, 0.0f );
			
			int ampIndex = ( bin * 2 );
			int phaseIndex = ( (bin * 2) + 1 );
			
			fftArray[ ampIndex ] = 100.0f;
			fftArray[ phaseIndex ] = 0.0f;
			
			outputFft.realInverse( fftArray, true );
			
			String wavOutPath = "fftbin_sine_" + bin + ".wav";
			WaveFileWriter wfw = new WaveFileWriter( wavOutPath, 1, 44100, (short)16 );
			wfw.writeFloats( fftArray, numReals );
			wfw.close();
		}
	}

}
