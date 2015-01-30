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

package uk.co.modularaudio.util.audio.gui.mad.helper;

import java.awt.Component;

import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

@SuppressWarnings("rawtypes")
public class InternalMadUiControlInstance extends AbstractMadUiControlInstance
{
	private final IMadUiControlInstance realUiControlInstance;

	@SuppressWarnings("unchecked")
	public InternalMadUiControlInstance( final AbstractMadUiInstance uiInstance,
			final MadUiControlDefinition definition,
			final IMadUiControlInstance realUiControlInstance )
	{
		super( uiInstance, definition );
		this.realUiControlInstance = realUiControlInstance;
	}

	@Override
	public String getControlValue()
	{
		return realUiControlInstance.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		realUiControlInstance.receiveControlValue( value );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		realUiControlInstance.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public Component getControl()
	{
		return realUiControlInstance.getControl();
	}

	@Override
	public void destroy()
	{
		realUiControlInstance.destroy();
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return realUiControlInstance.needsDisplayProcessing();
	}

}
