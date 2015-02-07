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

package uk.co.modularaudio.service.rack;

import java.util.Map;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public interface RackService
{
	public final static int DEFAULT_RACK_COLS = 4;
	public final static int DEFAULT_RACK_ROWS = 20;

	// Lifecycle of the rack itself
	RackDataModel createNewRackDataModel( String rackName, String rackPath, int numCols, int numRows, boolean withRackIO )
		throws DatastoreException;
	RackDataModel createNewSubRackDataModel( String subRackName, String subRackPath, int numCols, int numRows, boolean withRackIO )
		throws DatastoreException;
	void destroyRackDataModel( RackDataModel rack ) throws DatastoreException, MAConstraintViolationException;

	void setRackName( RackDataModel rack, String newRackName );
	String getRackName( RackDataModel rack );

	void setRackDirty( RackDataModel rack, boolean dirtyFlag );
	boolean isRackDirty( RackDataModel rack );

	// Components in the rack
	RackComponent createComponentAtPosition( RackDataModel rack, MadDefinition<?,?> definition,
			Map<MadParameterDefinition,String> parameterValues, String name, int col, int row )
			throws TableCellFullException, TableIndexOutOfBoundsException, DatastoreException, MAConstraintViolationException, RecordNotFoundException;
	RackComponent createComponent( RackDataModel rack, MadDefinition<?,?> definition,
			Map<MadParameterDefinition,String> parameterValues, String name )
			throws TableCellFullException, TableIndexOutOfBoundsException, DatastoreException, MAConstraintViolationException, RecordNotFoundException;

	void renameContents( RackDataModel rack, RackComponent component, String newName )
		throws DatastoreException, MAConstraintViolationException, RecordNotFoundException;

	void moveContentsToPosition(RackDataModel rack, RackComponent component, int x, int y) throws DatastoreException, NoSuchContentsException, TableIndexOutOfBoundsException, TableCellFullException;
	String getNameForNewComponentOfType(RackDataModel rack, MadDefinition<?,?> definition)
		throws DatastoreException;

	void removeContentsFromRack( RackDataModel rackDataModel,
			RackComponent componentForAction )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException, NoSuchContentsException;

	// Access to the underlying graphs
	MadGraphInstance<?,?> getRackGraphInstance( RackDataModel rack );

	// Links in the rack
	RackLink addRackLink( RackDataModel rack, RackComponent producerRackComponent, MadChannelInstance producerChannelInstance,
			RackComponent consumerRackComponent, MadChannelInstance consumerChannelInstance  )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;
	void deleteRackLink(RackDataModel rack, RackLink rackLink) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	// Links to the IO of the rack
	RackIOLink addRackIOLink( RackDataModel rack, MadChannelInstance rackChannelInstance, RackComponent rackComponent,
			MadChannelInstance rackComponentChannelInstance )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;
	void deleteRackIOLink(RackDataModel rack, RackIOLink rackLink ) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	// Debugging
	void dumpRack(RackDataModel rack);

}
