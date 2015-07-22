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

package uk.co.modularaudio.controller.rack;

import java.io.IOException;
import java.util.Map;

import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

/**
 * <p>Entry point for the macro operations that may be performed on a "rack".</p>
 * <p>The rack controller is a vertical responsibility controller that delegates
 * and / or coordinates work as appropriate from services that implement
 * the required functionality.</p>
 *
 * @author dan
 */
public interface RackController
{
	/**
	 * <p>Creates a new rack with the requested parameters.</p>
	 * @see RackService#createNewRackDataModel(String, String, int, int, boolean)
	 */
	RackDataModel createNewRackDataModel( String rackName, String rackPath, int numCols, int numRows, boolean withRackIO ) throws DatastoreException;

	/**
	 * <p>Create a component within a rack and return it should
	 * the caller need to do any operations on it.</p>
	 * @see RackService#createComponent(RackDataModel, MadDefinition, Map, String)
	 */
	RackComponent createComponent( RackDataModel rack, MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues, String name )
			throws TableCellFullException, TableIndexOutOfBoundsException, DatastoreException,
			MAConstraintViolationException, RecordNotFoundException;

	/**
	 * <p>Load a rack from a filesystem file.</p>
	 * @see RackMarshallingService#loadBaseRackFromFile(String)
	 */
	RackDataModel loadBaseRackFromFile(String filename) throws DatastoreException, IOException;

	/**
	 * <p>Load a sub rack from a filesystem file.</p>
	 * @see RackMarshallingService#loadSubRackFromFile(String)
	 */
	RackDataModel loadSubRackFromFile(String filename) throws DatastoreException, IOException;

	/**
	 * <p>Save a rack to the filesystem file.</p>
	 * @see RackMarshallingService#saveBaseRackToFile(RackDataModel, String)
	 */
	void saveBaseRackToFile(RackDataModel dataModel, String filename ) throws DatastoreException, IOException;

	/**
	 * <p>Save a sub rack to the filesystem file.</p>
	 * @see RackMarshallingService#saveSubRackToFile(RackDataModel, String)
	 */
	void saveSubRackToFile(RackDataModel dataModel, String filename ) throws DatastoreException, IOException;

	/**
	 * <p>Perform any clean up needed on a rack.</p>
	 * @see RackService#destroyRackDataModel(RackDataModel)
	 */
	void destroyRackDataModel(RackDataModel rackDataModel) throws DatastoreException, MAConstraintViolationException;

	/**
	 * <p>Obtain the internal MadGraphInstance inside a rack.</p>
	 * @see RackService#getRackGraphInstance(RackDataModel)
	 */
	MadGraphInstance<?,?> getRackGraphInstance( RackDataModel rack );

	// Debugging methods
	/**
	 * <p>Output the rack contents to the console.</p>
	 * @see RackService#dumpRack(RackDataModel)
	 */
	void dumpRack( RackDataModel rdm );
}
