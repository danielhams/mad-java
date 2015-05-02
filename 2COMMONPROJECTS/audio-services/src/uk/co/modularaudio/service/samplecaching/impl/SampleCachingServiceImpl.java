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

package uk.co.modularaudio.service.samplecaching.impl;

import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.library.LibraryEntry;
import uk.co.modularaudio.service.library.LibraryService;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleAcceptor;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SampleCachingServiceImpl implements ComponentWithLifecycle, SampleCachingService
{
	private static Log log = LogFactory.getLog( SampleCachingServiceImpl.class.getName() );

	private static final String CONFIG_KEY_ENABLED = SampleCachingServiceImpl.class.getSimpleName() + ".Enabled";
	private static final String CONFIG_KEY_BLOCK_BUFFER_MAX_BLOCKS = SampleCachingServiceImpl.class.getSimpleName() +".BlockBufferMaxBlocks";
	private static final String CONFIG_KEY_BLOCK_BUFFER_LENGTH = SampleCachingServiceImpl.class.getSimpleName() +".BlockBufferLength";
	private static final String CONFIG_KEY_BLOCK_BUFFER_MIN_SECS_BEFORE = SampleCachingServiceImpl.class.getSimpleName() +".BlockBufferMinSecsBefore";
	private static final String CONFIG_KEY_BLOCK_BUFFER_MIN_SECS_AFTER = SampleCachingServiceImpl.class.getSimpleName() +".BlockBufferMinSecsAfter";

	private LibraryService libraryService;
	private AudioFileIORegistryService audioFileIORegistryService;
	private ConfigurationService configurationService;

	private boolean enabled;

	private SampleCache sampleCache;

	// Block buffering configuration
	private BlockBufferingConfiguration blockBufferingConfiguration;

	@Override
	public void init() throws ComponentConfigurationException
	{
		final HashMap<String, String> errors = new HashMap<String, String>();
		enabled = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_ENABLED, errors );
		final int maxBlocksToBuffer = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_MAX_BLOCKS, errors );
		final int blockLengthInFloats = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_LENGTH, errors );
		final float minSecsBeforePosition = ConfigurationServiceHelper.checkForFloatKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_MIN_SECS_BEFORE, errors );
		final float minSecsAfterPosition = ConfigurationServiceHelper.checkForFloatKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_MIN_SECS_AFTER, errors );
		ConfigurationServiceHelper.errorCheck( errors );

		blockBufferingConfiguration = new BlockBufferingConfiguration( maxBlocksToBuffer,
				blockLengthInFloats,
				minSecsBeforePosition,
				minSecsAfterPosition );

		sampleCache = new SampleCache( blockBufferingConfiguration );
		sampleCache.init( enabled );
	}

	@Override
	public void destroy()
	{
		sampleCache.destroy();
	}

	public void setLibraryService( final LibraryService libraryService )
	{
		this.libraryService = libraryService;
	}

	public void setAudioFileIORegistryService( final AudioFileIORegistryService audioFileIORegistryService )
	{
		this.audioFileIORegistryService = audioFileIORegistryService;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public SampleCacheClient registerCacheClientForFile( final String path )
		throws NoSuchHibernateSessionException, DatastoreException, IOException, UnsupportedAudioFileException
	{
		InternalSampleCacheClient internalClient = null;

		final AudioFileHandleAtom afha = audioFileIORegistryService.openFileForRead( path );
		try
		{
			// Find or add to library
			LibraryEntry libraryEntry = null;
			try
			{
				libraryEntry = libraryService.findLibraryEntryByAudioFile( afha );
			}
			catch(final RecordNotFoundException rnfe )
			{
				libraryEntry = libraryService.addAudioFileToLibrary( afha );
			}

			internalClient = new InternalSampleCacheClient( libraryEntry, 0L, 0L );
			sampleCache.addClient( internalClient, afha );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught registering cache client for file " + path + ": " + e.toString();
			throw new DatastoreException( msg, e );
		}

		if( log.isDebugEnabled() )
		{
			log.debug( "Registered sample caching client for " + internalClient.getLibraryEntry().getLocation() );
		}
		return internalClient;
	}

	@Override
	public void unregisterCacheClientForFile( final SampleCacheClient client ) throws DatastoreException, RecordNotFoundException
	{
		final InternalSampleCacheClient internalClient = (InternalSampleCacheClient)client;
		sampleCache.removeClient( internalClient );
		if( log.isDebugEnabled() )
		{
			log.debug( "Unregistered sample caching client for " + internalClient.getLibraryEntry().getLocation() );
		}
	}

	public BlockBufferingConfiguration getBlockBufferingConfiguration()
	{
		return blockBufferingConfiguration;
	}

	@Override
	public RealtimeMethodReturnCodeEnum readSamplesForCacheClient( final SampleCacheClient client,
			final float[] outputSamples,
			final int outputArrayPos,
			final int numFrames )
	{
		final InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		return sampleCache.readSamplesForCacheClient( iscc, outputSamples, outputArrayPos, iscc.getCurrentFramePosition(), numFrames );
	}

	@Override
	public void dumpSampleCacheToLog()
	{
		log.debug("Sample cache block buffering configuration:");
		log.debug( blockBufferingConfiguration.toString() );
		sampleCache.dumpDetails();
	}

	@Override
	public RealtimeMethodReturnCodeEnum readSamplesInBlocksForCacheClient(
			final SampleCacheClient client,
			final long framePosition,
			final int numFrames,
			final SampleAcceptor sampleAcceptor)
	{
		final InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		return sampleCache.readSamplesInBlocksForCacheClient( iscc, framePosition, numFrames, sampleAcceptor );
	}

	@Override
	public String getSampleFileTitleForCacheClient( final SampleCacheClient client )
	{
		final InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		final LibraryEntry le = iscc.getLibraryEntry();
		return le.getTitle();
	}

	@Override
	public void registerForBufferFillCompletion( final SampleCacheClient client,
			final BufferFillCompletionListener completionListener )
	{
		final InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		sampleCache.registerForBufferFillCompletion( iscc,
				completionListener );
	}

	@Override
	public void addJobToCachePopulationThread()
	{
		sampleCache.addJobForPopulationThread();
	}
}
