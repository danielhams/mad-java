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

package test.uk.co.modularaudio.util.audio.oscillatortable;

import java.io.IOException;

import uk.co.modularaudio.util.audio.fileio.WavFileException;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;


public class TestWaveFileWriter
{
	private final WaveFileWriter myWriter;

	public TestWaveFileWriter( final String outFilename, final int numChannels, final int sampleRate, final long numFrames )
			throws IOException
	{
		if( numChannels != 1 )
		{
			throw new IOException("Only one channel supported");
		}
		myWriter = new WaveFileWriter( outFilename, numChannels, sampleRate, (short)32 );
	}

	public void writeFloats( final float[][] source, final int offset, final int length ) throws IOException, WavFileException
	{
		myWriter.writeFrames( source[0], offset, length );
	}

	public void writeDoubles( final double[][] source, final int offset, final int length ) throws IOException, WavFileException
	{
		myWriter.writeFramesDoubles( source[0], offset, length );
	}

	public void close() throws IOException
	{
		myWriter.close();
	}
}
