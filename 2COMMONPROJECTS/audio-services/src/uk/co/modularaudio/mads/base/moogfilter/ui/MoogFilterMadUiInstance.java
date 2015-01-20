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

package uk.co.modularaudio.mads.base.moogfilter.ui;

import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadInstance;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterIOQueueBridge;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;

public class MoogFilterMadUiInstance extends AbstractNonConfigurableMadUiInstance<MoogFilterMadDefinition, MoogFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( MoogFilterMadUiInstance.class.getName() );
	
	public MoogFilterMadUiInstance( MoogFilterMadInstance instance,
			MoogFilterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendFilterModeChange( FrequencyFilterMode desiredFilterMode )
	{
		sendTemporalValueToInstance( MoogFilterIOQueueBridge.COMMAND_FILTER_MODE, desiredFilterMode.ordinal() );
	}

	public void sendFrequencyChange( float desiredFrequency )
	{
		sendTemporalValueToInstance( MoogFilterIOQueueBridge.COMMAND_FREQUENCY, Float.floatToIntBits( desiredFrequency ) );
	}

	public void sendBandwidthChange( float desiredBandwidth )
	{
		sendTemporalValueToInstance( MoogFilterIOQueueBridge.COMMAND_Q, Float.floatToIntBits( desiredBandwidth ) );
	}

	@Override
	public void consumeQueueEntry( MoogFilterMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
