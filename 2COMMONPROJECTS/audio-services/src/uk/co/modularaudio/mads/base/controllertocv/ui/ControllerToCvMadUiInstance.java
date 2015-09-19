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

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerEventMapping;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvIOQueueBridge;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.mads.base.controllertocv.ui.ControllerToCvInterpolationChoiceUiJComponent.InterpolationChoice;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class ControllerToCvMadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance>
{
	private static Log log = LogFactory.getLog( ControllerToCvMadUiInstance.class.getName() );

	private final ArrayList<ControllerToCvLearnListener> learnListeners = new ArrayList<ControllerToCvLearnListener>();

	public ControllerToCvMadUiInstance( final ControllerToCvMadInstance instance,
			final ControllerToCvMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTick )
	{
		// Process incoming queue messages before we let the controls have a chance to process;
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );
		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
	}

	@Override
	public void consumeQueueEntry( final ControllerToCvMadInstance instance, final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case ControllerToCvIOQueueBridge.COMMAND_OUT_LEARNT_CONTROLLER:
			{
				final long value = nextOutgoingEntry.value;
				final int channelBits = (int)(value >> 32);
				final int controllerBits = (int)(value & 0xffff);
				log.trace("Received learnt channel: " + channelBits + " and controller " + controllerBits );

				for( final ControllerToCvLearnListener ll : learnListeners )
				{
					ll.receiveLearntController( channelBits, controllerBits );
				}
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown command received: " + nextOutgoingEntry.command );
				}
			}
		}
	}

	public void sendMapping( final ControllerEventMapping mappingToUse )
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_IN_EVENT_MAPPING, mappingToUse.ordinal() );
	}

	public void sendSelectedChannel( final int channelNumber )
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_IN_CHANNEL_NUMBER, channelNumber );
	}

	public void sendSelectedController( final int controller )
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_IN_CONTROLLER_NUMBER, controller );
	}

	public void sendLearn()
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_IN_BEGIN_LEARN, 0 );
	}

	public void addLearnListener( final ControllerToCvLearnListener ll )
	{
		learnListeners.add( ll );
	}

	public void sendInterpolationChoice( final InterpolationChoice ic )
	{
		sendTemporalValueToInstance( ControllerToCvIOQueueBridge.COMMAND_IN_INTERPOLATION, ic.ordinal() );
	}
}
