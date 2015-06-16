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

package uk.co.modularaudio.service.apprendering.util;

import uk.co.modularaudio.service.renderingplan.profiling.RenderingPlanProfileResults;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.DatastoreException;


public interface AppRenderingSession
{
	void destroy();

	void startRendering();
	boolean isRendering();
	boolean stopRendering();

	boolean testRendering( long testClientRunMillis );

	long getCurrentUiFrameTime();

	void dumpRenderingPlan() throws DatastoreException;
	void dumpProfileResults();
	RenderingPlanProfileResults getProfileResults() throws DatastoreException;

	void setApplicationGraph( MadGraphInstance<?, ?> newGraphToRender ) throws DatastoreException;
	boolean isApplicationGraphSet();
	void unsetApplicationGraph( MadGraphInstance<?, ?> oldGraphToUnset ) throws DatastoreException;

	void activateApplicationGraph() throws MadProcessingException;
	boolean isApplicationGraphActive();
	void deactivateApplicationGraph() throws MadProcessingException;

}
