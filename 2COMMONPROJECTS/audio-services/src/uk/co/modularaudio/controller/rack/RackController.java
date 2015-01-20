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

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public interface RackController
{
	RackDataModel createNewRackDataModel( String rackName, String rackPath, int numCols, int numRows, boolean withRackIO ) throws DatastoreException;

	RackComponent createComponentAtPosition( RackDataModel rack, MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues, String name, int col, int row )
		throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException, DatastoreException,
			MAConstraintViolationException, RecordNotFoundException;

	RackComponent createComponent( RackDataModel rack, MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues, String name )
			throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException, DatastoreException,
			MAConstraintViolationException, RecordNotFoundException;

	String getNameForNewComponentOfType( RackDataModel rackDataModel, MadDefinition<?,?> typeToAdd) throws DatastoreException;

	void moveContentsToPosition(RackDataModel rackDataModel, RackComponent component, int x, int y)
		throws DatastoreException, NoSuchContentsException, TableIndexOutOfBoundsException, ContentsAlreadyAddedException, TableCellFullException;

	RackLink addRackLink( RackDataModel rack, RackComponent producerRackComponent, MadChannelInstance producerChannelInstance,
			RackComponent consumerRackComponent, MadChannelInstance consumerChannelInstance )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	void removeRackLink( RackDataModel rackDataModel, RackLink rackLink)
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	// Debugging methods
	void dumpRack( RackDataModel rdm );

	RackDataModel loadRackFromFile(String filename) throws DatastoreException, IOException;

	void saveRackToFile(RackDataModel dataModel, String filename ) throws DatastoreException, IOException;

	void destroyRackDataModel(RackDataModel rackDataModel) throws DatastoreException, MAConstraintViolationException;

	MadGraphInstance<?,?> getRackGraphInstance( RackDataModel rack );


}
