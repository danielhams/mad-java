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

import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadChannelPeriodData;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MadRenderingJobWithEvents extends AbstractMadRenderingJob
{
	public MadRenderingJobWithEvents( final MadInstance<?,?> madInstance )
	{
		super( madInstance );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.renderingplan.impl.MadRenderingJob#go(uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage, uk.co.modularaudio.util.audio.mad.timing.MadTimingSource)
	 */
	@Override
	public RealtimeMethodReturnCodeEnum go( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingSource timingSource )
	{
		errctx.reset();

		final MadTimingParameters timingParameters = timingSource.getTimingParameters();
		final MadChannelPeriodData timingPeriodData = timingSource.getTimingPeriodData();
		final long periodTimestamp = timingPeriodData.getPeriodStartFrameTimes();
		final int numFrames = timingPeriodData.getNumFramesThisPeriod();

		return madInstance.processWithEvents( tempQueueEntryStorage,
				timingParameters,
				periodTimestamp,
				channelActiveBitset,
				channelBuffers,
				numFrames );
	}
}
