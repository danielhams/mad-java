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

package uk.co.modularaudio.mads.base.waveshaper.ui;

import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperMadDefinition;
import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperMadInstance;
import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperIOQueueBridge;
import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperWaveTables.WaveType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;

public class WaveShaperMadUiInstance extends AbstractNonConfigurableMadUiInstance<WaveShaperMadDefinition, WaveShaperMadInstance>
{
	public WaveShaperMadUiInstance( WaveShaperMadInstance instance,
			WaveShaperMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}
	

	public void sendWaveChoice( WaveType waveType )
	{
		sendTemporalValueToInstance( WaveShaperIOQueueBridge.COMMAND_WAVE, waveType.ordinal() );
	}


	@Override
	public void consumeQueueEntry( WaveShaperMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
