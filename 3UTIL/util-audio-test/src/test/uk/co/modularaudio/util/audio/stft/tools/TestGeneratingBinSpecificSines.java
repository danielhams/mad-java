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

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;

public class TestGeneratingBinSpecificSines extends TestCase
{
//	private static Log log = LogFactory.getLog(  TestGeneratingBinSpecificSines.class.getName() );

	@Override
	protected void setUp() throws Exception
	{
	}

	@Override
	protected void tearDown() throws Exception
	{
	}

	public void testGenerateEm() throws Exception
	{
		final int numReals = 2048;
		final FloatFFT_1D outputFft = new FloatFFT_1D( numReals );

		final float[] fftArray = new float[ numReals ];

		final int[] binsToGenerate = new int[] { 2, 7, 17, 97, 98, 99, 100, 257 };

		for( final int bin : binsToGenerate )
		{
			Arrays.fill( fftArray, 0.0f );

			final int ampIndex = ( bin * 2 );
			final int phaseIndex = ( (bin * 2) + 1 );

			fftArray[ ampIndex ] = 100.0f;
			fftArray[ phaseIndex ] = 0.0f;

			outputFft.realInverse( fftArray, true );

			final String wavOutPath = "tmpoutput/fftbin_sine_" + bin + ".wav";
			final File waveOutputFile = new File(wavOutPath);
			final File parentDir = waveOutputFile.getParentFile();
			parentDir.mkdirs();
			final WaveFileWriter wfw = new WaveFileWriter( wavOutPath, 1, 44100, (short)16 );
			wfw.writeFloats( fftArray, numReals );
			wfw.close();
		}
	}

}
