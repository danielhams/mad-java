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

package uk.co.modularaudio.service.apprenderinggraph.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprenderinggraph.AppRenderingGraphService;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingGraph;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AppRenderingGraphServiceImpl implements ComponentWithLifecycle, AppRenderingGraphService
{
	private static Log log = LogFactory.getLog( AppRenderingGraphServiceImpl.class.getName() );

	private ConfigurationService configurationService = null;
	private MadComponentService componentService = null;
	private MadGraphService graphService = null;
	private RenderingService renderingService = null;
	private TimingService timingService = null;

	private final static String CONFIG_KEY_NUM_HELPER_THREADS = AppRenderingGraphServiceImpl.class.getSimpleName() + ".NumHelperThreads";
	private static final String CONFIG_KEY_PROFILE_RENDERING_JOBS = AppRenderingGraphServiceImpl.class.getSimpleName() + ".ProfileRenderingJobs";
	private static final String CONFIG_KEY_MAX_WAIT_FOR_TRANSITION_MILLIS = AppRenderingGraphServiceImpl.class.getSimpleName() + ".MaxWaitForTransitionMillis";

	private int numHelperThreads = -1;
	private boolean shouldProfileRenderingJobs = false;
	private int maxWaitForTransitionMillis = -1;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				componentService == null ||
				graphService == null ||
				renderingService == null ||
				timingService == null )
		{
			String msg = "AppRenderingGraphServiceImpl is missing service dependencies. Check configuration.";
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}

		Map<String,String> errors = new HashMap<String,String>();

		numHelperThreads = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_NUM_HELPER_THREADS, errors );

		shouldProfileRenderingJobs = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_PROFILE_RENDERING_JOBS,
				errors );

		maxWaitForTransitionMillis = ConfigurationServiceHelper.checkForIntKey(configurationService, CONFIG_KEY_MAX_WAIT_FOR_TRANSITION_MILLIS, errors);

		ConfigurationServiceHelper.errorCheck( errors );
	}

	@Override
	public void destroy()
	{
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setGraphService( final MadGraphService graphService )
	{
		this.graphService = graphService;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setRenderingService( final RenderingService renderingService )
	{
		this.renderingService = renderingService;
	}

	public void setTimingService( final TimingService timingService )
	{
		this.timingService = timingService;
	}

	@Override
	public AppRenderingGraph createAppRenderingGraph() throws DatastoreException
	{
		try
		{
			return new AppRenderingGraph( componentService, graphService, renderingService, timingService,
					numHelperThreads,
					shouldProfileRenderingJobs,
					maxWaitForTransitionMillis );
		}
		catch( RecordNotFoundException rnfe )
		{
			throw new DatastoreException( "RecordNotFoundException creating app rendering graph: " + rnfe.toString(), rnfe );
		}
		catch( MadProcessingException aupe )
		{
			throw new DatastoreException( "MadProcessingException creating app rendering graph: " + aupe.toString(), aupe );
		}
		catch (MAConstraintViolationException ecve)
		{
			throw new DatastoreException( "ConstraintViolationException creating app rendering graph: " + ecve.toString(), ecve );
		}
	}

	@Override
	public boolean shouldProfileRenderingJobs()
	{
		return shouldProfileRenderingJobs;
	}

	@Override
	public void destroyAppRenderingGraph( final AppRenderingGraph graphToDestroy )
		throws DatastoreException
	{
		// Don't need to do anything, the GC will take care of it
	}
}
