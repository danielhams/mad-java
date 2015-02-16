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

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.thread.RealtimeMethodErrorContext;

public abstract class AbstractMadRenderingJob implements MadRenderingJob
{
//	private static Log log = LogFactory.getLog( MadRenderingJob.class.getName() );

	protected final String jobName;
	protected final MadInstance<?,?> madInstance;
	protected final MadChannelConnectedFlags channelActiveBitset;
	protected final MadChannelBuffer[] channelBuffers;
	protected final RealtimeMethodErrorContext errctx = new RealtimeMethodErrorContext();

	public AbstractMadRenderingJob( final MadInstance<?,?> madInstance )
	{
		this.jobName = madInstance.getInstanceName() + " of type " + madInstance.getDefinition().getName();
		this.madInstance = madInstance;
		final int numChannelInstances = madInstance.getChannelInstances().length;
		channelBuffers = new MadChannelBuffer[ numChannelInstances ];
		channelActiveBitset = new MadChannelConnectedFlags( numChannelInstances );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.impl.MadRenderingJob#getJobName()
	 */
	@Override
	public String getJobName()
	{
		return jobName;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.impl.MadRenderingJob#getMadInstance()
	 */
	@Override
	public MadInstance<?,?> getMadInstance()
	{
		return madInstance;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.impl.MadRenderingJob#getChannelBuffers()
	 */
	@Override
	public MadChannelBuffer[] getChannelBuffers()
	{
		return channelBuffers;
	}

	@Override
	public String toString()
	{
		return madInstance.getInstanceName() + " of type " + madInstance.getDefinition().getName();
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.impl.MadRenderingJob#getChannelConnectedFlags()
	 */
	@Override
	public MadChannelConnectedFlags getChannelConnectedFlags()
	{
		return channelActiveBitset;
	}
}
