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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;
import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftException;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.tools.ComplexPolarConverter;
import uk.co.modularaudio.util.math.MathDefines;


public abstract class RawWaveTableGenerator
{
	private static Log log = LogFactory.getLog( RawWaveTableGenerator.class.getName() );

	private final static Map<String, CubicPaddedRawWaveTable> NAME_TO_WAVE_TABLE = new HashMap<String, CubicPaddedRawWaveTable>();

	private final static ReentrantLock CACHE_LOCK = new ReentrantLock( true );

	public final static boolean GENERATE_USING_FFT = true;

	public CubicPaddedRawWaveTable readFromCacheOrGenerate( final String cacheFileRoot,
			final int cycleLength,
			final int numHarmonics )
			throws IOException
	{
		CACHE_LOCK.lock();
		try
		{
			final int sampleRate = 44100;
			final int numChannels = 1;
			final short numBitsPerSample = 32; // NOPMD by dan on 29/01/15 16:30
			final String uniqueName = getWaveTypeId() + "_l" + cycleLength + "_h" + numHarmonics + ".wav";

			CubicPaddedRawWaveTable retVal = NAME_TO_WAVE_TABLE.get( uniqueName );
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
				final int discoveredNumChannels = fileReader.getNumChannels();
				if( discoveredNumChannels != numChannels )
				{
					throw new IOException("Mismatch on num channels");
				}
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
				fileReader.readFrames( data, 0, 0, numTotalFloatsAsInt );
				fileReader.close();
				retVal = new CubicPaddedRawWaveTable( data );
				NAME_TO_WAVE_TABLE.put( uniqueName, retVal );
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

				if( GENERATE_USING_FFT )
				{
					retVal = generateWaveTableInverseFft( cycleLength, numHarmonics );
				}
				else
				{
					retVal = generateWaveTableAdditiveFourier( cycleLength, numHarmonics );
				}

				final String tmpPath = pathToCachedWave + ".tmp";
				final WaveFileWriter fileWriter = new WaveFileWriter( tmpPath, numChannels, sampleRate, numBitsPerSample );
				fileWriter.writeFrames( retVal.buffer, 0, retVal.bufferLength );
				fileWriter.close();

				final File tmpFile = new File(tmpPath);
				final boolean success = tmpFile.renameTo( new File(pathToCachedWave ) );
				if( !success )
				{
					final String msg = "Failed moving temporary wave table cache file over to its final name";
					log.error( msg );
					throw new IOException( msg );
				}

				NAME_TO_WAVE_TABLE.put( uniqueName, retVal );
			}

			return retVal;
		}
		catch( final StftException se )
		{
			throw new IOException( se );
		}
		finally
		{
			CACHE_LOCK.unlock();
		}
	}

	public abstract String getWaveTypeId();
	public abstract CubicPaddedRawWaveTable generateWaveTableAdditiveFourier( int cycleLength, int numHarmonics );
	public abstract RawLookupTable getHarmonics( int numHarmonics );
	public abstract float getPhase();

	public CubicPaddedRawWaveTable generateWaveTableInverseFft( final int cycleLength, final int numHarmonics )
			throws StftException
	{
		final int fftRealLength = cycleLength;
		final FftWindow emptyFftWindow = null;
		final StftParameters stftParams = new StftParameters( DataRate.CD_QUALITY,
				1,
				fftRealLength,
				4,
				fftRealLength,
				emptyFftWindow );

		final int numBins = stftParams.getNumBins();
		final int fftComplexArraySize = stftParams.getComplexArraySize();

		final ComplexPolarConverter cpc = new ComplexPolarConverter( stftParams );

		final FloatFFT_1D fftEngine = new FloatFFT_1D( fftRealLength );

		final StftDataFrame dataFrame = new StftDataFrame( 1, fftRealLength, fftComplexArraySize, numBins );
		Arrays.fill( dataFrame.amps[0], 0.0f );
		Arrays.fill( dataFrame.phases[0], 0.0f );
		Arrays.fill( dataFrame.complexFrame[0], 0.0f );

		final int startBin = 1;

		final RawLookupTable harmonics = getHarmonics( numHarmonics );
		final float phase = getPhase() * MathDefines.TWO_PI_F;

		// What's used in the fourier generator
		for( int i = 0 ; i < numHarmonics ; ++i )
		{
			// Every other bin gets a peak
			final int harmonicIndex = i;
			final int binToFill = startBin + harmonicIndex;
			final float ampOfBin = harmonics.floatBuffer[ harmonicIndex ] * (fftRealLength / 2);

			dataFrame.amps[0][binToFill] = ampOfBin;
			dataFrame.phases[0][binToFill] = phase;
		}

		// Convert back to complex form
		cpc.polarToComplex( dataFrame );
		fftEngine.realInverse( dataFrame.complexFrame[0], true );

		final CubicPaddedRawWaveTable retVal = new CubicPaddedRawWaveTable( cycleLength );
		System.arraycopy( dataFrame.complexFrame[0], 0, retVal.buffer, 1, fftRealLength );
		retVal.completeCubicBufferFillAndNormalise();

		return retVal;
	}
}
