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

import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.thread.RealtimeMethodErrorContext;

public abstract class AbstractMadParallelRenderingJob extends AbstractRenderingJob
{
	protected final MadTimingSource timingSource;
	protected final int cardinality;

	protected final MadInstance<?,?> madInstance;
	protected final MadChannelConnectedFlags channelActiveBitset;
	protected final MadChannelBuffer[] channelBuffers;
	protected final RealtimeMethodErrorContext errctx = new RealtimeMethodErrorContext();

	public AbstractMadParallelRenderingJob( final int cardinality,
			final MadTimingSource timingSource,
			final MadInstance<?,?> madInstance )
	{
		super( madInstance.getInstanceName() + " of type " + madInstance.getDefinition().getName(),
				null,
				0 );
		this.timingSource = timingSource;
		this.cardinality = cardinality;

		this.madInstance = madInstance;
		final int numChannelInstances = madInstance.getChannelInstances().length;
		channelBuffers = new MadChannelBuffer[ numChannelInstances ];
		channelActiveBitset = new MadChannelConnectedFlags( numChannelInstances );
	}

	public void setDependencies( final RenderingJob[] consJobsThatWaitForUs,
			final int numSourcesWeWaitFor )
	{
		this.consJobsThatWaitForUs = consJobsThatWaitForUs;
		this.numProducersWeWaitFor = numSourcesWeWaitFor;
		this.numProducersStillToComplete.set(numSourcesWeWaitFor);
	}

	public int getCardinality()
	{
		return cardinality;
	}

	public MadInstance<?,?> getMadInstance()
	{
		return madInstance;
	}

	public MadChannelConnectedFlags getChannelConnectedFlags()
	{
		return channelActiveBitset;
	}

	public MadChannelBuffer[] getChannelBuffers()
	{
		return channelBuffers;
	}
}
