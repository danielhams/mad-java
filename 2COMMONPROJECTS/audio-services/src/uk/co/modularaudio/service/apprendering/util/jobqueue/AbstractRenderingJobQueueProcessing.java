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

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public abstract class AbstractRenderingJobQueueProcessing
{
//	private static Log log = LogFactory.getLog( RenderingJobQueueProcessing.class.getName() );

	public enum Type
	{
		CLOCK_SOURCE,
		HELPER_THREAD
	}

	protected final AppRenderingJobQueue renderingJobQueue;
	protected final Type processingType;

	protected final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage;

	protected long numJobsProcessed;
	protected final int threadNum;

	public AbstractRenderingJobQueueProcessing( final int threadNum, final AppRenderingJobQueue jobQueue, final Type processingType )
	{
		this.threadNum = threadNum;
		this.renderingJobQueue = jobQueue;
		this.processingType = processingType;
		this.tempQueueEntryStorage = new ThreadSpecificTemporaryEventStorage( MTRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY );
	}

	public final RealtimeMethodReturnCodeEnum doBlockingProcessing( final boolean shouldProfileRenderingJobs )
	{
		// First parameter says it's okay to block me
		return processJobAndDependants( true, shouldProfileRenderingJobs );
	}

	protected final RealtimeMethodReturnCodeEnum processJobAndDependants( final boolean canBlock, final boolean shouldProfileRenderingJobs )
	{
		RenderingJob job = renderingJobQueue.getAJob( canBlock );

		while( job != null )
		{
			numJobsProcessed++;
			job = processOneJobReturnFirstDependant( job, shouldProfileRenderingJobs );
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected final RenderingJob processOneJobReturnFirstDependant( final RenderingJob iJob,
			final boolean shouldProfileRenderingJobs )
	{
		RenderingJob job = iJob;
		job.goWithTimestamps( threadNum, tempQueueEntryStorage );

		final RenderingJob consJobs[] = job.getConsJobsThatWaitForUs();
		job = null;

		for( final RenderingJob consJob : consJobs )
		{
			final boolean readyToGo = consJob.markOneProducerAsCompleteCheckIfReadyToGo();
			if( readyToGo )
			{
				if( job == null )
				{
					job = consJob;
				}
				else
				{
					consJob.addSelfToQueue( renderingJobQueue );
				}
			}
		}

		return job;
	}

	public long getNumJobsProcessed()
	{
		return numJobsProcessed;
	}

	public void resetNumJobProcessed()
	{
		numJobsProcessed = 0;
	}
}
