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

package uk.co.modularaudio.service.gui.impl.guirackpanel;

import uk.co.modularaudio.service.gui.GuiRackBackActionListener;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public class RackServiceToBackActionAdaptor implements
		GuiRackBackActionListener
{
	private final RackService rackService;
	private RackDataModel rackDataModel;

	public RackServiceToBackActionAdaptor( final RackService rackService, final RackDataModel rackDataModel )
	{
		this.rackService = rackService;
		this.rackDataModel = rackDataModel;
	}

	@Override
	public void guiMoveContentsToPosition( final RackComponent component, final int x, final int y ) throws DatastoreException, TableCellFullException, TableIndexOutOfBoundsException, NoSuchContentsException
	{
		rackService.moveContentsToPosition( rackDataModel, component, x, y );
	}

	@Override
	public void guiRemoveRackLink( final RackLink rackLink ) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		rackService.deleteRackLink( rackDataModel, rackLink );
	}

	@Override
	public void guiAddRackLink(  final RackComponent producerRackComponent, final MadChannelInstance producerChannelInstance,
			final RackComponent consumerRackComponent, final MadChannelInstance consumerChannelInstance  )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		rackService.addRackLink( rackDataModel, producerRackComponent, producerChannelInstance, consumerRackComponent, consumerChannelInstance );
	}

	@Override
	public void setRackDataModel( final RackDataModel rackDataModel )
	{
		this.rackDataModel = rackDataModel;
	}

	@Override
	public void guiAddRackIOLink( final MadChannelInstance rackChannelInstance, final RackComponent rackComponent,
			final MadChannelInstance rackComponentChannelInstance )
			throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		rackService.addRackIOLink( rackDataModel, rackChannelInstance, rackComponent, rackComponentChannelInstance );
	}

	@Override
	public void guiRemoveRackIOLink( final RackIOLink existingLink )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		rackService.deleteRackIOLink( rackDataModel, existingLink );
	}

	@Override
	public void destroy()
	{
		this.rackDataModel = null;
	}

}
