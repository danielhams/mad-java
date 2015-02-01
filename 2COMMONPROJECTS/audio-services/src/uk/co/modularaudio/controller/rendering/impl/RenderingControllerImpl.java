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

package uk.co.modularaudio.controller.rendering.impl;

import uk.co.modularaudio.controller.rendering.RenderingController;
import uk.co.modularaudio.service.apprenderingsession.AppRenderingSession;
import uk.co.modularaudio.service.apprenderingsession.AppRenderingSessionService;
import uk.co.modularaudio.service.rendering.RenderingPlan;
import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class RenderingControllerImpl implements ComponentWithLifecycle, RenderingController
{
	private AppRenderingSessionService appRenderingSessionService;
	private RenderingService renderingService;

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.component.ComponentWithLifecycle#init()
	 */
	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.component.ComponentWithLifecycle#destroy()
	 */
	@Override
	public void destroy()
	{
	}

	public void setRenderingService(final RenderingService renderingService)
	{
		this.renderingService = renderingService;
	}

	public void setAppRenderingSessionService( final AppRenderingSessionService appRenderingGraphService )
	{
		this.appRenderingSessionService = appRenderingGraphService;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.controller.rendering.RenderingController#createAppRenderingGraph()
	 */
	@Override
	public AppRenderingSession createAppRenderingSession() throws DatastoreException
	{
		return appRenderingSessionService.createAppRenderingSession();
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.controller.rendering.RenderingController#createRenderingPlan(uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance, uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings, uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory)
	 */
	@Override
	public RenderingPlan createRenderingPlan( final MadGraphInstance<?,?> graphInstance,
			final HardwareIOChannelSettings hardwareChannelSettings,
			final MadFrameTimeFactory frameTimeFactory )
		throws DatastoreException
	{
		return renderingService.createRenderingPlan(graphInstance, hardwareChannelSettings, frameTimeFactory );
	}
}
