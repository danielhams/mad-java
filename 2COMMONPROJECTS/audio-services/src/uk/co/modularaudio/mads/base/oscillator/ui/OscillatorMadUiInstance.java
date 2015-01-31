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

package uk.co.modularaudio.mads.base.oscillator.ui;

import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorMadDefinition;
import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorMadInstance;
import uk.co.modularaudio.mads.base.oscillator.mu.OscillatorIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;

public class OscillatorMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<OscillatorMadDefinition, OscillatorMadInstance>
{
//	private static Log log = LogFactory.getLog( OscillatorMadUiInstance.class.getName() );

	public OscillatorMadUiInstance( final OscillatorMadInstance instance,
			final OscillatorMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendWaveChoice( final OscillatorWaveShape waveShape )
	{
		sendTemporalValueToInstance( OscillatorIOQueueBridge.COMMAND_IN_WAVE, waveShape.ordinal() );
	}

	public void sendFrequencyChange( final float frequency )
	{
		sendTemporalValueToInstance( OscillatorIOQueueBridge.COMMAND_IN_FREQUENCY, Float.floatToIntBits( frequency) );
	}

	public void sendFrequencyChangeImmediate( final float frequency )
	{
		sendCommandValueToInstance( OscillatorIOQueueBridge.COMMAND_IN_FREQUENCY_IMMEDIATE, Float.floatToIntBits( frequency) );
	}

	public void sendWaveShape( final OscillatorWaveShape waveShape )
	{
		sendTemporalValueToInstance( OscillatorIOQueueBridge.COMMAND_IN_WAVE, waveShape.ordinal() );
	}
}
