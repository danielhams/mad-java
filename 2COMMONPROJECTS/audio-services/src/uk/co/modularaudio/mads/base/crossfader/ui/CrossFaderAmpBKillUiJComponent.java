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

package uk.co.modularaudio.mads.base.crossfader.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public class CrossFaderAmpBKillUiJComponent extends LWTCToggleButton
	implements IMadUiControlInstance<CrossFaderMadDefinition, CrossFaderMadInstance, CrossFaderMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final CrossFaderMadUiInstance uiInstance;

	public CrossFaderAmpBKillUiJComponent(
			final CrossFaderMadDefinition definition,
			final CrossFaderMadInstance instance,
			final CrossFaderMadUiInstance uiInstance,
			final int controlIndex )
	{
		// Default value
		super( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS, "Kill B", true, false );

		this.uiInstance = uiInstance;
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
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

	@Override
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		uiInstance.setGuiKillB( newValue );
		uiInstance.recalculateAmps();
	}
}
