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

package uk.co.modularaudio.service.brokenaudiofileio;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiodatafetcher.AudioDataFetcherFactory;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.DynamicMetadata;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.util.audio.fileio.IAudioDataFetcher;
import uk.co.modularaudio.util.audio.fileio.IAudioDataFetcher.DetectedFormat;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.format.SampleBits;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class BrokenAudioFileIOService implements ComponentWithLifecycle, AudioFileIOService
{
	private static Log log = LogFactory.getLog( BrokenAudioFileIOService.class.getName() );

	private AudioFileIORegistryService audioFileIORegistryService;

	private final Set<AudioFileFormat> encodingFormats = new HashSet<AudioFileFormat>();
	private final Set<AudioFileFormat> decodingFormats = new HashSet<AudioFileFormat>();

	private final AudioDataFetcherFactory audioDataFetcherFactory = new AudioDataFetcherFactory();

	public BrokenAudioFileIOService()
	{
//		decodingFormats.add( AudioFileFormat.WAV );
		decodingFormats.add( AudioFileFormat.MP3 );
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( audioFileIORegistryService == null )
		{
			throw new ComponentConfigurationException( "Missing service dependencies. Please check configuration." );
		}
//		audioFileIORegistryService.registerAudioFileIOService( this );
	}

	@Override
	public void destroy()
	{
//		audioFileIORegistryService.unregisterAudioFileIOService( this );
	}

	public void setAudioFileIORegistryService( final AudioFileIORegistryService audioFileIORegistryService )
	{
		this.audioFileIORegistryService = audioFileIORegistryService;
	}

	@Override
	public Set<AudioFileFormat> listSupportedEncodingFormats()
	{
		return encodingFormats;
	}

	@Override
	public Set<AudioFileFormat> listSupportedDecodingFormats()
	{
		return decodingFormats;
	}

	@Override
	public AudioFileFormat sniffFileFormatOfFile( final String path )
			throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException
	{
		AudioFileFormat format = AudioFileFormat.UNKNOWN;
		try
		{
			final File inputFile = new File(path );
			final IAudioDataFetcher dataFetcher = audioDataFetcherFactory.getFetcherForFile( inputFile );
			dataFetcher.open( inputFile );

			final DetectedFormat dfFormat = dataFetcher.getDetectedFormat();
			switch( dfFormat )
			{
				case FLAC:
				{
					format = AudioFileFormat.FLAC;
					break;
				}
				case MP3:
				{
					format = AudioFileFormat.MP3;
					break;
				}
				default:
				{
					format = AudioFileFormat.UNKNOWN;
				}
			}
			dataFetcher.close();
			return format;
		}
		catch( final UnsupportedAudioFileException uafe )
		{
			throw uafe;
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught sniffing audio file format: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg );
		}
	}

	@Override
	public AudioFileHandleAtom openForWrite( final String path )
			throws DatastoreException, IOException
	{
		return null;
	}

	@Override
	public AudioFileHandleAtom openForRead( final String path )
			throws DatastoreException, IOException
	{
		InternalFileHandleAtom retVal = null;
		try
		{
			final File inputFile = new File(path );
			final IAudioDataFetcher dataFetcher = audioDataFetcherFactory.getFetcherForFile( inputFile );
			dataFetcher.open( inputFile );
			final DetectedFormat dfFormat = dataFetcher.getDetectedFormat();
			AudioFileFormat format;
			switch( dfFormat )
			{
				case FLAC:
				{
					format = AudioFileFormat.FLAC;
					break;
				}
				case MP3:
				{
					format = AudioFileFormat.MP3;
					break;
				}
				default:
				{
					format = AudioFileFormat.UNKNOWN;
				}
			}

			final DataRate dataRate = DataRate.fromFrequency( dataFetcher.getSampleRate() );
			final SampleBits sampleBits = SampleBits.SAMPLE_FLOAT;
			final int numChannels = dataFetcher.getNumChannels();
			final long numFloats = dataFetcher.getNumTotalFloats();
			final long numFrames = numFloats / numChannels;
			final StaticMetadata sm = new StaticMetadata( format,
					dataRate,
					sampleBits,
					numChannels,
					numFrames,
					path );

			retVal = new InternalFileHandleAtom( this,
					AudioFileDirection.DECODE,
					sm,
					dataFetcher );
			if( log.isTraceEnabled() )
			{
				log.trace( "Opened file handle " + path + " for reading");
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught opening for read: " + e.toString();
			log.error( msg, e );
		}
		return retVal;
	}

	@Override
	public void closeHandle( final AudioFileHandleAtom handle )
			throws DatastoreException, IOException
	{
		final InternalFileHandleAtom ifh = (InternalFileHandleAtom)handle;
		ifh.close();
		if( log.isTraceEnabled() )
		{
			log.trace( "Closed file handle " + ifh.getStaticMetadata().path );
		}
	}

	@Override
	public int readFrames( final AudioFileHandleAtom handle,
			final float[] destFloats,
			final int destPositionFrames,
			final int numFrames,
			final long frameReadOffset )
			throws DatastoreException, IOException
	{
		final InternalFileHandleAtom ifh = (InternalFileHandleAtom)handle;
		final int numRead = ifh.readFrames( destFloats, destPositionFrames, numFrames, frameReadOffset );
		if( numRead != numFrames )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Oops - asked for " + numFrames + " received " + numRead);
			}
		}
		return numRead;
	}

	@Override
	public int writeFrames( final AudioFileHandleAtom handle, final float[] srcFloats,
			final long writePosition, final int numFrames )
			throws DatastoreException, IOException
	{
		return 0;
	}

	@Override
	public DynamicMetadata readMetadata( final AudioFileHandleAtom handle )
			throws DatastoreException, IOException
	{
		final DynamicMetadata retVal = new DynamicMetadata();

		return retVal;
	}

	@Override
	public void writeMetadata( final AudioFileHandleAtom handle,
			final DynamicMetadata outDynamicMetadata )
			throws DatastoreException, IOException
	{
	}

}
