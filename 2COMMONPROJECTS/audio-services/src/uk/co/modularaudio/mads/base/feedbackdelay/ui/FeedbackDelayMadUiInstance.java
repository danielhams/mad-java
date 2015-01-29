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

package uk.co.modularaudio.mads.base.feedbackdelay.ui;

import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadDefinition;
import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadInstance;
import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class FeedbackDelayMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<FeedbackDelayMadDefinition, FeedbackDelayMadInstance>
{
//	private static Log log = LogFactory.getLog( FeedbackDelayMadUiInstance.class.getName() );

	public FeedbackDelayMadUiInstance( final FeedbackDelayMadInstance instance,
			final FeedbackDelayMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendDelayMillisChange( final float incomingDelayMillis )
	{
		sendTemporalValueToInstance( FeedbackDelayIOQueueBridge.COMMAND_DELAY, Float.floatToIntBits(incomingDelayMillis) );
	}

	public void sendFeedbackChange( final float incomingFeedback )
	{
		sendTemporalValueToInstance( FeedbackDelayIOQueueBridge.COMMAND_FEEDBACK, Float.floatToIntBits( incomingFeedback ) );
	}
}
