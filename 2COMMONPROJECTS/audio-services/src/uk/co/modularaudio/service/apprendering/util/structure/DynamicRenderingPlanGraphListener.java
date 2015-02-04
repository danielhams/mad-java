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

package uk.co.modularaudio.service.apprendering.util.structure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprendering.util.AppRenderingStructure;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.service.renderingplan.RenderingPlanService;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphListener;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.exception.DatastoreException;

public class DynamicRenderingPlanGraphListener implements MadGraphListener
{
	private static Log log = LogFactory.getLog( DynamicRenderingPlanGraphListener.class.getName() );

	private final RenderingPlanService renderingPlanService;
	private final MadGraphInstance<?,?> rootGraph;
	private final AppRenderingStructure appRenderingStructure;

	public DynamicRenderingPlanGraphListener( final RenderingPlanService renderingPlanService,
			final AppRenderingStructure appRenderingStructure,
			final MadGraphInstance<?,?> rootGraph )
	{
		this.renderingPlanService = renderingPlanService;
		this.rootGraph = rootGraph;
		this.appRenderingStructure = appRenderingStructure;
	}

	public void forcePlanCreation( final HardwareIOChannelSettings coreEngineChannelSettings,
			final MadFrameTimeFactory frameTimeFactory )
		throws DatastoreException, MadProcessingException
	{
		try
		{
			final RenderingPlan oldRp = appRenderingStructure.getAtomicRenderingPlan().get();
			if( oldRp != null )
			{
				final String msg = "Found an old rendering plan although we shouldn't be rendering!";
				throw new MadProcessingException( msg );
			}
			final RenderingPlan renderingPlan = renderingPlanService.createRenderingPlan( rootGraph, coreEngineChannelSettings, frameTimeFactory );
			appRenderingStructure.useNewRenderingPlanWithWaitDestroyPrevious( renderingPlan );
		}
		catch( final Exception e )
		{
			 final String msg = "Exception caugtht forcing plan creation: " + e.toString();
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
			final RenderingPlan oldRenderingPlan = appRenderingStructure.getAtomicRenderingPlan().get();
			final HardwareIOChannelSettings planChannelSettings = oldRenderingPlan.getPlanChannelSettings();
			final MadFrameTimeFactory planFrameTimeFactory = oldRenderingPlan.getPlanFrameTimeFactory();
			final RenderingPlan renderingPlan = renderingPlanService.createRenderingPlan( rootGraph, planChannelSettings, planFrameTimeFactory );

			appRenderingStructure.useNewRenderingPlanWithWaitDestroyPrevious( renderingPlan );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught generating a rendering plan and pushing it to the execution service: " + e.toString();
			log.error( msg, e );
		}
	}

	public void destroy() throws MadProcessingException, DatastoreException
	{
		// Remove the rendering plan and destroy it
		appRenderingStructure.useNewRenderingPlanWithWaitDestroyPrevious( null );
	}

	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}

}
