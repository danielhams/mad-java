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

package test.uk.co.modularaudio.util.audio.fileio;

import java.io.File;

import junit.framework.TestCase;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;

public class TestCreateDCMonoFile extends TestCase
{
//	private static Log log = LogFactory.getLog( TestCreateDCMonoFile.class.getName() );

	private final String outputFilename = "tmpoutput/dcmonooffset0_5.wav";

	public void testWriteAFile()
		throws Exception
	{
		final int sampleRate = 44100;
		final short bitsPerSample = 16;

		final File outputFile = new File(outputFilename);
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();
		final WaveFileWriter outputWriter = new WaveFileWriter( outputFile.getAbsolutePath(), 1, sampleRate, bitsPerSample );

		final int testDataLength = sampleRate;
		final float[] testData = new float[ testDataLength ];
		for( int i = 0 ; i < testDataLength ; i++ )
		{
			testData[i] = 0.5f;
		}

		outputWriter.writeFloats( testData, testDataLength );

		outputWriter.close();
	}
}
