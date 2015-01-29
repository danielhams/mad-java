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

package uk.co.modularaudio.mads.base.frequencyfilter.ui;

import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadInstance;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterIOQueueBridge;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class FrequencyFilterMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<FrequencyFilterMadDefinition, FrequencyFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( FrequencyFilterMadUiInstance.class.getName() );

	public FrequencyFilterMadUiInstance( final FrequencyFilterMadInstance instance,
			final FrequencyFilterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendFilterModeChange( final FrequencyFilterMode desiredFilterMode )
	{
		sendTemporalValueToInstance( FrequencyFilterIOQueueBridge.COMMAND_FILTER_MODE, desiredFilterMode.ordinal() );
	}

	public void sendFrequencyChange( final float desiredFrequency )
	{
		sendTemporalValueToInstance( FrequencyFilterIOQueueBridge.COMMAND_FREQUENCY, Float.floatToIntBits( desiredFrequency ) );
	}

	public void sendBandwidthChange( final float desiredBandwidth )
	{
		sendTemporalValueToInstance( FrequencyFilterIOQueueBridge.COMMAND_BANDWIDTH, Float.floatToIntBits( desiredBandwidth ) );
	}

	public void send24dBChange( final boolean desired24db )
	{
		sendTemporalValueToInstance( FrequencyFilterIOQueueBridge.COMMAND_DBTOGGLE, (desired24db ? 1 : 0 ) );
	}
}
