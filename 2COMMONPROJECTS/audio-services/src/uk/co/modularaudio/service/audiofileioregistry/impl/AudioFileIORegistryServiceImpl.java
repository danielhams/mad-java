package uk.co.modularaudio.service.audiofileioregistry.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AudioFileIORegistryServiceImpl implements ComponentWithLifecycle, AudioFileIORegistryService
{
	private static Log log = LogFactory.getLog( AudioFileIORegistryServiceImpl.class.getName() );

	private final Map<AudioFileFormat, AudioFileIOService> formatToEncodingServiceMap =
			new HashMap<AudioFileFormat, AudioFileIOService>();
	private final Map<AudioFileFormat, AudioFileIOService> formatToDecodingServiceMap =
			new HashMap<AudioFileFormat, AudioFileIOService>();

	private final Set<AudioFileIOService> services = new HashSet<AudioFileIOService>();

	@Override
	public void registerAudioFileIOService( final AudioFileIOService audioFileIOService )
	{
		log.trace("Received a register of an audio file io service \"" + audioFileIOService.getClass().getSimpleName() + "\"");
		final Set<AudioFileFormat> encFormats = audioFileIOService.listSupportedEncodingFormats();
		final Set<AudioFileFormat> decFormats = audioFileIOService.listSupportedDecodingFormats();

		for( final AudioFileFormat serviceFormat : encFormats )
		{
			formatToEncodingServiceMap.put( serviceFormat, audioFileIOService );
			log.trace("Set service \"" + audioFileIOService.getClass().getSimpleName() + "\" as encoding handler for format: " +
					serviceFormat );
		}

		for( final AudioFileFormat serviceFormat : decFormats )
		{
			formatToDecodingServiceMap.put( serviceFormat, audioFileIOService );
			log.trace("Set service \"" + audioFileIOService.getClass().getSimpleName() + "\" as decoding handler for format: " +
					serviceFormat );
		}

		services.add( audioFileIOService );
	}

	@Override
	public void unregisterAudioFileIOService( final AudioFileIOService audioFileIOService )
	{
		log.trace("Received an unregister of an audio file io service \"" + audioFileIOService.getClass().getSimpleName() + "\"");
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
			log.trace("Removed service \"" + audioFileIOService.getClass().getSimpleName() + "\" as encoding handler for " +
					efr );
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
			log.trace("Removed service \"" + audioFileIOService.getClass().getSimpleName() + "\" as decoding handler for " +
					dfr );
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
				log.trace("Service \"" + oneService.getClass().getSimpleName() + "\" recognised format as " +
						foundFormat.toString() );
				if( retVal != AudioFileFormat.UNKNOWN )
				{
					return retVal;
				}
			}
			catch( final RecordNotFoundException rnfe )
			{
				log.trace("Service \"" + oneService.getClass().getSimpleName() + "\" threw rnfe for file " +
						path );
			}
			catch( final UnsupportedAudioFileException uafe )
			{
				log.trace("Service \"" + oneService.getClass().getSimpleName() + "\" threw uafe for file " +
						path );
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
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public AudioFileHandleAtom openFileForRead( final String path )
		throws DatastoreException, UnsupportedAudioFileException, IOException
	{
		AudioFileHandleAtom retVal = null;
		for( final AudioFileIOService oneService : services )
		{
			try
			{
				retVal = oneService.openForRead( path );
				break;
			}
			catch( final UnsupportedAudioFileException uafe )
			{
				log.trace("Service \"" + oneService.getClass().getSimpleName() + "\" threw uafe for file " +
						path );
			}
		}
		if( retVal == null )
		{
			throw new UnsupportedAudioFileException("Could not determine type of file \"" + path + "\"");
		}
		return retVal;
	}
}
