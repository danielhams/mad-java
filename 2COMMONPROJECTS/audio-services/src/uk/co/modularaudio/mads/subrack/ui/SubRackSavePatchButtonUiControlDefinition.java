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

import java.awt.Rectangle;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;

// Yes, it's a hack - java generics blow
public class SubRackSavePatchButtonUiControlDefinition extends MadUiControlDefinition<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>
{
	public final static String NAME = "SavePatch";
	public final static ControlType TYPE = ControlType.BUTTON;

	public SubRackSavePatchButtonUiControlDefinition( int controlIndex, Rectangle controlBounds )
	{
		super( controlIndex, NAME, TYPE, controlBounds );
	}

	@Override
	public AbstractMadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance> createInstance(
			SubRackMadInstance instance, SubRackMadUiInstance uiInstance )
	{
		return new SubRackSavePatchButtonUiControlInstance( instance, uiInstance, this );
	}
}
