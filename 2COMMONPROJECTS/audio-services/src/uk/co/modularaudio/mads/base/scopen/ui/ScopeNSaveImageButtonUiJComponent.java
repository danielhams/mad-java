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

package uk.co.modularaudio.mads.base.scopen.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.scopen.mu.ScopeNMadDefinition;
import uk.co.modularaudio.mads.base.scopen.mu.ScopeNMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ScopeNSaveImageButtonUiJComponent<D extends ScopeNMadDefinition<D, I>,
	I extends ScopeNMadInstance<D, I>,
	U extends ScopeNMadUiInstance<D, I>>
	implements IMadUiControlInstance<D, I, U>
{
	private final LWTCButton button;

	public ScopeNSaveImageButtonUiJComponent(
			final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex )
	{
		button = new LWTCButton( LWTCControlConstants.STD_BUTTON_COLOURS,
				"*",
				false )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void receiveClick()
			{
				uiInstance.saveImage();
			}
		};
	}

	@Override
	public JComponent getControl()
	{
		return button;
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public void destroy()
	{
	}
}
