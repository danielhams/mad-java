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

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadDefinition;
import uk.co.modularaudio.mads.base.feedbackdelay.mu.FeedbackDelayMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class FeedbackDelayFeedbackSliderUiJComponent extends PacSlider
	implements IMadUiControlInstance<FeedbackDelayMadDefinition, FeedbackDelayMadInstance, FeedbackDelayMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final FeedbackDelayMadUiInstance uiInstance;

	public FeedbackDelayFeedbackSliderUiJComponent( final FeedbackDelayMadDefinition definition,
			final FeedbackDelayMadInstance instance,
			final FeedbackDelayMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
		this.setPaintLabels( true );
		this.setMinimum( 0 );
		this.setMaximum( 1000 );
		// Default value
		this.setValue( 500 );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final int value )
	{
		final float valueToPass = value / 1000.0f;
		uiInstance.sendFeedbackChange( valueToPass );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void processValueChange( final int previousValue, final int newValue )
	{
		if( previousValue != newValue )
		{
			passChangeToInstanceData( newValue );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
