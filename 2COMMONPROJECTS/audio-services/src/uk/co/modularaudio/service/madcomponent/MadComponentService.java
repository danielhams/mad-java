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

package uk.co.modularaudio.service.madcomponent;

import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public interface MadComponentService
{
	// How we can dynamically add plugins
	public void registerComponentFactory( MadComponentFactory componentFactory )
		throws DatastoreException, MAConstraintViolationException;
	public void unregisterComponentFactory( MadComponentFactory componentFactory )
		throws DatastoreException;

	// Components are classified into ALPHA, BETA and RELEASED
	// Allow a setting that determines which are returned when the service
	// is queried.
	public void setReleaseLevel( boolean showAlpha, boolean showBeta );

	// Components have a "type" - (the definitions) i.e. stereo mixer, three band stereo EQ etc
	public MadDefinitionListModel listDefinitionsAvailable()
		throws DatastoreException;

	public MadDefinition<?,?> findDefinitionById(String string)
		throws DatastoreException, RecordNotFoundException;

	// Creating an instance of a definition
	public MadInstance<?,?> createInstanceFromDefinition( MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues,
			String instanceName )
			throws DatastoreException, RecordNotFoundException, MadProcessingException;

	// Destroying an instance of a component
	public void destroyInstance( MadInstance<?,?> instance )
		throws DatastoreException, RecordNotFoundException;

}
