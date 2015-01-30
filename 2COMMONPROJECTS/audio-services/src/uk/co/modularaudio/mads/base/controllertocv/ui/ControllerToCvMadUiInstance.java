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

package uk.co.modularaudio.mads.base.controllertocv.ui;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerEventMapping;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class ControllerToCvMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance>
{
//	private static Log log = LogFactory.getLog( ControllerToCvMadUiInstance.class.getName() );

	public ControllerToCvMadUiInstance( final ControllerToCvMadInstance instance,
			final ControllerToCvMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendMapping( final ControllerEventMapping mappingToUse )
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_EVENT_MAPPING, mappingToUse.ordinal() );
	}

	public void sendSelectedChannel( final int channelNumber )
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_CHANNEL_NUMBER, channelNumber );
	}

	public void sendSelectedController( final int controller )
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_CONTROLLER_NUMBER, controller );
	}
}
