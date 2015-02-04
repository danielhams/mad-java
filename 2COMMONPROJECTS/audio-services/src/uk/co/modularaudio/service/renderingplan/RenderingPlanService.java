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

package uk.co.modularaudio.service.renderingplan;

import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.exception.DatastoreException;

/**
 * <p>A service that can transform a low level MAD graph structure
 * into an appropriate rendering plan that can be used at runtime.</p>
 *
 * @author dan
 */
public interface RenderingPlanService
{
	/**
	 * <p>Create a dynamic job style rendering plan from the
	 * low-level graph of components and links.</p>
	 * <p>The supplied graph may currently contain:
	 * <ul>
	 * <li>Components</li>
	 * <li>Links between components</li>
	 * <li>Sub graphs within the root containing the above</li>
	 * </ul>
	 * <p>Current the use of circular links within the graph is
	 * not supported. Neither that of self-links.</p>
	 * @param graph Graph containing (sub)components and links
	 * @param hardwareSettings hardware settings (channel buffer lengths, sample rates) used to start components
	 * @param frameTimeFactory the factory providing the "frame time" mapping
	 * @return a new rendering plan
	 * @throws DatastoreException
	 */
	RenderingPlan createRenderingPlan( MadGraphInstance<?,?> graph,
			HardwareIOChannelSettings hardwareSettings,
			MadFrameTimeFactory frameTimeFactory )
		throws DatastoreException;

	/**
	 * <p>Output the supplied rendering plan in execution
	 * order on the console</p>
	 * @param renderingPlan
	 * @throws DatastoreException
	 */
	void dumpRenderingPlan( RenderingPlan renderingPlan )
		throws DatastoreException;

	/**
	 * <p>Do any required clean up of a rendering plan</p>
	 * @param renderingPlan
	 */
	void destroyRenderingPlan( RenderingPlan renderingPlan );
}
