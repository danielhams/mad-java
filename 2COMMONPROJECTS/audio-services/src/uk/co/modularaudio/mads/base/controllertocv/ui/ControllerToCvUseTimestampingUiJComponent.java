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

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.colouredtoggle.ColouredLabelToggle;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class ControllerToCvUseTimestampingUiJComponent
	implements IMadUiControlInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
	public final static boolean DEFAULT_STATE = true;

	private final ColouredLabelToggle labelToggle;

	public ControllerToCvUseTimestampingUiJComponent(
			final ControllerToCvMadDefinition definition,
			final ControllerToCvMadInstance instance,
			final ControllerToCvMadUiInstance uiInstance,
			final int controlIndex )
	{
		final ToggleReceiver toggleReceiver = new ToggleReceiver()
		{

			@Override
			public void receiveToggle( final int toggleId, final boolean active )
			{
				uiInstance.setUseTimestamps( active );
			}
		};

		labelToggle = new ColouredLabelToggle( "Use timestamping",
				"Toggle the respecting of control timestamps",
				LWTCControlConstants.CONTROL_ROTCHO_CHOICE_BACKGROUND,
				LWTCControlConstants.CONTROL_ROTCHO_FLECHE_ACTIVE,
				LWTCControlConstants.CONTROL_ROTCHO_FLECHE_ACTIVE,
				DEFAULT_STATE,
				toggleReceiver,
				0 );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return labelToggle.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		labelToggle.receiveControlValue( value );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , int framesSinceLastTick  )
	{
	}

	@Override
	public Component getControl()
	{
		return labelToggle;
	}

	@Override
	public void destroy()
	{
	}
}
