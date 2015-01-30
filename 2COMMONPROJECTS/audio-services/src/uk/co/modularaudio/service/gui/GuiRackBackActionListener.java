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

package uk.co.modularaudio.service.gui;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public interface GuiRackBackActionListener extends GuiRackFrontActionListener
{

	@Override
	void setRackDataModel( RackDataModel rackDataModel );

	void guiAddRackLink(  RackComponent producerRackComponent, MadChannelInstance producerChannelInstance,
			RackComponent consumerRackComponent, MadChannelInstance consumerChannelInstance  )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;
	void guiRemoveRackLink(RackLink existingLink) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	void guiAddRackIOLink( MadChannelInstance rackChannelInstance, RackComponent rackComponent,
			MadChannelInstance rackComponentChannelInstance )
			throws DatastoreException, MAConstraintViolationException, RecordNotFoundException;
	void guiRemoveRackIOLink( RackIOLink existingLink ) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	void destroy();

}
