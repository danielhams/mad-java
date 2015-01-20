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

package uk.co.modularaudio.service.apprenderinggraph.vos;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.service.rendering.vos.RenderingPlan;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphListener;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.exception.DatastoreException;

public class DynamicRenderingPlanGraphListener implements MadGraphListener
{
	private static Log log = LogFactory.getLog( DynamicRenderingPlanGraphListener.class.getName() );

	private RenderingService renderingService = null;
	private MadGraphInstance<?,?> rootGraph = null;
	private AppRenderingGraph appRenderingGraph = null;

	public DynamicRenderingPlanGraphListener( RenderingService renderingService,
			AppRenderingGraph appRenderingGraph,
			MadGraphInstance<?,?> rootGraph )
	{
		this.renderingService = renderingService;
		this.rootGraph = rootGraph;
		this.appRenderingGraph = appRenderingGraph;
	}

	public void forcePlanCreation( HardwareIOChannelSettings coreEngineChannelSettings,
			MadFrameTimeFactory frameTimeFactory )
		throws DatastoreException, MadProcessingException
	{
		try
		{
			RenderingPlan oldRp = appRenderingGraph.getAtomicRenderingPlan().get();
			if( oldRp != null )
			{
				String msg = "Found an old rendering plan although we shouldn't be rendering!";
				throw new MadProcessingException( msg );
			}
			RenderingPlan renderingPlan = renderingService.createRenderingPlan( rootGraph, coreEngineChannelSettings, frameTimeFactory );
			appRenderingGraph.useNewRenderingPlanWithWaitDestroyPrevious( renderingPlan );
		}
		catch( Exception e )
		{
			 String msg = "Exception caugtht forcing plan creation: " + e.toString();
			 log.error( msg, e );
			 throw new DatastoreException( msg, e  );
		}
	}

	@Override
	public void receiveGraphChangeSignal()
	{
//		log.debug("DRPGL Got a graph changed signal!");

		// Recompute the rendering plan for the graph and push it into the execution service as the plan that will be executed.
		try
		{
			RenderingPlan oldRenderingPlan = appRenderingGraph.getAtomicRenderingPlan().get();
			HardwareIOChannelSettings planChannelSettings = oldRenderingPlan.getPlanChannelSettings();
			MadFrameTimeFactory planFrameTimeFactory = oldRenderingPlan.getPlanFrameTimeFactory();
			RenderingPlan renderingPlan = renderingService.createRenderingPlan( rootGraph, planChannelSettings, planFrameTimeFactory );

			appRenderingGraph.useNewRenderingPlanWithWaitDestroyPrevious( renderingPlan );
		}
		catch (Exception e)
		{
			String msg = "Exception caught generating a rendering plan and pushing it to the execution service: " + e.toString();
			log.error( msg, e );
		}
	}

	public void destroy() throws MadProcessingException, DatastoreException
	{
		// Remove the rendering plan and destroy it
		appRenderingGraph.useNewRenderingPlanWithWaitDestroyPrevious( null );
	}

	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}

}
