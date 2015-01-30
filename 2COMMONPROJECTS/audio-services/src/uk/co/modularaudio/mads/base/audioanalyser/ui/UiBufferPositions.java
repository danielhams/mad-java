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

package uk.co.modularaudio.mads.base.audioanalyser.ui;

public class UiBufferPositions
{
	public boolean frozen;

	public int numSamplesToDisplay;

	public int startBufferPos;
	public int endBufferPos;
	public int startWindowOffset;
	public int endWindowOffset;

	public UiBufferPositions( final boolean frozen, final int numSamplesToDisplay, final int startBufferPos, final int endBufferPos, final int startWindowOffset, final int endWindowOffset )
	{
		this.frozen = frozen;

		this.numSamplesToDisplay = numSamplesToDisplay;
		this.startBufferPos = startBufferPos;
		this.endBufferPos = endBufferPos;

		// Assume unfrozen on init
		this.startWindowOffset = startWindowOffset;
		this.endWindowOffset = endWindowOffset;
	}

	public UiBufferPositions( final UiBufferPositions sp )
	{
		assign( sp );
	}

	public void assign( final UiBufferPositions sp )
	{
		this.frozen = sp.frozen;
		this.numSamplesToDisplay = sp.numSamplesToDisplay;
		this.startBufferPos = sp.startBufferPos;
		this.endBufferPos = sp.endBufferPos;
		this.startWindowOffset = sp.startWindowOffset;
		this.endWindowOffset = sp.endWindowOffset;
	}

	public void resetBufferPositions( final int newStartBufferPos,
		final int newEndBufferPos,
		final int newStartWindowOffset,
		final int newEndWindowOffset )
	{
		this.startBufferPos = newStartBufferPos;
		this.endBufferPos = newEndBufferPos;
		this.startWindowOffset = newStartWindowOffset;
		this.endWindowOffset = newEndWindowOffset;
	}

	public void setNumSamplesToDisplay( final int newNumSamplesToDisplay )
	{
		this.numSamplesToDisplay = newNumSamplesToDisplay;
	}
}
