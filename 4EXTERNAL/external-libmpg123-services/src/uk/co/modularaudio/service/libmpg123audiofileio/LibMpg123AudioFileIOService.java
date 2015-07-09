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

package uk.co.modularaudio.service.libmpg123audiofileio;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.libmpg123wrapper.LibMpg123WrapperLoader;
import uk.co.modularaudio.libmpg123wrapper.swig.CArrayInt;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_int;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_mpg123_handle_struct;
import uk.co.modularaudio.libmpg123wrapper.swig.libmpg123;
import uk.co.modularaudio.libmpg123wrapper.swig.mpg123_errors;
import uk.co.modularaudio.libmpg123wrapper.swig.mpg123_param_flags;
import uk.co.modularaudio.libmpg123wrapper.swig.mpg123_parms;
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

public class LibMpg123AudioFileIOService implements ComponentWithLifecycle, AudioFileIOService
{
	private static Log log = LogFactory.getLog( LibMpg123AudioFileIOService.class.getName() );

	private AudioFileIORegistryService audioFileIORegistryService;

	private final static Set<AudioFileFormat> ENCODING_FORMATS = Collections.unmodifiableSet(
			new HashSet<AudioFileFormat>( Arrays.asList( new AudioFileFormat[] {
			} ) ) );
	private final static Set<AudioFileFormat> DECODING_FORMATS = Collections.unmodifiableSet(
			new HashSet<AudioFileFormat>( Arrays.asList( new AudioFileFormat[] {
					AudioFileFormat.MP3
			} ) ) );

	public final static int SEEK_SET = 0;
	public final static int SEEK_CUR = 1;
	public final static int SEEK_END = 2;

	public LibMpg123AudioFileIOService()
	{
		LibMpg123WrapperLoader.loadIt();
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( audioFileIORegistryService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}

		final int initSuccess = libmpg123.mpg123_init();
		if( initSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			final String msg = "Failed mpg123_init: " + initSuccess;
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}

		audioFileIORegistryService.registerAudioFileIOService( this );
	}

	@Override
	public void destroy()
	{
		audioFileIORegistryService.unregisterAudioFileIOService( this );

		libmpg123.mpg123_exit();
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

	private SWIGTYPE_p_mpg123_handle_struct openHandle( final String path )
		throws DatastoreException, UnsupportedAudioFileException
	{
		final CArrayInt errorInt = new CArrayInt( 1 );
		final SWIGTYPE_p_int error = errorInt.cast();
		final SWIGTYPE_p_mpg123_handle_struct handle = libmpg123.mpg123_new( null, error );
		final int errorValue = errorInt.getitem( 0 );
		boolean isOpen = false;
		boolean isMp3 = false;
		try
		{
			if( errorValue != mpg123_errors.MPG123_OK.swigValue() )
			{
				throw new DatastoreException("Error thrown in mpg123_new: " + errorValue );
			}

			libmpg123.mpg123_param( handle,
					mpg123_parms.MPG123_ADD_FLAGS,
					mpg123_param_flags.MPG123_FORCE_FLOAT.swigValue(),
					0.0f );

			final int openSuccess = libmpg123.mpg123_open( handle, path );

			if( openSuccess != mpg123_errors.MPG123_OK.swigValue() )
			{
				throw new DatastoreException( "Failed in mpg123_open: " + openSuccess );
			}
			isOpen = true;

			final int formatCheck = libmpg123.CheckFormat( handle );

			if( formatCheck != mpg123_errors.MPG123_OK.swigValue() )
			{
				throw new UnsupportedAudioFileException( "File is not an MP3: " + path );
			}

			final int scanSuccess = libmpg123.mpg123_scan( handle );

			if( scanSuccess != mpg123_errors.MPG123_OK.swigValue() )
			{
				throw new UnsupportedAudioFileException( "File failed mpg123_scan - not an MP3: " + path );
			}

			final long endSeek = libmpg123.mpg123_seek( handle, 0, SEEK_END );
			if( endSeek == 0 )
			{
				throw new UnsupportedAudioFileException( "File failed end seek - not an MP3" );
			}

			final long tellFrames =libmpg123.mpg123_tell( handle );
			if( tellFrames == 0 )
			{
				throw new UnsupportedAudioFileException( "File has tell of zero after scan - not an MP3" );
			}

			final long numFrames = libmpg123.mpg123_length( handle );
			if( numFrames == 0 )
			{
				throw new UnsupportedAudioFileException( "File has zero length after scan - guessing it's not an MP3" );
			}

			final long seekResult = libmpg123.mpg123_seek( handle, 0, SEEK_SET );

			if( seekResult != mpg123_errors.MPG123_OK.swigValue() )
			{
				throw new UnsupportedAudioFileException( "Filed failed mpg123_seek - not an MP3: " + path );
			}

			isMp3 = true;
		}
		finally
		{
			// Cleanup depends on if it's an mp3 or not.
			// If it isn't cleanup everything.
			if( isOpen && !isMp3 )
			{
				final int closeSuccess = libmpg123.mpg123_close( handle );
				if( closeSuccess != mpg123_errors.MPG123_OK.swigValue() )
				{
					if( log.isErrorEnabled() )
					{
						log.error("Failed during non-mp3 close: " + closeSuccess );
					}
				}
				libmpg123.mpg123_delete( handle );
			}
			errorInt.delete();
		}
		return handle;
	}

	private void closeHandle( final SWIGTYPE_p_mpg123_handle_struct handle )
	{
		final int closeSuccess = libmpg123.mpg123_close( handle );
		if( closeSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Failed during handle close: " + closeSuccess );
			}
		}
		libmpg123.mpg123_delete( handle );
	}

