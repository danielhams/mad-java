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
	<D extends MadDefinition<D,I>,
	I extends MadInstance<D,I>,
	U extends AbstractMadUiInstance<D, I>> implements IMadUiControlDefinition<D, I, U>
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

	protected final int controlIndex;
	protected final ControlType controlType;
	protected final Rectangle controlBounds;
	protected final String controlName;

	public MadUiControlDefinition( final int controlIndex, final String controlName, final ControlType controlType, final Rectangle controlBounds )
	{
		this.controlIndex = controlIndex;
		this.controlName = controlName;
		this.controlType = controlType;
		this.controlBounds = controlBounds;
	}

	@Override
	public int getControlIndex()
	{
		return controlIndex;
	}

	@Override
	public String getControlName()
	{
		return controlName;
	}

	@Override
	public Rectangle getControlBounds()
	{
		return controlBounds;
	}

	@Override
	public abstract AbstractMadUiControlInstance<D, I, U> createInstance( I instance, U uiInstance )
		throws DatastoreException;
}
