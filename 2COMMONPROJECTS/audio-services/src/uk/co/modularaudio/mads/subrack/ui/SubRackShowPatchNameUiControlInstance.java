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

package uk.co.modularaudio.mads.subrack.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SubRackShowPatchNameUiControlInstance
	extends AbstractMadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>
{
	private SubRackMadInstance instance = null;

	private SubRackShowPatchNameUiJComponent jComponent = null;

	public SubRackShowPatchNameUiControlInstance( final SubRackMadInstance instance,
			final SubRackMadUiInstance uiInstance,
			final SubRackShowPatchNameUiControlDefinition showPatchNameControlDefinition )
	{
		super( uiInstance, showPatchNameControlDefinition );
		this.instance = instance;
		jComponent = new SubRackShowPatchNameUiJComponent( instance.getDefinition(),  this.instance, uiInstance, showPatchNameControlDefinition );
	}

	@Override
	public String getControlValue()
	{
		return jComponent.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		jComponent.receiveControlValue( value );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		jComponent.doDisplayProcessing( null, timingParameters, currentGuiTime );
	}

	@Override
	public JComponent getControl()
	{
		return jComponent;
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public void destroy()
	{
		// Do nothing
	}
}
