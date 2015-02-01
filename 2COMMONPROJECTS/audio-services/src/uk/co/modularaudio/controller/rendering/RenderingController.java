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

package uk.co.modularaudio.controller.rendering;

import uk.co.modularaudio.service.apprenderingstructure.HotspotRenderingContainer;
import uk.co.modularaudio.service.rendering.RenderingPlan;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.exception.DatastoreException;

/**
 * <p>The contract of the rendering controller that is in charge of objects
 * related to rendering such as:
 * <ul>
 * <li>Creation of a rendering plan from a graph
 * <li>Creation of a container that can be used to hotspot compile DSP code before use
 * </ul>
 * </p>
 *
 * @author dan
 *
 */
public interface RenderingController
{
	/**
	 * <p>Create a container that can be used to force hotspot
	 * compilation of dsp code.</p>
	 * @return a newly initialised rendering container in the state "not rendering"
	 * @throws DatastoreException if a non-recoverable error occurred
	 */
	HotspotRenderingContainer createHotspotRenderingContainer() throws DatastoreException;

	/**
	 * <p>Create a rendering plan for the given graph instances and output settings
	 * such as channel buffer length and GUI fps.</p>
	 * @param graphInstance graph the rendering plan should represent
	 * @param hardwareChannelSettings the hardware settings to take into account
	 * @param frameTimeFactory the place the rendering plan should take timing information from
	 * @return a new rendering plan
	 * @throws DatastoreException if a non-recoverable error occurred
	 */
	RenderingPlan createRenderingPlan( MadGraphInstance<?,?> graphInstance,
			HardwareIOChannelSettings hardwareChannelSettings,
			MadFrameTimeFactory frameTimeFactory )
		throws DatastoreException;
}
