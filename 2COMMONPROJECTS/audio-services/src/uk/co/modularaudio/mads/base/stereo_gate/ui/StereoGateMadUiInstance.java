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

package uk.co.modularaudio.mads.base.stereo_gate.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateIOQueueBridge;
import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadDefinition;
import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class StereoGateMadUiInstance
	extends AbstractNoNameChangeNonConfigurableMadUiInstance<StereoGateMadDefinition, StereoGateMadInstance>
	implements IOQueueEventUiConsumer<StereoGateMadInstance>
{
	private static Log log = LogFactory.getLog( StereoGateMadUiInstance.class.getName() );

	private final List<GateListener> gateListeners = new ArrayList<GateListener>();

	public StereoGateMadUiInstance( final StereoGateMadInstance instance,
			final StereoGateMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendOneCurveAsFloat( final int command,
			final float guiDesiredValue )
	{
		final long value = (Float.floatToIntBits( guiDesiredValue ) );
		sendTemporalValueToInstance(command, value);
		propogateChange( command, guiDesiredValue );
	}

	private void propogateChange( final int command, final float value )
	{
		for( int i =0 ; i < gateListeners.size() ; i++ )
		{
			final GateListener l = gateListeners.get( i );
			l.receiveChange( command, value );
		}
	}

	public void addGateListener( final GateListener l )
	{
		gateListeners.add( l );
	}

	public void removeGateListener( final GateListener l )
	{
		gateListeners.remove( l );
	}

	@Override
	public void doDisplayProcessing(
			final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTick )
	{
		// Receive any events from the instance first
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );

		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
	}

	@Override
	public void consumeQueueEntry( final StereoGateMadInstance instance, final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			default:
			{
				final String msg = "Unknown command receive for UI: " + nextOutgoingEntry.command;
				log.error( msg );
				break;
			}
		}

	}

	public void updateThresholdType( final int thresholdType )
	{
		sendTemporalValueToInstance(StereoGateIOQueueBridge.COMMAND_IN_THRESHOLD_TYPE, thresholdType );
	}
}
