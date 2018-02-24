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

package uk.co.modularaudio.mads.base.crossover.ui;

import uk.co.modularaudio.mads.base.crossover.mu.CrossoverIOQueueBridge;
import uk.co.modularaudio.mads.base.crossover.mu.CrossoverMadDefinition;
import uk.co.modularaudio.mads.base.crossover.mu.CrossoverMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class CrossoverMadUiInstance
extends NoEventsNoNameChangeNonConfigurableMadUiInstance
<CrossoverMadDefinition, CrossoverMadInstance>
{
//	private static Log log = LogFactory.getLog( CrossoverMadUiInstance.class.getName() );

	public CrossoverMadUiInstance( final CrossoverMadInstance instance,
			final CrossoverMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendFrequencyChange( final float desiredFrequency )
	{
		sendTemporalValueToInstance( CrossoverIOQueueBridge.COMMAND_FREQUENCY, Float.floatToIntBits( desiredFrequency ) );
	}

	public void send24dBChange( final boolean desired24db )
	{
		sendTemporalValueToInstance( CrossoverIOQueueBridge.COMMAND_DBTOGGLE, (desired24db ? 1 : 0 ) );
	}
}
