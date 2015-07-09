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

package uk.co.modularaudio.mads.base.controllertocv.ui;

import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ControllerToCvLearnButtonUiJComponent
	implements IMadUiControlInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
	private static Log log = LogFactory.getLog( ControllerToCvLearnButtonUiJComponent.class.getName() );

	private final LWTCButton theButton;

	public ControllerToCvLearnButtonUiJComponent(
			final ControllerToCvMadDefinition definition,
			final ControllerToCvMadInstance instance,
			final ControllerToCvMadUiInstance uiInstance,
			final int controlIndex )
	{
		theButton = new LWTCButton( LWTCControlConstants.STD_BUTTON_COLOURS, "Learn", true )
		{
			private static final long serialVersionUID = -8901816933509201750L;

			@Override
			public void receiveClick()
			{
				log.trace("Would activate midi learn");
			}
		};
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
	public Component getControl()
	{
		return theButton;
	}

	@Override
	public void destroy()
	{
	}

}
