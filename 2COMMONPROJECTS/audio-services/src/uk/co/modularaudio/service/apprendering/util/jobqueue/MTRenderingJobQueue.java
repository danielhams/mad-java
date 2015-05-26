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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.service.renderingplan.RenderingJob;


public class MTRenderingJobQueue implements AppRenderingJobQueue
{
//	private static Log log = LogFactory.getLog( MTRenderingJobQueue.class.getName() );

	private static final int JOB_FETCH_TIMEOUT_MILLIS = 10;

	private final MTSafeGenericRingBuffer<RenderingJob> mtSafeJobQueue;

//	private AtomicBoolean internalShouldBlock = new AtomicBoolean(true);
	private volatile boolean internalShouldBlock = true; // NOPMD by dan on 22/01/15 07:40
	private final ReentrantLock internalLock = new ReentrantLock( true );
	private final Condition notEmpty = internalLock.newCondition();

	public MTRenderingJobQueue( final int capacity )
	{
		mtSafeJobQueue = new MTSafeGenericRingBuffer<RenderingJob>( RenderingJob.class, capacity );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#getAJob(boolean)
	 */
	@Override
	public RenderingJob getAJob( final boolean canBlock )
	{
		RenderingJob retVal = null;
		final boolean localCanBlock = canBlock && internalShouldBlock;

		if( localCanBlock )
		{
			internalLock.lock();
			retVal = mtSafeJobQueue.readOneOrNull();
			if( retVal == null )
			{
				try
				{
					notEmpty.await( JOB_FETCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS );
				}
				catch( final InterruptedException ie )
				{
					// Don't care, we're mirroring lack of an interrupted exception in c++
				}
			}
			internalLock.unlock();
		}
		else
		{
			retVal = mtSafeJobQueue.readOneOrNull();
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#setBlocking(boolean)
	 */
	@Override
	public void setBlocking( final boolean shouldBlock )
	{
		internalShouldBlock = shouldBlock;
		if( !shouldBlock )
		{
			internalLock.lock();
			notEmpty.signalAll();
			internalLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#write(uk.co.modularaudio.projects.pac.service.rendering.vos.AbstractParallelRenderingJob[], int, int)
	 */
	@Override
	public void write( final RenderingJob[] jobs, final int startOffset, final int length )
	{
		mtSafeJobQueue.write( jobs, startOffset, length );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#writeOne(uk.co.modularaudio.projects.pac.service.rendering.vos.AbstractParallelRenderingJob)
	 */
	@Override
	public void writeOne( final RenderingJob job )
	{
		mtSafeJobQueue.writeOne( job );
	}
}
