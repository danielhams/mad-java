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

import java.io.File;
import java.util.HashMap;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.library.LibraryService;
import uk.co.modularaudio.service.library.vos.LibraryEntry;
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

	private LibraryService libraryService = null;
	private AudioFileIOService audioFileIOService = null;
	private ConfigurationService configurationService = null;

	private boolean enabled = false;

	private SampleCache sampleCache = null;

	// Block buffering configuration
	private BlockBufferingConfiguration blockBufferingConfiguration = null;

	@Override
	public void init() throws ComponentConfigurationException
	{
		HashMap<String, String> errors = new HashMap<String, String>();
		enabled = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_ENABLED, errors );
		int maxBlocksToBuffer = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_MAX_BLOCKS, errors );
		int blockLengthInFloats = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_LENGTH, errors );
		float minSecsBeforePosition = ConfigurationServiceHelper.checkForFloatKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_MIN_SECS_BEFORE, errors );
		float minSecsAfterPosition = ConfigurationServiceHelper.checkForFloatKey( configurationService, CONFIG_KEY_BLOCK_BUFFER_MIN_SECS_AFTER, errors );
		ConfigurationServiceHelper.errorCheck( errors );

		blockBufferingConfiguration = new BlockBufferingConfiguration( maxBlocksToBuffer,
				blockLengthInFloats,
				minSecsBeforePosition,
				minSecsAfterPosition );

		sampleCache = new SampleCache( audioFileIOService, blockBufferingConfiguration );
		sampleCache.init( enabled );
	}

	@Override
	public void destroy()
	{
		sampleCache.destroy();
	}

	public LibraryService getLibraryService()
	{
		return libraryService;
	}

	public void setLibraryService( LibraryService libraryService )
	{
		this.libraryService = libraryService;
	}

	public AudioFileIOService getAudioFileIOService()
	{
		return audioFileIOService;
	}

	public void setAudioFileIOService( AudioFileIOService audioFileIOService )
	{
		this.audioFileIOService = audioFileIOService;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public SampleCacheClient registerCacheClientForFile( String path ) throws NoSuchHibernateSessionException, DatastoreException, UnsupportedAudioFileException
	{
		InternalSampleCacheClient internalClient;
		try
		{
			// Find or add to library
			File sampleFile = new File( path );
			LibraryEntry libraryEntry = null;
			try
			{
				libraryEntry = libraryService.findLibraryEntryByFile( sampleFile );
			}
			catch(RecordNotFoundException rnfe )
			{
				libraryEntry = libraryService.addFileToLibrary( sampleFile );
			}

			internalClient = new InternalSampleCacheClient( libraryEntry, 0L, 0L );
			sampleCache.addClient( internalClient );
		}
		catch (Exception e)
		{
			String msg = "Exception caught registering cache client for file " + path + ": " + e.toString();
			throw new DatastoreException( msg, e );
		}

		log.debug( "Registered sample caching client for " + internalClient.getLibraryEntry().getLocation() );
		return internalClient;
	}

	@Override
	public void unregisterCacheClientForFile( SampleCacheClient client ) throws DatastoreException, RecordNotFoundException
	{
		InternalSampleCacheClient internalClient = (InternalSampleCacheClient)client;
		sampleCache.removeClient( internalClient );
		log.debug( "Unregistered sample caching client for " + internalClient.getLibraryEntry().getLocation() );

	}

	public BlockBufferingConfiguration getBlockBufferingConfiguration()
	{
		return blockBufferingConfiguration;
	}

	@Override
	public RealtimeMethodReturnCodeEnum readSamplesForCacheClient( SampleCacheClient client,
			float[] outputSamples,
			int outputFramePos,
			int numFrames )
	{
		InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		return sampleCache.readSamplesForCacheClient( iscc, outputSamples, outputFramePos, iscc.getCurrentFramePosition(), numFrames );
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
			SampleCacheClient client,
			long framePosition,
			int numFrames,
			SampleAcceptor sampleAcceptor)
	{
		InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		return sampleCache.readSamplesInBlocksForCacheClient( iscc, framePosition, numFrames, sampleAcceptor );
	}

	@Override
	public String getSampleFileTitleForCacheClient( SampleCacheClient client )
	{
		InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		LibraryEntry le = iscc.getLibraryEntry();
		return le.getTitle();
	}

	@Override
	public void registerForBufferFillCompletion(SampleCacheClient client,
			BufferFillCompletionListener completionListener)
	{
		InternalSampleCacheClient iscc = (InternalSampleCacheClient)client;
		sampleCache.registerForBufferFillCompletion( iscc,
				completionListener );
	}

	@Override
	public void addJobToCachePopulationThread()
	{
		sampleCache.addJobForPopulationThread();
	}
}
