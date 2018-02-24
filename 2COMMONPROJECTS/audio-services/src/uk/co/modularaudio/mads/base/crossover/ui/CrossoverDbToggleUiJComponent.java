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

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.crossover.mu.CrossoverMadDefinition;
import uk.co.modularaudio.mads.base.crossover.mu.CrossoverMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public class CrossoverDbToggleUiJComponent extends LWTCToggleButton
	implements IMadUiControlInstance<CrossoverMadDefinition, CrossoverMadInstance, CrossoverMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final CrossoverMadUiInstance uiInstance;

	public CrossoverDbToggleUiJComponent(
			final CrossoverMadDefinition definition,
			final CrossoverMadInstance instance,
			final CrossoverMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS, "Toggle 24dB", true, false );
		this.uiInstance = uiInstance;
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , final int framesSinceLastTick )
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		uiInstance.send24dBChange( newValue );
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
