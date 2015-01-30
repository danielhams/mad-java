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

package uk.co.modularaudio.mads.base.ms20filter.ui;

import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadDefinition;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadInstance;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterIOQueueBridge;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class Ms20FilterMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<Ms20FilterMadDefinition, Ms20FilterMadInstance>
{
//	private static Log log = LogFactory.getLog( Ms20FilterMadUiInstance.class.getName() );

	public Ms20FilterMadUiInstance( final Ms20FilterMadInstance instance,
			final Ms20FilterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendFilterModeChange( final FrequencyFilterMode desiredFilterMode )
	{
		sendTemporalValueToInstance( Ms20FilterIOQueueBridge.COMMAND_FILTER_MODE, desiredFilterMode.ordinal() );
	}

	public void sendFrequencyChange( final float desiredFrequency )
	{
		sendTemporalValueToInstance( Ms20FilterIOQueueBridge.COMMAND_FREQUENCY, Float.floatToIntBits( desiredFrequency ) );
	}

	public void sendFilterResonanceChange( final float desiredFilterResonance )
	{
		sendTemporalValueToInstance( Ms20FilterIOQueueBridge.COMMAND_FILTER_RESONANCE, Float.floatToIntBits( desiredFilterResonance ) );
	}

	public void sendThresholdChange( final float desiredThreshold )
	{
		sendTemporalValueToInstance( Ms20FilterIOQueueBridge.COMMAND_THRESHOLD, Float.floatToIntBits( desiredThreshold ) );
	}
}
