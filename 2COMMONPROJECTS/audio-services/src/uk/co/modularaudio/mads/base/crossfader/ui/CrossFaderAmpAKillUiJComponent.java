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

import java.awt.Font;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacToggleButton;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class CrossFaderAmpAKillUiJComponent extends PacToggleButton
	implements IMadUiControlInstance<CrossFaderMadDefinition, CrossFaderMadInstance, CrossFaderMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private CrossFaderMadUiInstance uiInstance = null;

	public CrossFaderAmpAKillUiJComponent(
			CrossFaderMadDefinition definition,
			CrossFaderMadInstance instance,
			CrossFaderMadUiInstance uiInstance,
			int controlIndex )
	{
		// Default value
		super( false );

		this.uiInstance = uiInstance;
		this.setOpaque( true );
//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );
		this.setText( "Kill A" );
	}

	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( boolean selected )
	{
		if (selected != uiInstance.guiKillA)
		{
			uiInstance.guiKillA = selected;
			if (uiInstance.guiKillA)
			{
				uiInstance.guiDesiredAmpA = 0.0f;
			}
			else
			{
				// Use the value the slider set
				uiInstance.guiDesiredAmpA = uiInstance.guiAmpA;
			}
		}
		uiInstance.recalculateAmps();
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveUpdateEvent( boolean previousValue, boolean newValue )
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
