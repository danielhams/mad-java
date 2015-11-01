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

package uk.co.modularaudio.mads.base.limiter.ui;

import uk.co.modularaudio.mads.base.limiter.mu.LimiterIOQueueBridge;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class LimiterMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<LimiterMadDefinition, LimiterMadInstance>
{
//	private static Log log = LogFactory.getLog( LimiterMadUiInstance.class.getName() );

	public LimiterMadUiInstance( final LimiterMadInstance instance, final LimiterMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendKneeChange( final float incomingKnee )
	{
		final float asValue = AudioMath.dbToLevelF( incomingKnee );
		sendTemporalValueToInstance( LimiterIOQueueBridge.COMMAND_KNEE, Float.floatToIntBits( asValue ) );
	}

	public void sendFalloffChange( final float incomingFalloff )
	{
		sendTemporalValueToInstance( LimiterIOQueueBridge.COMMAND_FALLOFF, Float.floatToIntBits( incomingFalloff ) );
	}

	public void setUseHardLimit( final boolean active )
	{
		sendTemporalValueToInstance( LimiterIOQueueBridge.COMMAND_USE_HARD_LIMIT, active ? 1 : 0 );
	}
}
