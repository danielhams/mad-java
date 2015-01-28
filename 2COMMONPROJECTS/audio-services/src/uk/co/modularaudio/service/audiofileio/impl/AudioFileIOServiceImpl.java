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

package uk.co.modularaudio.service.audiofileio.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiodatafetcher.AudioDataFetcherFactory;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.DynamicMetadata;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.util.audio.fileio.IAudioDataFetcher;
import uk.co.modularaudio.util.audio.fileio.IAudioDataFetcher.DetectedFormat;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AudioFileIOServiceImpl implements ComponentWithLifecycle, AudioFileIOService
{
	private static Log log = LogFactory.getLog( AudioFileIOServiceImpl.class.getName() );

	private final Set<AudioFileFormat> encodingFormats = new HashSet<AudioFileFormat>();
	private final Set<AudioFileFormat> decodingFormats = new HashSet<AudioFileFormat>();

	private final AudioDataFetcherFactory audioDataFetcherFactory = new AudioDataFetcherFactory();

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
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
	public StaticMetadata sniffFileFormatOfFile( final String path )
			throws DatastoreException, RecordNotFoundException
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
			final int numChannels = dataFetcher.getNumChannels();
			final int sampleRate = dataFetcher.getSampleRate();
			final long numFloats = dataFetcher.getNumTotalFloats();
			final long numFrames = numFloats / numChannels;
			final StaticMetadata retVal = new StaticMetadata( format, numChannels, sampleRate, numFrames, path );
			dataFetcher.close();
			return retVal;
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

			final int numChannels = dataFetcher.getNumChannels();
			final int sampleRate = dataFetcher.getSampleRate();
			final long numFloats = dataFetcher.getNumTotalFloats();
			final long numFrames = numFloats / numChannels;
			final StaticMetadata sm = new StaticMetadata( format, numChannels, sampleRate, numFrames, path );

			retVal = new InternalFileHandleAtom( AudioFileDirection.DECODE, sm, dataFetcher );
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
	public void readFloats( final AudioFileHandleAtom handle, final float[] destFloats,
			final int destPosition, final int numFrames, final long frameReadOffset )
			throws DatastoreException, IOException
	{
		final InternalFileHandleAtom ifh = (InternalFileHandleAtom)handle;
		final int numRead = ifh.read( destFloats, destPosition, numFrames, frameReadOffset );
		if( numRead != numFrames )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Oops - asked for " + numFrames + " received " + numRead);
			}
		}
	}

	@Override
	public void writeFloats( final AudioFileHandleAtom handle, final float[] srcFloats,
			final long writePosition, final int numFrames )
			throws DatastoreException, IOException
	{
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
