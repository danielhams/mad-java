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
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class TestCreateDCMonoFile extends TestCase
{
//	private static Log log = LogFactory.getLog( TestCreateDCMonoFile.class.getName() );

	private final static String OUTPUT_FILENAME = "tmpoutput/dc-3.1dB_48000_30secs_mono.wav";

	private final static float OUTPUT_LEVEL_DB = -3.1f;
	private final static float OUTPUT_LEVEL_AMP = AudioMath.dbToLevelF( OUTPUT_LEVEL_DB );

	private final static DataRate OUTPUT_SAMPLE_RATE = DataRate.SR_48000;

	public void testWriteAFile()
		throws Exception
	{
		final int sampleRate = OUTPUT_SAMPLE_RATE.getValue();
		final short bitsPerSample = 32;

		final File outputFile = new File(OUTPUT_FILENAME);
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();
		final WaveFileWriter outputWriter = new WaveFileWriter( outputFile.getAbsolutePath(),
				1,
				sampleRate,
				bitsPerSample );

		final int testDataLength = sampleRate * 30;
		final float[] testData = new float[ testDataLength ];
		for( int i = 0 ; i < testDataLength ; i++ )
		{
			testData[i] = OUTPUT_LEVEL_AMP;
		}

		outputWriter.writeFrames( testData, 0, testDataLength );

		outputWriter.close();
	}
}
