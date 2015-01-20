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

import uk.co.modularaudio.service.rendering.vos.AbstractParallelRenderingJob;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MadParallelRenderingJob extends AbstractParallelRenderingJob
{
	private MadTimingSource timingSource = null;
	private MadRenderingJob renderingJob = null;
	private int cardinality = -1;
	
	public MadParallelRenderingJob( int cardinality,
			MadTimingSource timingSource,
			MadRenderingJob renderingJob )
	{
		super( renderingJob.toString(), null, 0 );
		this.timingSource = timingSource;
		this.cardinality = cardinality;
		this.renderingJob = renderingJob;
	}
	
	public void setDependencies( AbstractParallelRenderingJob[] consJobsThatWaitForUs, int numSourcesWeWaitFor )
	{
		this.consJobsThatWaitForUs = consJobsThatWaitForUs;
		this.numProducersWeWaitFor = numSourcesWeWaitFor;
		this.numProducersStillToComplete.set(numSourcesWeWaitFor);
	}
	
	public MadRenderingJob getRenderingJob()
	{
		return renderingJob;
	}
	
	@Override
	public RealtimeMethodReturnCodeEnum go( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage )
	{
		return renderingJob.go( tempQueueEntryStorage, timingSource );
	}

	public int getCardinality()
	{
		return cardinality;
	}
}
