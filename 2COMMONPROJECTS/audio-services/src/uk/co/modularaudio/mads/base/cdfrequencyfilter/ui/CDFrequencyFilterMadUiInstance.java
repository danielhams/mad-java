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

package uk.co.modularaudio.mads.base.cdfrequencyfilter.ui;

import uk.co.modularaudio.mads.base.cdfrequencyfilter.mu.CDFrequencyFilterIOQueueBridge;
import uk.co.modularaudio.mads.base.cdfrequencyfilter.mu.CDFrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.cdfrequencyfilter.mu.CDFrequencyFilterMadInstance;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class CDFrequencyFilterMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<CDFrequencyFilterMadDefinition, CDFrequencyFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( CDFrequencyFilterMadUiInstance.class.getName() );

	public CDFrequencyFilterMadUiInstance( final CDFrequencyFilterMadInstance instance,
			final CDFrequencyFilterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendFilterModeChange( final FrequencyFilterMode desiredFilterMode )
	{
		sendTemporalValueToInstance( CDFrequencyFilterIOQueueBridge.COMMAND_FILTER_MODE, desiredFilterMode.ordinal() );
	}

	public void sendFrequencyChange( final float desiredFrequency )
	{
		sendTemporalValueToInstance( CDFrequencyFilterIOQueueBridge.COMMAND_FREQUENCY, Float.floatToIntBits( desiredFrequency ) );
	}

	public void send24dBChange( final boolean desired24db )
	{
		sendTemporalValueToInstance( CDFrequencyFilterIOQueueBridge.COMMAND_DB_TOGGLE, (desired24db ? 1 : 0 ) );
	}
}
