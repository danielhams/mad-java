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

package uk.co.modularaudio.service.apprenderingsession.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprenderingsession.AppRenderingSession;
import uk.co.modularaudio.service.apprenderingsession.AppRenderingSessionService;
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

public class AppRenderingSessionServiceImpl implements ComponentWithLifecycle, AppRenderingSessionService
{
	private static Log log = LogFactory.getLog( AppRenderingSessionServiceImpl.class.getName() );

	private ConfigurationService configurationService;
	private MadComponentService componentService;
	private MadGraphService graphService;
	private RenderingService renderingService;
	private TimingService timingService;

	private final static String CONFIG_KEY_NUM_HELPER_THREADS = AppRenderingSessionServiceImpl.class.getSimpleName() + ".NumHelperThreads";
	private static final String CONFIG_KEY_PROFILE_RENDERING_JOBS = AppRenderingSessionServiceImpl.class.getSimpleName() + ".ProfileRenderingJobs";
	private static final String CONFIG_KEY_MAX_WAIT_FOR_TRANSITION_MILLIS = AppRenderingSessionServiceImpl.class.getSimpleName() + ".MaxWaitForTransitionMillis";

	private int numHelperThreads;
	private boolean shouldProfileRenderingJobs;
	private int maxWaitForTransitionMillis;

	public AppRenderingSessionServiceImpl()
	{
		// Uses DI.
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				componentService == null ||
				graphService == null ||
				renderingService == null ||
				timingService == null )
		{
			final String msg = "AppRenderingGraphServiceImpl is missing service dependencies. Check configuration.";
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}

		final Map<String,String> errors = new HashMap<String,String>();

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
	public AppRenderingSession createAppRenderingSession() throws DatastoreException
	{
		try
		{
			return new AppRenderingSession( componentService, graphService, renderingService, timingService,
					numHelperThreads,
					shouldProfileRenderingJobs,
					maxWaitForTransitionMillis );
		}
		catch( final RecordNotFoundException rnfe )
		{
			throw new DatastoreException( "RecordNotFoundException creating app rendering session: " + rnfe.toString(), rnfe );
		}
		catch( final MadProcessingException aupe )
		{
			throw new DatastoreException( "MadProcessingException creating app rendering session: " + aupe.toString(), aupe );
		}
		catch (final MAConstraintViolationException ecve)
		{
			throw new DatastoreException( "ConstraintViolationException creating app rendering session: " + ecve.toString(), ecve );
		}
	}

	@Override
	public boolean shouldProfileRenderingJobs()
	{
		return shouldProfileRenderingJobs;
	}

	@Override
	public void destroyAppRenderingSession( final AppRenderingSession renderingSession )
		throws DatastoreException
	{
		// Don't need to do anything, the GC will take care of it
	}
}