	@Override
	public AudioFileFormat sniffFileFormatOfFile( final String path )
			throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException
	{
		AudioFileFormat retVal = AudioFileFormat.UNKNOWN;

		final SWIGTYPE_p_mpg123_handle_struct handle = openHandle( path );
		retVal = AudioFileFormat.MP3;

		closeHandle( handle );

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

		String libraryPath = absPath;
		if( !FileUtilities.isRelativePath( libraryPath ) )
		{
			final String userMusicDir = audioFileIORegistryService.getUserMusicDir();
			if( libraryPath.startsWith( userMusicDir ) )
			{
				libraryPath = libraryPath.substring( userMusicDir.length() + 1 );
			}
		}

		SWIGTYPE_p_mpg123_handle_struct handle = null;
		try
		{
			handle = openHandle( absPath );

			// Get the metadata needed for the atom
			final long sampleRate = libmpg123.GetFormatSampleRate( handle );
			final int channels = libmpg123.GetFormatChannels( handle );
			final long numFrames = libmpg123.mpg123_length( handle );

			final StaticMetadata sm = new StaticMetadata( AudioFileFormat.MP3,
					DataRate.fromFrequency( (int)sampleRate ),
					SampleBits.SAMPLE_FLOAT,
					channels,
					numFrames,
					libraryPath );

			final LibMpg123Atom atom = new LibMpg123Atom( this, AudioFileDirection.DECODE, sm,
					handle );

			handle = null;

			return atom;

		}
		catch( final UnknownDataRateException e )
		{
			throw new DatastoreException( e );
		}
		finally
		{
			if( handle != null )
			{
				// We're failing, close the handle
				closeHandle( handle );
			}
		}
	}

	@Override
	public void closeHandle( final AudioFileHandleAtom handle ) throws DatastoreException, IOException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Closing open file handle \"" + handle.getStaticMetadata().path + "\"");
		}
		final LibMpg123Atom realAtom = (LibMpg123Atom)handle;
		final SWIGTYPE_p_mpg123_handle_struct mh = realAtom.handle;

		final int closeRv = libmpg123.mpg123_close( mh );

		if( closeRv != mpg123_errors.MPG123_OK.swigValue() )
		{
			final String msg = "Failed during mgp123_close: " + closeRv;
			log.error( msg );
			throw new DatastoreException( msg );
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
		final LibMpg123Atom realAtom = (LibMpg123Atom)handle;
		final SWIGTYPE_p_mpg123_handle_struct mh = realAtom.handle;

		final long currentPosition = realAtom.currentPosition;

		if( currentPosition != frameReadOffset )
		{
			// Need to seek
			final long setPosition = libmpg123.mpg123_seek( mh, frameReadOffset, SEEK_SET );

			if( setPosition != frameReadOffset )
			{
				throw new DatastoreException("Seek failed to move to desired(" +
						frameReadOffset + ") set(" + setPosition );
			}
		}

		final int numChannels = realAtom.getStaticMetadata().numChannels;

		final int numFloats = numFrames * numChannels;

		final int rv = libmpg123.DecodeData( mh,
				destFloats,
				destPositionFrames * numChannels,
				numFloats,
				realAtom.donePtr );

		final int numFloatsRead = realAtom.doneArray.getitem( 0 );

		if( rv == mpg123_errors.MPG123_DONE.swigValue() )
		{
			log.trace("Hit DONE.");
		}
		else if( rv == mpg123_errors.MPG123_NEW_FORMAT.swigValue() )
		{
			log.warn("mpg123 warns of new format... but continuing");
		}
		else if( rv != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException( "Failed during decode data: " + rv );
		}

		if( numFloatsRead != numFloats )
		{
			throw new IOException("Failed to read as many floats as we asked " +
					" for - asked(" + numFloats + ") read(" + numFloatsRead + ")");
		}

		final int numFramesRead = numFloatsRead / numChannels;

		realAtom.currentPosition += numFramesRead;

		return numFramesRead;
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
		return 1;
	}

}
