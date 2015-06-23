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

package uk.co.modularaudio.service.audiofileioregistry.impl;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.atomicio.FileUtilities;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AudioFileIORegistryServiceImpl implements ComponentWithLifecycle, AudioFileIORegistryService
{
	private static Log log = LogFactory.getLog( AudioFileIORegistryServiceImpl.class.getName() );

	private UserPreferencesService userPreferencesService;

	private final Map<AudioFileFormat, AudioFileIOService> formatToEncodingServiceMap =
			new HashMap<AudioFileFormat, AudioFileIOService>();
	private final Map<AudioFileFormat, AudioFileIOService> formatToDecodingServiceMap =
			new HashMap<AudioFileFormat, AudioFileIOService>();

	private class AudioFileIOServiceComparator implements Comparator<AudioFileIOService>
	{

		@Override
		public int compare( final AudioFileIOService o1, final AudioFileIOService o2 )
		{
			final int o1p = o1.getFormatSniffPriority();
			final int o2p = o2.getFormatSniffPriority();

			if( o1p == o2p )
			{
				return o1.getClass().getName().compareTo( o2.getClass().getName() );
			}
			else
			{
				return o1p - o2p;
			}
		}
	}

	private final AudioFileIOServiceComparator afisc = new AudioFileIOServiceComparator();

	private final PriorityQueue<AudioFileIOService> services =
			new PriorityQueue<AudioFileIOService>( 2, afisc );

	@Override
	public void registerAudioFileIOService( final AudioFileIOService audioFileIOService )
	{
		if( log.isTraceEnabled() )
		{
			log.trace("Received a register of an audio file io service \"" + audioFileIOService.getClass().getSimpleName() + "\"");
		}
		final Set<AudioFileFormat> encFormats = audioFileIOService.listSupportedEncodingFormats();
		final Set<AudioFileFormat> decFormats = audioFileIOService.listSupportedDecodingFormats();

		for( final AudioFileFormat serviceFormat : encFormats )
		{
			formatToEncodingServiceMap.put( serviceFormat, audioFileIOService );
			if( log.isTraceEnabled() )
			{
				log.trace("Set service \"" + audioFileIOService.getClass().getSimpleName() + "\" as encoding handler for format: " +
						serviceFormat );
			}
		}

		for( final AudioFileFormat serviceFormat : decFormats )
		{
			formatToDecodingServiceMap.put( serviceFormat, audioFileIOService );
			if( log.isTraceEnabled() )
			{
				log.trace("Set service \"" + audioFileIOService.getClass().getSimpleName() + "\" as decoding handler for format: " +
						serviceFormat );
			}
		}

		services.add( audioFileIOService );
	}

	@Override
	public void unregisterAudioFileIOService( final AudioFileIOService audioFileIOService )
	{
		if( log.isTraceEnabled() )
		{
			log.trace("Received an unregister of an audio file io service \"" + audioFileIOService.getClass().getSimpleName() + "\"");
		}
		final Set<AudioFileFormat> encFormatsToRemove = new HashSet<AudioFileFormat>();
		for( final Map.Entry<AudioFileFormat, AudioFileIOService> e : formatToEncodingServiceMap.entrySet() )
		{
			if( e.getValue() == audioFileIOService )
			{
				encFormatsToRemove.add( e.getKey() );
			}
		}
		for( final AudioFileFormat efr : encFormatsToRemove )
		{
			formatToEncodingServiceMap.remove( efr );
			if( log.isTraceEnabled() )
			{
				log.trace("Removed service \"" + audioFileIOService.getClass().getSimpleName() + "\" as encoding handler for " +
						efr );
			}
		}

		final Set<AudioFileFormat> decFormatsToRemove = new HashSet<AudioFileFormat>();
		for( final Map.Entry<AudioFileFormat, AudioFileIOService> d : formatToDecodingServiceMap.entrySet() )
		{
			if( d.getValue() == audioFileIOService )
			{
				decFormatsToRemove.add( d.getKey() );
			}
		}
		for( final AudioFileFormat dfr : decFormatsToRemove )
		{
			formatToDecodingServiceMap.remove( dfr );
			if( log.isTraceEnabled() )
			{
				log.trace("Removed service \"" + audioFileIOService.getClass().getSimpleName() + "\" as decoding handler for " +
						dfr );
			}
		}

		services.remove( audioFileIOService );
	}

	@Override
	public AudioFileIOService getAudioFileIOServiceForFormatAndDirection( final AudioFileFormat format,
			final AudioFileDirection direction )
			throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException
	{
		switch( direction )
		{
			case ENCODE:
			{
				final AudioFileIOService fis = formatToEncodingServiceMap.get( format );
				if( fis == null )
				{
					throw new UnsupportedAudioFileException("File format not handled for encoding");
				}
				return fis;
			}
			case DECODE:
			default:
			{
				final AudioFileIOService fis = formatToDecodingServiceMap.get( format );
				if( fis == null )
				{
					throw new UnsupportedAudioFileException("File format not handled for decoding");
				}
				return fis;
			}
		}
	}

	@Override
	public AudioFileFormat sniffFileFormatOfFile( final String path )
			throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException
	{
		AudioFileFormat retVal = AudioFileFormat.UNKNOWN;

		for( final AudioFileIOService oneService : services )
		{
			try
			{
				final AudioFileFormat foundFormat = oneService.sniffFileFormatOfFile( path );
				retVal = foundFormat;
				if( retVal != AudioFileFormat.UNKNOWN )
				{
					if( log.isTraceEnabled() )
					{
						log.trace("Service \"" + oneService.getClass().getSimpleName() + "\" recognised format as " +
								foundFormat.toString() );
					}
					return retVal;
				}
			}
			catch( final RecordNotFoundException rnfe )
			{
				if( log.isTraceEnabled() )
				{
					log.trace("Service \"" + oneService.getClass().getSimpleName() + "\" threw rnfe for file " +
							path );
				}
			}
			catch( final UnsupportedAudioFileException uafe )
			{
				if( log.isTraceEnabled() )
				{
					log.trace("Service \"" + oneService.getClass().getSimpleName() + "\" threw uafe for file " +
							path );
				}
			}
		}
		if( retVal == AudioFileFormat.UNKNOWN )
		{
			throw new UnsupportedAudioFileException("Could not determine type of file \"" + path + "\"");
		}
		return retVal;
	}

	@Override
	public Set<AudioFileFormat> listSupportedEncodingFormats()
	{
		return formatToEncodingServiceMap.keySet();
	}

	@Override
	public Set<AudioFileFormat> listSupportedDecodingFormats()
	{
		return formatToDecodingServiceMap.keySet();
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( userPreferencesService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public AudioFileHandleAtom openFileForRead( final String path )
		throws DatastoreException, UnsupportedAudioFileException, IOException
	{
		AudioFileFormat format;
		AudioFileIOService fis;
		try
		{
			String audioFilePath = path;
			if( FileUtilities.isRelativePath( audioFilePath ) )
			{
				final String userMusicDir = userPreferencesService.getUserMusicDir();
				audioFilePath = userMusicDir + File.separatorChar + path;
			}

			format = sniffFileFormatOfFile( audioFilePath );
			fis = getAudioFileIOServiceForFormatAndDirection( format, AudioFileDirection.DECODE );

			return fis.openForRead( audioFilePath );
		}
		catch( final RecordNotFoundException e )
		{
			throw new IOException( e );
		}
	}

	public void setUserPreferencesService( final UserPreferencesService userPreferencesService )
	{
		this.userPreferencesService = userPreferencesService;
	}

	@Override
	public String getUserMusicDir()
	{
		return userPreferencesService.getUserMusicDir();
	}
}
