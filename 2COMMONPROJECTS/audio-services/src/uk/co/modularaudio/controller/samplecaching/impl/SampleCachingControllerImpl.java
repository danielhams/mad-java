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
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;


public class SampleCachingControllerImpl implements ComponentWithLifecycle, SampleCachingController
{
	private BlockResamplerService blockResamplerService = null;
	private SampleCachingService sampleCachingService = null;

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
	}

	public SampleCachingService getSampleCachingService()
	{
		return sampleCachingService;
	}

	public void setSampleCachingService( SampleCachingService sampleCachingService )
	{
		this.sampleCachingService = sampleCachingService;
	}

	@Override
	public SampleCacheClient registerCacheClientForFile( String path )
			throws NoSuchHibernateSessionException, DatastoreException,
			UnsupportedAudioFileException
	{
		return sampleCachingService.registerCacheClientForFile( path );
	}

	@Override
	public void unregisterCacheClientForFile( SampleCacheClient client )
			throws DatastoreException, RecordNotFoundException, IOException
	{
		sampleCachingService.unregisterCacheClientForFile( client );
	}

	public BlockResamplerService getBlockResamplerService()
	{
		return blockResamplerService;
	}

	public void setBlockResamplerService( BlockResamplerService blockResamplerService )
	{
		this.blockResamplerService = blockResamplerService;
	}

	@Override
	public void dumpSampleCache()
	{
		sampleCachingService.dumpSampleCacheToLog();
	}

}
