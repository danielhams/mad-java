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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadInstance;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorWaveShape;

public class BandLimitedOscillatorMadUiInstance extends AbstractNonConfigurableMadUiInstance<BandLimitedOscillatorMadDefinition, BandLimitedOscillatorMadInstance>
{
	static Log log = LogFactory.getLog( BandLimitedOscillatorMadUiInstance.class.getName() );
	
	public BandLimitedOscillatorMadUiInstance( BandLimitedOscillatorMadInstance instance,
			BandLimitedOscillatorMadUiDefinition componentUiDefinition )
	{
		super( componentUiDefinition.getCellSpan(), instance, componentUiDefinition );
	}

	public void sendFrequencyChange( float frequency )
	{
		sendTemporalValueToInstance( BandLimitedOscillatorIOQueueBridge.COMMAND_FREQUENCY, (long)(Float.floatToIntBits( frequency ) ) );
	}
	
	public void sendFrequencyChangeImmediate( float frequency )
	{
		sendCommandValueToInstance( BandLimitedOscillatorIOQueueBridge.COMMAND_FREQUENCY_IMMEDIATE, (long)(Float.floatToIntBits( frequency ) ) );
	}
	
	public void sendWaveChoice( OscillatorWaveShape waveShape )
	{
		sendTemporalValueToInstance( BandLimitedOscillatorIOQueueBridge.COMMAND_WAVE, waveShape.ordinal() );
	}

	public void sendPulsewidthChange( float pulsewidth )
	{
		sendTemporalValueToInstance( BandLimitedOscillatorIOQueueBridge.COMMAND_PULSE_WIDTH, (long)(Float.floatToIntBits( pulsewidth ) ) );
	}

	@Override
	public void consumeQueueEntry( BandLimitedOscillatorMadInstance instance,
			IOQueueEvent nextOutgoingEntry)
	{
	}
}
