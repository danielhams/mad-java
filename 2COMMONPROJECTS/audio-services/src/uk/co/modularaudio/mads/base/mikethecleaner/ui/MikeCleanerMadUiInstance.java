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

package uk.co.modularaudio.mads.base.mikethecleaner.ui;

import uk.co.modularaudio.mads.base.mikethecleaner.mu.MikeCleanerIOQueueBridge;
import uk.co.modularaudio.mads.base.mikethecleaner.mu.MikeCleanerMadDefinition;
import uk.co.modularaudio.mads.base.mikethecleaner.mu.MikeCleanerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class MikeCleanerMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<MikeCleanerMadDefinition, MikeCleanerMadInstance>
{
//	private static Log log = LogFactory.getLog( MikeCleanerMadUiInstance.class.getName() );

	public MikeCleanerMadUiInstance( final MikeCleanerMadInstance instance,
			final MikeCleanerMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendThresholdChange( final float threshold )
	{
		sendTemporalValueToInstance( MikeCleanerIOQueueBridge.COMMAND_THRESHOLD, Float.floatToIntBits( threshold ) );
	}
}
