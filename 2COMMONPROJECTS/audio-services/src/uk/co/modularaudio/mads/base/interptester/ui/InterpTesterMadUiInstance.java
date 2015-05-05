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

package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterIOQueueBridge;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public class InterpTesterMadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<InterpTesterMadDefinition, InterpTesterMadInstance>
{
	private static Log log = LogFactory.getLog( InterpTesterMadUiInstance.class.getName() );

	private ModelChangeReceiver modelChangeReceiver;
	private PerfDataReceiver perfDataReceiver;

	public static final LWTCSliderViewColors SLIDER_COLORS = getSliderColors();

	private static final LWTCSliderViewColors getSliderColors()
	{
		final Color bgColor = Color.black;
		final Color fgColor = Color.white;
		final Color indicatorColor = null;
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color labelColor = LWTCControlConstants.CONTROL_LABEL_BORDER;
		final Color unitsColor = labelColor;

		return new LWTCSliderViewColors( bgColor,
				fgColor,
				indicatorColor,
				textboxBgColor,
				textboxFgColor,
				selectionColor,
				selectedTextColor,
				labelColor,
				unitsColor );
	}

	public InterpTesterMadUiInstance( final InterpTesterMadInstance instance,
			final InterpTesterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void setChaseValueMillis( final float chaseMillis )
	{
		final long floatIntBits = Float.floatToIntBits( chaseMillis );
		sendTemporalValueToInstance( InterpTesterIOQueueBridge.COMMAND_CHASE_MILLIS, floatIntBits );
	}

	@Override
	public void consumeQueueEntry( final InterpTesterMadInstance instance, final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case InterpTesterIOQueueBridge.COMMAND_TO_UI_NONE_NANOS:
			{
				perfDataReceiver.setNoneNanos( nextOutgoingEntry.value );
				break;
			}
			case InterpTesterIOQueueBridge.COMMAND_TO_UI_LIN_NANOS:
			{
				perfDataReceiver.setLNanos( nextOutgoingEntry.value );
				break;
			}
			case InterpTesterIOQueueBridge.COMMAND_TO_UI_HH_NANOS:
			{
				perfDataReceiver.setHHNanos( nextOutgoingEntry.value );
				break;
			}
			case InterpTesterIOQueueBridge.COMMAND_TO_UI_SD_NANOS:
			{
				perfDataReceiver.setSDNanos( nextOutgoingEntry.value );
				break;
			}
			case InterpTesterIOQueueBridge.COMMAND_TO_UI_LP_NANOS:
			{
				perfDataReceiver.setLPNanos( nextOutgoingEntry.value );
				break;
			}
			case InterpTesterIOQueueBridge.COMMAND_TO_UI_SDD_NANOS:
			{
				perfDataReceiver.setSDDNanos( nextOutgoingEntry.value );
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown command received: " + nextOutgoingEntry.command);
				}
				break;
			}
		}

	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTick )
	{
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );
		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
	}

	public void setValueModelIndex( final int selectedIndex )
	{
		modelChangeReceiver.receiveNewModelIndex( selectedIndex );
		sendTemporalValueToInstance( InterpTesterIOQueueBridge.COMMAND_SET_MODEL, selectedIndex );
	}

	public void setModelChangeReceiver( final ModelChangeReceiver changeReceiver )
	{
		this.modelChangeReceiver = changeReceiver;
	}

	public void setPerfDataReceiver( final PerfDataReceiver dataReceiver )
	{
		this.perfDataReceiver = dataReceiver;
	}

	public void setValue( final float newValue )
	{
//		log.debug("Received new value: " + MathFormatter.slowFloatPrint( newValue, 5, true ) );

		final int intBits = Float.floatToIntBits( newValue );
		sendTemporalValueToInstance( InterpTesterIOQueueBridge.COMMAND_AMP, intBits );
		sendCommandValueToInstance( InterpTesterIOQueueBridge.COMMAND_AMP_NOTS, intBits );
		
//		long curTimestamp = System.nanoTime();
//		log.debug("Sending new value at timestamp " + curTimestamp );
	}

	public void sendUiActive( final boolean active )
	{
		sendCommandValueToInstance( InterpTesterIOQueueBridge.COMMAND_UIACTIVE, (active ? 1 : 0 ) );

	}
}
