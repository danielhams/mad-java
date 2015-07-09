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

package uk.co.modularaudio.service.libsndfileaudiofileio;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.libsndfilewrapper.LibSndfileWrapperLoader;
import uk.co.modularaudio.libsndfilewrapper.swig.SF_INFO;
import uk.co.modularaudio.libsndfilewrapper.swig.SWIGTYPE_p_SNDFILE_tag;
import uk.co.modularaudio.libsndfilewrapper.swig.libsndfile;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.DynamicMetadata;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.util.atomicio.FileUtilities;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.format.SampleBits;
import uk.co.modularaudio.util.audio.format.UnknownDataRateException;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class LibSndfileAudioFileIOService implements ComponentWithLifecycle, AudioFileIOService
{
	private static Log log = LogFactory.getLog( LibSndfileAudioFileIOService.class.getName() );

	private AudioFileIORegistryService audioFileIORegistryService;

	private final static Set<AudioFileFormat> ENCODING_FORMATS = Collections.unmodifiableSet(
			new HashSet<AudioFileFormat>( Arrays.asList( new AudioFileFormat[] {
			} ) ) );
	private final static Set<AudioFileFormat> DECODING_FORMATS = Collections.unmodifiableSet(
			new HashSet<AudioFileFormat>( Arrays.asList( new AudioFileFormat[] {
					AudioFileFormat.WAV,
					AudioFileFormat.OGG,
					AudioFileFormat.FLAC,
					AudioFileFormat.AIFF
			} ) ) );

	public final static int SEEK_SET = 0;
	public final static int SEEK_CUR = 1;
	public final static int SEEK_END = 2;

	public LibSndfileAudioFileIOService()
	{
		LibSndfileWrapperLoader.loadIt();
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( audioFileIORegistryService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}

		audioFileIORegistryService.registerAudioFileIOService( this );
	}

	@Override
	public void destroy()
	{
		audioFileIORegistryService.unregisterAudioFileIOService( this );
	}

	@Override
	public Set<AudioFileFormat> listSupportedEncodingFormats()
	{
		return ENCODING_FORMATS;
	}

	@Override
	public Set<AudioFileFormat> listSupportedDecodingFormats()
	{
		return DECODING_FORMATS;
	}

	@Override
	public AudioFileFormat sniffFileFormatOfFile( final String path )
			throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException
	{
		AudioFileFormat retVal = AudioFileFormat.UNKNOWN;

		final SF_INFO sfInfo = new SF_INFO();

		SWIGTYPE_p_SNDFILE_tag sndfilePtr = null;

		try
		{
			sndfilePtr = libsndfile.sf_open( path, libsndfile.SFM_READ, sfInfo );

			if( sndfilePtr != null )
			{
				final int format = sfInfo.getFormat();
				retVal = decodeLibsndfileFormat( format );
			}
		}
		finally
		{
			if( sndfilePtr != null )
			{
				libsndfile.sf_close( sndfilePtr );
			}
			sfInfo.delete();
		}

		return retVal;
	}

	private static boolean checkOne( final int format, final int testFormat )
	{
		final int andedValue = format & testFormat;
		return( andedValue == testFormat );
	}

	private static AudioFileFormat decodeLibsndfileFormat( final int format )
	{
		AudioFileFormat retVal = AudioFileFormat.UNKNOWN;

		if( checkOne( format, libsndfile.SF_FORMAT_OGG ) )
		{
			retVal = AudioFileFormat.OGG;
		}
		else if( checkOne( format, libsndfile.SF_FORMAT_FLAC ) )
		{
			retVal = AudioFileFormat.FLAC;
		}
		else if( checkOne( format, libsndfile.SF_FORMAT_AIFF ) )
		{
			retVal = AudioFileFormat.AIFF;
		}
		else if( checkOne( format, libsndfile.SF_FORMAT_WAV ) )
		{
			retVal = AudioFileFormat.WAV;
		}
		return retVal;
	}

	@Override
	public AudioFileHandleAtom openForWrite( final String absPath ) throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		throw new DatastoreException("NI");
	}

	@Override
	public AudioFileHandleAtom openForRead( final String absPath ) throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Attempting to open \"" + absPath + "\"");
		}

		final SF_INFO sfInfo = new SF_INFO();

		SWIGTYPE_p_SNDFILE_tag sndfilePtr = null;
		try
		{
			sndfilePtr = libsndfile.sf_open( absPath, libsndfile.SFM_READ, sfInfo );

			final int format = sfInfo.getFormat();

			final AudioFileFormat aff = decodeLibsndfileFormat( format );
			if( aff == AudioFileFormat.UNKNOWN )
			{
				throw new UnsupportedAudioFileException("File format unsupported.");
			}
			final SampleBits sb = SampleBits.SAMPLE_FLOAT;
			final DataRate dataRate = DataRate.fromFrequency( sfInfo.getSamplerate() );
			final int numChannels = sfInfo.getChannels();
			final long numFrames = sfInfo.getFrames();

			String libraryPath = absPath;
			if( !FileUtilities.isRelativePath( libraryPath ) )
			{
				final String userMusicDir = audioFileIORegistryService.getUserMusicDir();
				if( libraryPath.startsWith( userMusicDir ) )
				{
					libraryPath = libraryPath.substring( userMusicDir.length() + 1 );
				}
			}

			final StaticMetadata sm = new StaticMetadata( aff, dataRate, sb, numChannels, numFrames, libraryPath );

			final LibSndfileAtom retVal = new LibSndfileAtom( this, AudioFileDirection.DECODE, sm,
					sfInfo,
					sndfilePtr );
			// We're good, set it to null so we don't close it.
			sndfilePtr = null;

			return retVal;
		}
		catch( final UnknownDataRateException udre )
		{
			final String msg = "UnknownDataRateException from sfInfo: " + udre.toString();
			throw new DatastoreException( msg, udre );
		}
		finally
		{
			if( sndfilePtr != null )
			{
				final int closeSuccess = libsndfile.sf_close( sndfilePtr );
				if( closeSuccess != 0 )
				{
					log.error("Failed in libsndfile close during cleanup");
				}
			}
			sfInfo.delete();
		}
	}

	@Override
	public void closeHandle( final AudioFileHandleAtom handle ) throws DatastoreException, IOException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Closing open file handle \"" + handle.getStaticMetadata().path + "\"");
		}
		final LibSndfileAtom realAtom = (LibSndfileAtom)handle;
		final SWIGTYPE_p_SNDFILE_tag sndfilePtr = realAtom.sndfilePtr;
		final int closeSuccess = libsndfile.sf_close( sndfilePtr );
		if( closeSuccess != 0 )
		{
			final String errMsg = libsndfile.sf_error_number( closeSuccess );
			throw new IOException("Failed libsndfile close of open audio file handle: " + errMsg);
		}
		realAtom.sfInfo.delete();
	}

	@Override
	public int readFrames( final AudioFileHandleAtom handle,
			final float[] destFloats,
			final int destPositionFrames,
			final int numFrames,
			final long frameReadOffset )
		throws DatastoreException, IOException
	{
		final LibSndfileAtom realAtom = (LibSndfileAtom)handle;
		if( realAtom.direction != AudioFileDirection.DECODE )
		{
			throw new DatastoreException( "readFloat called on encoding audio file atom." );
		}

		final SWIGTYPE_p_SNDFILE_tag sndfilePtr = realAtom.sndfilePtr;

		if( realAtom.currentHandleFrameOffset != frameReadOffset )
		{
			if( log.isTraceEnabled() )
			{
				log.trace("Current frame offset requires a seek from " + realAtom.currentHandleFrameOffset + " to " + frameReadOffset );
			}

			final long actualOffset = libsndfile.sf_seek( sndfilePtr, frameReadOffset, SEEK_SET );

			if( log.isTraceEnabled() )
			{
				log.trace("Actual offset is now " + actualOffset);
			}

			if( actualOffset != frameReadOffset )
			{
				final String msg = "The seek didn't produce expected offset - asked for " + frameReadOffset + " and got " +
						actualOffset;
				throw new IOException( msg );
			}
			realAtom.currentHandleFrameOffset = frameReadOffset;
		}

		final int numChannels = realAtom.getStaticMetadata().numChannels;
		final int destPositionFloats = destPositionFrames * numChannels;
		final int numFloats = numFrames * numChannels;
		final long numFloatsRead = libsndfile.CustomSfReadFloatsOffset( sndfilePtr, destFloats, destPositionFloats, numFloats );
		if( numFloatsRead != numFloats )
		{
			final String msg = "Reading after the seek didn't produce the expected num floats " +
					"asked for " + numFloats + " and read " + numFloatsRead;
			throw new IOException( msg );
		}

		realAtom.currentHandleFrameOffset += numFrames;

		return numFrames;
	}

	@Override
	public int writeFrames( final AudioFileHandleAtom handle, final float[] srcFloats, final long writePosition, final int numFrames )
			throws DatastoreException, IOException
	{
		throw new DatastoreException("NI");
	}

	@Override
	public DynamicMetadata readMetadata( final AudioFileHandleAtom handle ) throws DatastoreException, IOException
	{
		return new DynamicMetadata();
	}

	@Override
	public void writeMetadata( final AudioFileHandleAtom handle, final DynamicMetadata outDynamicMetadata )
			throws DatastoreException, IOException
	{
		throw new DatastoreException("NI");
	}

	public void setAudioFileIORegistryService( final AudioFileIORegistryService audioFileIORegistryService )
	{
		this.audioFileIORegistryService = audioFileIORegistryService;
	}

	@Override
	public int getFormatSniffPriority()
	{
		// Top priority since we recognise most file formats
		return 0;
	}

}
