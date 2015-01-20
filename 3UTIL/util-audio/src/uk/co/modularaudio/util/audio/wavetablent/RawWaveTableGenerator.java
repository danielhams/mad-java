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

package uk.co.modularaudio.util.audio.wavetablent;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.io.FileUtils;


public abstract class RawWaveTableGenerator
{
	private static Log log = LogFactory.getLog( RawWaveTableGenerator.class.getName() );

	public CubicPaddedRawWaveTable readFromCacheOrGenerate( String cacheFileRoot, int cycleLength, int numHarmonics )
			throws IOException
	{
		int sampleRate = 44100;
		int numChannels = 1;
		short numBitsPerSample = 16;
		String uniqueName = getWaveTypeId() + "_l" + cycleLength + "_h" + numHarmonics + ".wav";

		CubicPaddedRawWaveTable retVal = null;

		// See if the file exists
		String pathToCachedWave = cacheFileRoot + File.separatorChar + uniqueName;
		File cachedWave = new File( pathToCachedWave );
		if( cachedWave.exists() )
		{
			WaveFileReader fileReader = new WaveFileReader( pathToCachedWave );
			long numTotalFloats = fileReader.getNumTotalFloats();
			int numTotalFloatsAsInt = (int)numTotalFloats;
			if( numTotalFloatsAsInt != numTotalFloats )
			{
				throw new RuntimeException( "Internal error re-reading cached waves" );
			}
			else if( numTotalFloatsAsInt != cycleLength + CubicPaddedRawWaveTable.NUM_EXTRA_SAMPLES_IN_BUFFER )
			{
				throw new RuntimeException( "The cached wave shape on disk doesn't match the size we expect" );
			}
			float[] data = new float[ numTotalFloatsAsInt ];
			fileReader.read( data, 0, 0, numTotalFloatsAsInt );
			fileReader.close();
			retVal = new CubicPaddedRawWaveTable( data );
		}
		else
		{
			log.info("Generating wave table for " + uniqueName + " - please be patient");
			retVal = reallyGenerateWaveTable( cycleLength, numHarmonics );
			// And write it out
			FileUtils.recursiveMakeDir( cacheFileRoot );
			String tmpPath = pathToCachedWave + ".tmp";
			WaveFileWriter fileWriter = new WaveFileWriter( tmpPath, numChannels, sampleRate, numBitsPerSample );
			fileWriter.writeFloats( retVal.buffer, retVal.bufferLength );
			fileWriter.close();

			File tmpFile = new File(tmpPath);
			boolean success = tmpFile.renameTo( new File(pathToCachedWave ) );
			if( !success )
			{
				String msg = "Failed moving temporary wave table cache file over to its final name";
				log.error( msg );
				throw new IOException( msg );
			}
		}

		return retVal;
	}

	public abstract String getWaveTypeId();
	public abstract CubicPaddedRawWaveTable reallyGenerateWaveTable( int cycleLength, int numHarmonics );
}
