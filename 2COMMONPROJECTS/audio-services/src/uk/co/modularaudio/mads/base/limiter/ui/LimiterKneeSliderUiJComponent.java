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

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class LimiterKneeSliderUiJComponent extends PacSlider
		implements
		IMadUiControlInstance<LimiterMadDefinition, LimiterMadInstance, LimiterMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final LimiterMadUiInstance uiInstance;

	public LimiterKneeSliderUiJComponent(
			final LimiterMadDefinition definition,
			final LimiterMadInstance instance,
			final LimiterMadUiInstance uiInstance,
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
		// Convert it into a float
		final float valToSend = (value / 1000.0f);
		uiInstance.sendKneeChange( valToSend );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveControlValue( final String strValue )
	{
		super.receiveControlValue( strValue );
		final float initialValue = this.getValue() / 1000.0f;
		uiInstance.sendKneeChange( initialValue );
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
