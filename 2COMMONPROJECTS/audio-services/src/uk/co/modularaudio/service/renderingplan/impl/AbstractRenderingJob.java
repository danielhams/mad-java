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

package uk.co.modularaudio.service.renderingplan.impl;

import java.util.concurrent.atomic.AtomicInteger;

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public abstract class AbstractRenderingJob implements RenderingJob
{
//	private static Log log = LogFactory.getLog( AbstractParallelRenderingJob.class.getName() );

	protected final String jobName;

	protected RenderingJob[] consJobsThatWaitForUs;

	// To allow dependent jobs to decrement our ready to go counter
	protected final AtomicInteger numProducersStillToComplete;

	// When the job is complete and we update the sources we check if their counter above is zero
	// if it is, we add them to the "things to do" queue and reset this jobs counter back to it's original value
	// This way it is ready for the next pass
	protected int numProducersWeWaitFor;

	// Timing information
	protected long jobStartTimestamp;
	protected long jobEndTimestamp;
	protected int jobThreadExecutor;

	public AbstractRenderingJob( final String iJobName,
			final RenderingJob[] consJobsThatWaitForUs,
			final int numProducersWeWaitFor )
	{
		this.jobName = iJobName;
		this.consJobsThatWaitForUs = consJobsThatWaitForUs;
		this.numProducersWeWaitFor = numProducersWeWaitFor;
		this.numProducersStillToComplete = new AtomicInteger( numProducersWeWaitFor );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#toString()
	 */
	@Override
	public String toString()
	{
		return jobName;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#getConsJobsThatWaitForUs()
	 */
	@Override
	public final RenderingJob[] getConsJobsThatWaitForUs()
	{
		return consJobsThatWaitForUs;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#markOneProducerAsCompleteCheckIfReadyToGo()
	 */
	@Override
	public boolean markOneProducerAsCompleteCheckIfReadyToGo()
	{
		final int numAfter = numProducersStillToComplete.decrementAndGet();
		return numAfter == 0;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#goWithTimestamps(int, uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage)
	 */
	@Override
	public final RealtimeMethodReturnCodeEnum goWithTimestamps( final int jobThreadExecutor,
			final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage )
	{
		this.jobThreadExecutor = jobThreadExecutor;
		jobStartTimestamp = System.nanoTime();
		final RealtimeMethodReturnCodeEnum retVal = go( tempQueueEntryStorage );
		jobEndTimestamp = System.nanoTime();
		numProducersStillToComplete.set( numProducersWeWaitFor );
		return retVal;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#forDumpResetNumProducersStillToComplete()
	 */
	@Override
	public final void forDumpResetNumProducersStillToComplete()
	{
		numProducersStillToComplete.set( numProducersWeWaitFor );
	}
	abstract protected RealtimeMethodReturnCodeEnum go( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage );

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#getJobStartTimestamp()
	 */
	@Override
	public long getJobStartTimestamp()
	{
		return jobStartTimestamp;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#getJobEndTimestamp()
	 */
	@Override
	public long getJobEndTimestamp()
	{
		return jobEndTimestamp;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#getJobThreadExecutor()
	 */
	@Override
	public int getJobThreadExecutor()
	{
		return jobThreadExecutor;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.RenderingJob#addSelfToQueue(uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue)
	 */
	@Override
	public void addSelfToQueue( final AppRenderingJobQueue renderingJobQueue )
	{
		renderingJobQueue.writeOne( this );
	}
}
