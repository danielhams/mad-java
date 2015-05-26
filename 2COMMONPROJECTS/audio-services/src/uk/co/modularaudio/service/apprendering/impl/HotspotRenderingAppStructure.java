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

package uk.co.modularaudio.service.apprendering.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.service.apprendering.util.AppRenderingStructure;
import uk.co.modularaudio.service.apprendering.util.jobqueue.HotspotClockSourceJobQueueHelperThread;
import uk.co.modularaudio.service.apprendering.util.jobqueue.STRenderingJobQueue;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.service.renderingplan.RenderingPlanService;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class HotspotRenderingAppStructure extends AppRenderingStructure implements HotspotRenderingContainer
{
	private static Log log = LogFactory.getLog( HotspotRenderingAppStructure.class.getName() );

	private HotspotClockSourceJobQueueHelperThread hotspotClockSourceThread;

	public HotspotRenderingAppStructure( final MadComponentService componentService,
			final MadGraphService graphService,
			final RenderingPlanService renderingService,
			final TimingService timingService,
			final int renderingJobQueueCapacity,
			final int tempEventStorageCapacity,
			final boolean shouldProfileRenderingJobs,
			final int maxWaitForTransitionMillis,
			final RenderingPlan renderingPlan )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		super( componentService,
				graphService,
				renderingService,
				renderingJobQueueCapacity,
				0,
				tempEventStorageCapacity,
				shouldProfileRenderingJobs,
				maxWaitForTransitionMillis );

		hotspotClockSourceThread = new HotspotClockSourceJobQueueHelperThread( renderingPlan,
				renderingService,
				timingService,
				new STRenderingJobQueue( AppRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY ),
				shouldProfileRenderingJobs );
	}

	@Override
	public void startHotspotLooping()
	{
		hotspotClockSourceThread.start();
	}

	@Override
	public void stopHotspotLooping()
	{
		// Let it exit gracefully
		try
		{
			hotspotClockSourceThread.halt();
			Thread.sleep( 200 );
		}
		catch(final InterruptedException ie )
		{
		}
		if( hotspotClockSourceThread.isAlive() )
		{
			hotspotClockSourceThread.forceHalt();
		}
		try
		{
			hotspotClockSourceThread.join();
		}
		catch (final InterruptedException ie)
		{
			final String msg = "Interrupted waiting for join to hotspot thread: " + ie.toString();
			log.error( msg, ie );
		}
		hotspotClockSourceThread = null;
	}
}
