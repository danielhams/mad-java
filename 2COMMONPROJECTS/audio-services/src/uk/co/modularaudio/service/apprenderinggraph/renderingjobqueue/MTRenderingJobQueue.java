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


public class MTRenderingJobQueue implements RenderingJobQueue
{
	private static final int JOB_FETCH_TIMEOUT_MILLIS = 10;

	private MTSafeGenericRingBuffer<AbstractParallelRenderingJob> mtSafeJobQueue = null;
	
//	private AtomicBoolean internalShouldBlock = new AtomicBoolean(true);
	private volatile boolean internalShouldBlock = true;
	private Integer internalLock = new Integer(0);

	public static final int RENDERING_JOB_QUEUE_CAPACITY = 256;
	
	public MTRenderingJobQueue( int capacity )
	{
		mtSafeJobQueue = new MTSafeGenericRingBuffer<AbstractParallelRenderingJob>( AbstractParallelRenderingJob.class, capacity );
	}
	
	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#getAJob(boolean)
	 */
	@Override
	public AbstractParallelRenderingJob getAJob( boolean canBlock )
	{
		AbstractParallelRenderingJob retVal = null;
		boolean localCanBlock = ( canBlock && internalShouldBlock );
		
		if( localCanBlock )
		{
			synchronized( internalLock )
			{
				retVal = mtSafeJobQueue.readOneOrNull();
				if( retVal == null )
				{
					try
					{
						internalLock.wait( JOB_FETCH_TIMEOUT_MILLIS );
					}
					catch( InterruptedException ie )
					{
						// Don't care, we're mirroring lack of an interrupted exception in c++
					}
				}
			}
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
	public void setBlocking( boolean shouldBlock )
	{
//		internalShouldBlock.set( shouldBlock );
		internalShouldBlock = shouldBlock;
		if( !shouldBlock )
		{
			synchronized( internalLock )
			{
				internalLock.notifyAll();
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#write(uk.co.modularaudio.projects.pac.service.rendering.vos.AbstractParallelRenderingJob[], int, int)
	 */
	@Override
	public void write( AbstractParallelRenderingJob[] jobs, int startOffset, int length )
	{
		mtSafeJobQueue.write( jobs, startOffset, length );
	}
	
	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#writeOne(uk.co.modularaudio.projects.pac.service.rendering.vos.AbstractParallelRenderingJob)
	 */
	@Override
	public void writeOne( AbstractParallelRenderingJob job )
	{
		mtSafeJobQueue.writeOne( job );
	}
}
