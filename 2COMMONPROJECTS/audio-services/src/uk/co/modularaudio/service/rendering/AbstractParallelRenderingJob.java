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

package uk.co.modularaudio.service.rendering;

import java.util.concurrent.atomic.AtomicInteger;

import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public abstract class AbstractParallelRenderingJob
{
//	private static Log log = LogFactory.getLog( AbstractParallelRenderingJob.class.getName() );

	protected final String jobName;

	protected AbstractParallelRenderingJob[] consJobsThatWaitForUs;

	// To allow dependent jobs to decrement our ready to go counter
	protected final AtomicInteger numProducersStillToComplete;

	// When the job is complete and we update the sources we check if their counter above is zero
	// if it is, we add them to the "things to do" queue and reset this jobs counter back to it's original value
	// This way it is ready for the next pass
	protected int numProducersWeWaitFor;

	// Timing information
	protected long jobStartTimestamp = -1;
	protected long jobEndTimestamp = -1;
	protected int jobThreadExecutor = Integer.MAX_VALUE;

	public AbstractParallelRenderingJob( final String iJobName,
			final AbstractParallelRenderingJob[] consJobsThatWaitForUs,
			final int numProducersWeWaitFor )
	{
		this.jobName = iJobName;
		this.consJobsThatWaitForUs = consJobsThatWaitForUs;
		this.numProducersWeWaitFor = numProducersWeWaitFor;
		this.numProducersStillToComplete = new AtomicInteger( numProducersWeWaitFor );
	}

	@Override
	public String toString()
	{
		return jobName;
	}

	public final AbstractParallelRenderingJob[] getConsJobsThatWaitForUs()
	{
		return consJobsThatWaitForUs;
	}

	public int getNumProducersStillToComplete()
	{
		int retVal = numProducersStillToComplete.get();
		return retVal;
	}

	public int getNumProducersWeWaitFor()
	{
		return numProducersWeWaitFor;
	}

	public boolean markOneProducerAsCompleteCheckIfReadyToGo()
	{
		int numAfter = numProducersStillToComplete.decrementAndGet();
		return numAfter == 0;
	}

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

	public final void forDumpResetNumProducersStillToComplete()
	{
		numProducersStillToComplete.set( numProducersWeWaitFor );
	}
	abstract protected RealtimeMethodReturnCodeEnum go( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage );

	public long getJobStartTimestamp()
	{
		return jobStartTimestamp;
	}

	public long getJobEndTimestamp()
	{
		return jobEndTimestamp;
	}

	public int getJobThreadExecutor()
	{
		return jobThreadExecutor;
	}

	public void addSelfToQueue( final RenderingJobQueue renderingJobQueue )
	{
		renderingJobQueue.writeOne( this );
	}
}
