package uk.co.modularaudio.service.apprenderingstructure.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprenderingstructure.AppRenderingStructure;
import uk.co.modularaudio.service.apprenderingstructure.HotspotRenderingContainer;
import uk.co.modularaudio.service.apprenderingstructure.renderingjobqueue.HotspotClockSourceJobQueueHelperThread;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rendering.RenderingPlan;
import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class HotspotRenderingAppStructure extends AppRenderingStructure implements HotspotRenderingContainer
{
	private static Log log = LogFactory.getLog( HotspotRenderingAppStructure.class.getName() );

	private HotspotClockSourceJobQueueHelperThread hotspotClockSourceThread;

	private final TimingService timingService;

	public HotspotRenderingAppStructure( final MadComponentService componentService,
			final MadGraphService graphService,
			final RenderingService renderingService,
			final TimingService timingService,
			final int numHelperThreads,
			final boolean shouldProfileRenderingJobs,
			final int maxWaitForTransitionMillis )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		super( componentService,
				graphService,
				renderingService,
				numHelperThreads,
				shouldProfileRenderingJobs,
				maxWaitForTransitionMillis );
		this.timingService = timingService;
	}

	@Override
	public void startHotspotLooping( final RenderingPlan renderingPlan )
	{
		hotspotClockSourceThread = new HotspotClockSourceJobQueueHelperThread( renderingPlan,
				renderingService,
				timingService,
				renderingJobQueue,
				shouldProfileRenderingJobs );
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
