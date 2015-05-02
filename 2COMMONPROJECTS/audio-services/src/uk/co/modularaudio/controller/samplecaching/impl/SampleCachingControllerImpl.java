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

package uk.co.modularaudio.controller.samplecaching.impl;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.controller.samplecaching.SampleCachingController;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;


public class SampleCachingControllerImpl implements ComponentWithLifecycle, SampleCachingController
{
	private SampleCachingService sampleCachingService;
	private BlockResamplerService blockResamplerService;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( sampleCachingService == null ||
				blockResamplerService == null )
		{
			throw new ComponentConfigurationException( "SampleCachingController missing service dependencies. Check configuration." );
		}
	}

	@Override
	public void destroy()
	{
	}

	public void setSampleCachingService( final SampleCachingService sampleCachingService )
	{
		this.sampleCachingService = sampleCachingService;
	}

	public void setBlockResamplerService( final BlockResamplerService blockResamplerService )
	{
		this.blockResamplerService = blockResamplerService;
	}

	@Override
	public SampleCacheClient registerCacheClientForFile( final String path )
			throws DatastoreException, NoSuchHibernateSessionException, IOException, UnsupportedAudioFileException
	{
		return sampleCachingService.registerCacheClientForFile( path );
	}

	@Override
	public void registerForBufferFillCompletion( final SampleCacheClient client,
			final BufferFillCompletionListener completionListener )
	{
		sampleCachingService.registerForBufferFillCompletion( client, completionListener );
	}

	@Override
	public void unregisterCacheClientForFile( final SampleCacheClient client )
			throws DatastoreException, RecordNotFoundException, IOException
	{
		sampleCachingService.unregisterCacheClientForFile( client );
	}

	@Override
	public void dumpSampleCache()
	{
		sampleCachingService.dumpSampleCacheToLog();
	}

	@Override
	public BlockResamplingClient createResamplingClient( final String pathToFile, final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		return blockResamplerService.createResamplingClient( pathToFile, resamplingMethod );
	}

	@Override
	public BlockResamplingClient promoteSampleCacheClientToResamplingClient( final SampleCacheClient sampleCacheClient,
			final BlockResamplingMethod cubic )
	{
		return blockResamplerService.promoteSampleCacheClientToResamplingClient( sampleCacheClient, cubic );
	}

	@Override
	public void destroyResamplingClient( final BlockResamplingClient resamplingClient )
			throws DatastoreException, RecordNotFoundException
	{
		blockResamplerService.destroyResamplingClient( resamplingClient );
	}

}
