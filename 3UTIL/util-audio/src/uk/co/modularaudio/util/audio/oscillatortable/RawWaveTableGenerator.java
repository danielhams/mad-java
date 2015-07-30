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

package uk.co.modularaudio.util.audio.oscillatortable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;


public abstract class RawWaveTableGenerator
{
	private static Log log = LogFactory.getLog( RawWaveTableGenerator.class.getName() );

	private final static Map<String, CubicPaddedRawWaveTable> nameToWaveTable = new HashMap<String, CubicPaddedRawWaveTable>();

	private final static ReentrantLock cacheLock = new ReentrantLock( true );

	public CubicPaddedRawWaveTable readFromCacheOrGenerate( final String cacheFileRoot, final int cycleLength, final int numHarmonics )
			throws IOException
	{
		cacheLock.lock();
		try
		{
			final int sampleRate = 44100;
			final int numChannels = 1;
			final short numBitsPerSample = 16; // NOPMD by dan on 29/01/15 16:30
			final String uniqueName = getWaveTypeId() + "_l" + cycleLength + "_h" + numHarmonics + ".wav";

			CubicPaddedRawWaveTable retVal = nameToWaveTable.get( uniqueName );
			if( retVal != null )
			{
				if( log.isTraceEnabled() )
				{
					log.trace( "Using existing mapped wave table for: " + uniqueName );
				}
				return retVal;
			}

			// See if the file exists
			final String pathToCachedWave = cacheFileRoot + File.separatorChar + uniqueName;
			final File cachedWave = new File( pathToCachedWave );
			if( cachedWave.exists() )
			{
				if( log.isTraceEnabled() )
				{
					log.trace( "Reusing existing file: " + pathToCachedWave );
				}

				final WaveFileReader fileReader = new WaveFileReader( pathToCachedWave );
				final long numTotalFloats = fileReader.getNumTotalFloats();
				final int numTotalFloatsAsInt = (int)numTotalFloats;
				if( numTotalFloatsAsInt != numTotalFloats )
				{
					throw new IOException( "Internal error re-reading cached waves" );
				}
				else if( numTotalFloatsAsInt != cycleLength + CubicPaddedRawWaveTable.NUM_EXTRA_SAMPLES_IN_BUFFER )
				{
					throw new IOException( "The cached wave shape length doesn't match the size we expect" );
				}
				final float[] data = new float[ numTotalFloatsAsInt ];
				fileReader.read( data, 0, 0, numTotalFloatsAsInt );
				fileReader.close();
				retVal = new CubicPaddedRawWaveTable( data );
				nameToWaveTable.put( uniqueName, retVal );
			}
			else
			{
				if( log.isInfoEnabled() )
				{
					log.info( "Generating wave table for " + uniqueName + " - please be patient" ); // NOPMD by dan on 01/02/15 07:11
				}
				// Ensure parent dir exists
				final File parent = new File(pathToCachedWave).getParentFile();
				if( !parent.exists() )
				{
					if( !parent.mkdirs() )
					{
						throw new IOException("Failed creating parent for wave table cache: " + parent.getAbsolutePath() );
					}
				}

				retVal = reallyGenerateWaveTable( cycleLength, numHarmonics );

				final String tmpPath = pathToCachedWave + ".tmp";
				final WaveFileWriter fileWriter = new WaveFileWriter( tmpPath, numChannels, sampleRate, numBitsPerSample );
				fileWriter.writeFloats( retVal.buffer, retVal.bufferLength );
				fileWriter.close();

				final File tmpFile = new File(tmpPath);
				final boolean success = tmpFile.renameTo( new File(pathToCachedWave ) );
				if( !success )
				{
					final String msg = "Failed moving temporary wave table cache file over to its final name";
					log.error( msg );
					throw new IOException( msg );
				}

				nameToWaveTable.put( uniqueName, retVal );
			}

			return retVal;
		}
		finally
		{
			cacheLock.unlock();
		}
	}

	public abstract String getWaveTypeId();
	public abstract CubicPaddedRawWaveTable reallyGenerateWaveTable( int cycleLength, int numHarmonics );
}
