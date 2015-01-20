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

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadChannelPeriodData;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.thread.RealtimeMethodErrorContext;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MadRenderingJob
{
//	private static Log log = LogFactory.getLog( MadRenderingJob.class.getName() );
	
	private String instanceName = null;
	private MadInstance<?,?> madInstance = null;
	private MadChannelConnectedFlags channelActiveBitset = null;
	private MadChannelBuffer[] channelBuffers = null;
	private RealtimeMethodErrorContext errctx = new RealtimeMethodErrorContext();
	
	public MadRenderingJob( String instanceName, MadInstance<?,?> madInstance )
	{
		this.instanceName = instanceName;
		this.madInstance = madInstance;
		int numChannelInstances = madInstance.getChannelInstances().length;
		channelBuffers = new MadChannelBuffer[ numChannelInstances ];
		channelActiveBitset = new MadChannelConnectedFlags( numChannelInstances );
	}
	
	public String getInstanceName()
	{
		return instanceName;
	}

	public MadInstance<?,?> getMadInstance()
	{
		return madInstance;
	}

	public MadChannelBuffer[] getChannelBuffers()
	{
		return channelBuffers;
	}

	public String toString()
	{
		return madInstance.getInstanceName() + " of type " + madInstance.getDefinition().getName();
	}

	public RealtimeMethodReturnCodeEnum go( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingSource timingSource )
	{
		errctx.reset();
		
		boolean hasQueueProcessing = madInstance.hasQueueProcessing();
		MadTimingParameters timingParameters = timingSource.getTimingParameters();
		MadChannelPeriodData timingPeriodData = timingSource.getTimingPeriodData();
		long periodTimestamp = timingPeriodData.getPeriodStartFrameTimes();
		if( hasQueueProcessing )
		{
			if( !errctx.andWith( madInstance.preProcess( tempQueueEntryStorage,
					timingParameters,
					periodTimestamp ) ) )
			{
				return errctx.getCurRetCode();
			}
		}
		
		if( !errctx.andWith( madInstance.process( tempQueueEntryStorage,
				timingParameters,
				periodTimestamp,
				channelActiveBitset,
				channelBuffers,
				timingPeriodData.getNumFramesThisPeriod() ) ) )
		{
			return errctx.getCurRetCode();
		}

		if( hasQueueProcessing )
		{
			if( !errctx.andWith( madInstance.postProcess( tempQueueEntryStorage,
					timingParameters,
					periodTimestamp ) ) )
			{
				return errctx.getCurRetCode();
			}
		}
		return errctx.getCurRetCode();
	}

	public MadChannelConnectedFlags getChannelConnectedFlags()
	{
		return channelActiveBitset;
	}
}
