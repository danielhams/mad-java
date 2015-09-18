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

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.cdfrequencyfilter.mu.CDFrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.cdfrequencyfilter.mu.CDFrequencyFilterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public class CDFrequencyFilterDbToggleUiJComponent extends LWTCToggleButton
	implements IMadUiControlInstance<CDFrequencyFilterMadDefinition, CDFrequencyFilterMadInstance, CDFrequencyFilterMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final CDFrequencyFilterMadUiInstance uiInstance;

	public CDFrequencyFilterDbToggleUiJComponent(
			final CDFrequencyFilterMadDefinition definition,
			final CDFrequencyFilterMadInstance instance,
			final CDFrequencyFilterMadUiInstance uiInstance,
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
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		uiInstance.send24dBChange( this.isSelected() );
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
