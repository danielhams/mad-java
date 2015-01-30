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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.popup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public class DeleteComponentYesNoCallback implements YesNoQuestionDialogCallback
{
	private static Log log = LogFactory.getLog( DeleteComponentYesNoCallback.class.getName() );

	private final RackService rackService;
	private final RackDataModel rackDataModel;
	private final RackComponent componentForAction;

	public DeleteComponentYesNoCallback( final RackService rackService,
			final RackDataModel rackDataModel,
			final RackComponent componentForAction )
	{
		this.rackService = rackService;
		this.rackDataModel = rackDataModel;
		this.componentForAction = componentForAction;
	}

	@Override
	public void receiveDialogResultValue( final int value )
	{
		try
		{
			if( value == 0 )
			{
				log.debug("Deleting it...");
				rackService.removeContentsFromRack( rackDataModel, componentForAction );
			}
		}
		catch (final Exception e)
		{
			final String msg = "DeleteComponentCallback caught exception removing contents from rack: " + e.toString();
			log.error( msg, e );
		}
	}

}
