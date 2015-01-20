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

package uk.co.modularaudio.util.audio.gui.mad;

import java.awt.Rectangle;

import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.DatastoreException;


public abstract class MadUiControlDefinition
	<MD extends MadDefinition<MD,MI>,
	MI extends MadInstance<MD,MI>,
	MUI extends MadUiInstance<MD, MI>> implements IMadUiControlDefinition<MD, MI, MUI>
{
	public enum ControlType
	{
		DISPLAY,
		BUTTON,
		SLIDER,
		ROTARY_LINEAR,
		ROTARY_BIPOLAR,
		ROTARY_ORBITAL,
		COMBO, 
		DOUBLE_VALUE,
		CHECKBOX,
		CUSTOM
	}
	
	protected int controlIndex = -1;
	protected ControlType controlType = null;
	protected Rectangle controlBounds = null;
	protected String controlName = null;
	
	public MadUiControlDefinition( int controlIndex, String controlName, ControlType controlType, Rectangle controlBounds )
	{
		this.controlIndex = controlIndex;
		this.controlName = controlName;
		this.controlType = controlType;
		this.controlBounds = controlBounds;
	}
	
	public int getControlIndex()
	{
		return controlIndex;
	}
	
	public String getControlName()
	{
		return controlName;
	}
	
	public Rectangle getControlBounds()
	{
		return controlBounds;
	}

	public abstract MadUiControlInstance<MD, MI, MUI> createInstance( MI instance, MUI uiInstance )
		throws DatastoreException;
}
