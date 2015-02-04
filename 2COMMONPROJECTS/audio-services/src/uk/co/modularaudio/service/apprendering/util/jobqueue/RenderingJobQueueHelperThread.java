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

package uk.co.modularaudio.service.apprendering.util.jobqueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.thread.AbstractInterruptableThread;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public class RenderingJobQueueHelperThread extends AbstractInterruptableThread
{
	private static Log log = LogFactory.getLog( RenderingJobQueueHelperThread.class.getName() );

	private final HelperThreadJobQueueProcessing jobQueueProcessing;
	private final boolean shouldProfileRenderingJobs;

	public RenderingJobQueueHelperThread( final int threadNum, final AppRenderingJobQueue renderingJobQueue, final boolean shouldProfileRenderingJobs )
	{
		super( MAThreadPriority.REALTIME );
		jobQueueProcessing = new HelperThreadJobQueueProcessing( threadNum, renderingJobQueue );
		this.shouldProfileRenderingJobs = shouldProfileRenderingJobs;
		setName( RenderingJobQueueHelperThread.class.getSimpleName() + " " + threadNum );
	}

	@Override
	protected void doJob() throws InterruptedException, DatastoreException
	{
		boolean localShouldHalt = shouldHalt;
		while( !localShouldHalt )
		{
			jobQueueProcessing.doBlockingProcessing( shouldProfileRenderingJobs );
			localShouldHalt = shouldHalt;
		}

		if( log.isDebugEnabled() )
		{
			log.debug("Processed " + jobQueueProcessing.getNumJobsProcessed() + " jobs");
		}
		jobQueueProcessing.resetNumJobProcessed();
	}

}
