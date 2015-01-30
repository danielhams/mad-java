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

package uk.co.modularaudio.mads.base.xrunner.ui;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.xrunner.mu.XRunnerMadDefinition;
import uk.co.modularaudio.mads.base.xrunner.mu.XRunnerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacButton;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class DoXRunButtonUiJComponent extends PacButton
	implements IMadUiControlInstance<XRunnerMadDefinition, XRunnerMadInstance, XRunnerMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final XRunnerMadUiInstance uiInstance;

	public DoXRunButtonUiJComponent(
			final XRunnerMadDefinition definition,
			final XRunnerMadInstance instance,
			final XRunnerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
		this.setText( "XRun" );
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
		// log.debug("Received display tick");
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
	public void receiveEvent(final ActionEvent e)
	{
		uiInstance.sendDoXrun();
	}
}
