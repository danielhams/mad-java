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

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.thread.AbstractInterruptableThread;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public class SampleCachePopulatorThread extends AbstractInterruptableThread
{
	private static Log log = LogFactory.getLog( SampleCachePopulatorThread.class.getName() );

	private final SampleCache sampleCache;
	private final TemperatureBufferBlockMap temperatureBufferBlockMap;

	private final Semaphore jobsToDoSemaphore = new Semaphore( 1, false );

	public SampleCachePopulatorThread( final SampleCache sampleCache,
			final TemperatureBufferBlockMap temperatureBufferBlockMap )
	{
		super( MAThreadPriority.REALTIME_SUPPORT );
		this.setName( getClass().getSimpleName() );
		this.sampleCache = sampleCache;
		this.temperatureBufferBlockMap = temperatureBufferBlockMap;
	}

	@Override
	protected void doJob() throws Exception
	{
//		log.debug("doJob()");
		temperatureBufferBlockMap.allocate();

		boolean localHalt = shouldHalt;

		while( !localHalt )
		{
			try
			{
				jobsToDoSemaphore.acquire();
				jobsToDoSemaphore.drainPermits();
				if( SampleCache.DEBUG_SAMPLE_CACHE_ACTIVITY )
				{
					log.trace( "Attempting to refresh cache." );
				}
				sampleCache.refreshCache();
			}
			catch( final InterruptedException ie )
			{
				// Don't care, probably woken up to shutdown
			}
			catch( final Exception e )
			{
				if( log.isErrorEnabled() )
				{
					log.error("Exception caught during cache population run: " + e.toString(), e );
				}
			}
			localHalt = shouldHalt;
		}
	}

	public void addOneJobToDo()
	{
		jobsToDoSemaphore.release();
	}

	@Override
	public void halt()
	{
		super.halt();
		super.interrupt();
	}

}
