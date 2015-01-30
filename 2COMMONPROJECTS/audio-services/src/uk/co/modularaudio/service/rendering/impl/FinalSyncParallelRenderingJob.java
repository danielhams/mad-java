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

package uk.co.modularaudio.service.rendering.impl;

import uk.co.modularaudio.service.rendering.AbstractParallelRenderingJob;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class FinalSyncParallelRenderingJob extends AbstractParallelRenderingJob
{
//	private static Log log = LogFactory.getLog( FinalSyncParallelRenderingJob.class.getName() );

	private boolean localExecutedAtLeastOnce = false;
	private volatile boolean executedAtLeastOnce = false;

	private boolean localWasJobExecuted = false;
	private volatile boolean wasJobExecuted = false;

	public FinalSyncParallelRenderingJob()
	{
		super( "Final Sync", new AbstractParallelRenderingJob[0], 0 );
	}

	@Override
	public RealtimeMethodReturnCodeEnum go( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage )
	{
		// We're the final job set mark us as being executed - this lets our rendering plan
		// know that it's been used (so we can clear any old ones up).
		if( !localExecutedAtLeastOnce )
		{
			localExecutedAtLeastOnce = true;
			executedAtLeastOnce = true;
		}

		if( !localWasJobExecuted )
		{
			localWasJobExecuted = true;
			wasJobExecuted = true;
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setNumProducersWeWaitFor( final int numProducersWeWaitFor )
	{
		this.numProducersWeWaitFor = numProducersWeWaitFor;
		this.numProducersStillToComplete.set(  numProducersWeWaitFor );
	}

	public boolean wasExecutedAtLeastOnce()
	{
		return executedAtLeastOnce;
	}

	public void resetJobExecution()
	{
		wasJobExecuted = false;
		localWasJobExecuted = false;
	}

	public boolean wasJobExecuted()
	{
		return wasJobExecuted;
	}

}
