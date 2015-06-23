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

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.service.apprendering.util.jobqueue.AbstractRenderingJobQueueProcessing.Type;
import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.util.thread.RealtimeMethodErrorContext;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class ClockSourceJobQueueProcessing
{
//	private static Log log = LogFactory.getLog( ClockSourceJobQueueProcessing.class.getName() );

	private final RealtimeMethodErrorContext errCtx = new RealtimeMethodErrorContext();
	private final HelperThreadJobQueueProcessing jobQueueProcessing;
	private final AppRenderingJobQueue renderingJobQueue;

	public ClockSourceJobQueueProcessing( final AppRenderingJobQueue jobQueue )
	{
		jobQueueProcessing = new HelperThreadJobQueueProcessing( 0,  jobQueue, Type.CLOCK_SOURCE );
		renderingJobQueue = jobQueue;
	}

	public RealtimeMethodReturnCodeEnum doUnblockedJobQueueProcessing( final RenderingPlan renderingPlan, final boolean shouldProfileRenderingJobs )
	{
		errCtx.reset();

		final RenderingJob[] initialJobs = renderingPlan.getInitialJobs();

		final int numInitialJobs = initialJobs.length;

		// Unblock the job queue so no waiting
		renderingJobQueue.setBlocking( false );

		// Now do the job processing
		renderingJobQueue.write( initialJobs, 0, numInitialJobs );

		while( !renderingPlan.wasPlanExecuted() )
		{
			if( !errCtx.andWith(
					jobQueueProcessing.processJobAndDependants( false, shouldProfileRenderingJobs )
					)
				)
			{
				return errCtx.getCurRetCode();
			}
		}

		// And set the job queue back to waiting
		renderingJobQueue.setBlocking( true );

		renderingPlan.resetPlanExecution();

		return errCtx.getCurRetCode();
	}
}
