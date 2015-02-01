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

package uk.co.modularaudio.controller.rack.impl;

import java.io.IOException;
import java.util.Map;

import uk.co.modularaudio.controller.rack.RackController;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public class RackControllerImpl implements ComponentWithLifecycle, RackController
{
	private RackService rackService;
	private RackMarshallingService rackMarshallingService;

	public void setRackService(final RackService rackService)
	{
		this.rackService = rackService;
	}

	public void setRackMarshallingService(final RackMarshallingService rackMarshallingService)
	{
		this.rackMarshallingService = rackMarshallingService;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public RackDataModel createNewRackDataModel( final String rackName, final String rackPath, final int numCols, final int numRows, final boolean withRackIO ) throws DatastoreException
	{
		return rackService.createNewRackDataModel( rackName, rackPath, numCols, numRows, withRackIO );
	}

	@Override
	public void dumpRack( final RackDataModel rdm )
	{
		rackService.dumpRack( rdm );
	}

	@Override
	public RackDataModel loadRackFromFile(final String filename) throws DatastoreException, IOException
	{
		return rackMarshallingService.loadRackFromFile(filename);
	}

	@Override
	public void saveRackToFile(final RackDataModel dataModel, final String filename) throws DatastoreException, IOException
	{
		rackMarshallingService.saveRackToFile(dataModel, filename);
		rackService.setRackDirty( dataModel, false );
	}

	@Override
	public void destroyRackDataModel(final RackDataModel rackDataModel) throws DatastoreException, MAConstraintViolationException
	{
		rackService.destroyRackDataModel( rackDataModel );
	}

	@Override
	public MadGraphInstance<?,?> getRackGraphInstance( final RackDataModel rack )
	{
		return rackService.getRackGraphInstance( rack );
	}

	@Override
	public RackComponent createComponent( final RackDataModel rack, final MadDefinition<?,?> definition,
			final Map<MadParameterDefinition, String> parameterValues, final String name )
			throws TableCellFullException, TableIndexOutOfBoundsException, DatastoreException,
			MAConstraintViolationException, RecordNotFoundException
	{
		return rackService.createComponent( rack, definition, parameterValues, name );
	}
}
