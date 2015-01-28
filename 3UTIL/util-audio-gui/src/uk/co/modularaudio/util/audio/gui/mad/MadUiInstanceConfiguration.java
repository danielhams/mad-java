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

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;


public class MadUiInstanceConfiguration
{
	private final Point[] uiChannelPositions;
	private final int[] uiChannelInstanceIndexes;

	private final Rectangle[] uiControlBounds;
	private final Class<?>[] uiControlClasses;
	private final ControlType[] uiControlTypes;
	private final String[] uiControlNames;

	public MadUiInstanceConfiguration( final Point[] uiChannelPositions,
			final int[] uiChannelInstanceIndexes,
			final Rectangle[] uiControlBounds,
			final Class<?>[] uiControlClasses,
			final ControlType[] getUiControlTypes,
			final String[] uiControlNames )
	{
		this.uiChannelPositions = uiChannelPositions;
		this.uiChannelInstanceIndexes = uiChannelInstanceIndexes;
		this.uiControlBounds = uiControlBounds;
		this.uiControlClasses = uiControlClasses;
		this.uiControlTypes = getUiControlTypes;
		this.uiControlNames = uiControlNames;
	}

	public Point[] getUiChannelPositions()
	{
		return uiChannelPositions;
	}

	public int[] getUiChannelInstanceIndexes()
	{
		return uiChannelInstanceIndexes;
	}

	public Rectangle[] getUiControlBounds()
	{
		return uiControlBounds;
	}

	public Class<?>[] getUiControlClasses()
	{
		return uiControlClasses;
	}

	public ControlType[] getUiControlTypes()
	{
		return uiControlTypes;
	}

	public String[] getUiControlNames()
	{
		return uiControlNames;
	}
}
