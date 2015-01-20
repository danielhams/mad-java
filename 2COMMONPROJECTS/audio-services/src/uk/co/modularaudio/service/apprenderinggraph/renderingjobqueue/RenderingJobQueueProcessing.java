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

package uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue;

import uk.co.modularaudio.service.rendering.vos.AbstractParallelRenderingJob;
import uk.co.modularaudio.service.rendering.vos.RenderingJobQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public abstract class RenderingJobQueueProcessing
{
//	private static Log log = LogFactory.getLog( RenderingJobQueueProcessing.class.getName() );
	
	public enum Type
	{
		CLOCK_SOURCE,
		HELPER_THREAD
	}
	
	protected RenderingJobQueue renderingJobQueue = null;
	protected Type processingType = null;

	protected ThreadSpecificTemporaryEventStorage tempQueueEntryStorage;
	
	protected long numJobsProcessed = 0;
	protected final int threadNum;
	
	public RenderingJobQueueProcessing( int threadNum, RenderingJobQueue jobQueue, Type processingType )
	{
		this.threadNum = threadNum;
		this.renderingJobQueue = jobQueue;
		this.processingType = processingType;
		this.tempQueueEntryStorage = new ThreadSpecificTemporaryEventStorage( MTRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY );
	}

	public final RealtimeMethodReturnCodeEnum doBlockingProcessing( boolean shouldProfileRenderingJobs )
	{
		// First parameter says it's okay to block me
		return processJobAndDependants( true, shouldProfileRenderingJobs );
	}
	
	protected final RealtimeMethodReturnCodeEnum processJobAndDependants( boolean canBlock, boolean shouldProfileRenderingJobs )
	{
		AbstractParallelRenderingJob job = renderingJobQueue.getAJob( canBlock );
			
		while( job != null )
		{
			numJobsProcessed++;
			job = processOneJobReturnFirstDependant( job, shouldProfileRenderingJobs );
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected final AbstractParallelRenderingJob processOneJobReturnFirstDependant( AbstractParallelRenderingJob job,
			boolean shouldProfileRenderingJobs )
	{
		job.goWithTimestamps( threadNum, tempQueueEntryStorage );

		AbstractParallelRenderingJob consJobs[] = job.getConsJobsThatWaitForUs();
		job = null;
		
		for( AbstractParallelRenderingJob consJob : consJobs )
		{
			boolean readyToGo = consJob.markOneProducerAsCompleteCheckIfReadyToGo();	
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
