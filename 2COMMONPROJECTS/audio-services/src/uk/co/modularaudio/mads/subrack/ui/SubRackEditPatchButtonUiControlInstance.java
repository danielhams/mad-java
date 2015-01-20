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
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SubRackEditPatchButtonUiControlInstance 
	extends MadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>
{
	private SubRackMadInstance instance = null;
	
	private SubRackEditPatchButtonUiJComponent jComponent = null;
	
	public SubRackEditPatchButtonUiControlInstance( SubRackMadInstance instance,
			SubRackMadUiInstance uiInstance,
			SubRackEditPatchButtonUiControlDefinition def )
	{
		super( uiInstance, def );
		this.instance = instance;
		jComponent = new SubRackEditPatchButtonUiJComponent( instance.getDefinition(),  this.instance, uiInstance, def );
	}

	@Override
	public String getControlValue()
	{
		return jComponent.getControlValue();
	}

	@Override
	public void receiveControlValue( String value )
	{
		jComponent.receiveControlValue( value );
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public JComponent getControl()
	{
		return jComponent;
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
