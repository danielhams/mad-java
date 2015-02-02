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

package uk.co.modularaudio.service.apprenderingstructure;

import uk.co.modularaudio.service.rendering.RenderingPlan;
import uk.co.modularaudio.util.audio.apprendering.AppRenderingStructure;
import uk.co.modularaudio.util.exception.DatastoreException;

/**
 * <p>Concerned with the creation and destruction of the app rendering structures.</p>
 *
 * @author dan
 */
public interface AppRenderingStructureService
{
	/**
	 * @return
	 * @throws DatastoreException
	 */
	public AppRenderingStructure createAppRenderingStructure()
		throws DatastoreException;

	/**
	 * @param renderingStructure
	 * @throws DatastoreException
	 */
	public void destroyAppRenderingStructure( AppRenderingStructure renderingStructure )
		throws DatastoreException;

	/**
	 * @return
	 */
	boolean shouldProfileRenderingJobs();

	/**
	 * @param renderingPlan
	 * @return
	 * @throws DatastoreException
	 */
	public HotspotRenderingContainer createHotspotRenderingContainer( RenderingPlan renderingPlan )
		throws DatastoreException;
}
