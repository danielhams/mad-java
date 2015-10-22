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

public class MadParallelRenderingJobNoEvents extends AbstractMadParallelRenderingJob
{
	public MadParallelRenderingJobNoEvents( final int cardinality,
			final MadTimingSource timingSource,
			final MadInstance<?,?> madInstance )
	{
		super( cardinality, timingSource, madInstance );
	}

	@Override
	public RealtimeMethodReturnCodeEnum go( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage )
	{
		errctx.reset();

		final MadTimingParameters timingParameters = timingSource.getTimingParameters();
		final MadChannelPeriodData timingPeriodData = timingSource.getTimingPeriodData();
		final long periodTimestamp = timingPeriodData.getPeriodStartFrameTimes();
		if( !errctx.andWith( madInstance.processNoEvents( tempQueueEntryStorage,
				timingParameters,
				periodTimestamp,
				channelActiveBitset,
				channelBuffers,
				timingPeriodData.getNumFramesThisPeriod() ) ) )
		{
			return errctx.getCurRetCode();
		}

		return errctx.getCurRetCode();
	}
}
