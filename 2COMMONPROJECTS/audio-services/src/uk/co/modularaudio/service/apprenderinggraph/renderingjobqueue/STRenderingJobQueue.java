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
import uk.co.modularaudio.util.audio.buffer.UnsafeGenericRingBuffer;


public class STRenderingJobQueue implements RenderingJobQueue
{
	public static final int RENDERING_JOB_QUEUE_CAPACITY = 256;

	private final UnsafeGenericRingBuffer<AbstractParallelRenderingJob> jobRing;

	public STRenderingJobQueue( final int capacity )
	{
		jobRing = new UnsafeGenericRingBuffer<AbstractParallelRenderingJob>( AbstractParallelRenderingJob.class, capacity );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#getAJob(boolean)
	 */
	@Override
	public AbstractParallelRenderingJob getAJob( final boolean canBlock )
	{
		return jobRing.readOneOrNull();
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#setBlocking(boolean)
	 */
	@Override
	public void setBlocking( final boolean shouldBlock )
	{
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#write(uk.co.modularaudio.projects.pac.service.rendering.vos.AbstractParallelRenderingJob[], int, int)
	 */
	@Override
	public void write( final AbstractParallelRenderingJob[] jobs, final int startOffset, final int length )
	{
		jobRing.write( jobs, startOffset, length );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.rendering.vos.RenderingJobQueueI#writeOne(uk.co.modularaudio.projects.pac.service.rendering.vos.AbstractParallelRenderingJob)
	 */
	@Override
	public void writeOne( final AbstractParallelRenderingJob job )
	{
		jobRing.writeOne( job );
	}
}
