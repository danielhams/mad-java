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

package uk.co.modularaudio.mads.base.audiocvgen.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;

public class AudioToCvGenUiInstanceConfiguration
{
	private static final Point INPUT_CHANNELS_START = new Point( 90, 30 );

	private static final int CHANNEL_TO_CHANNEL_INCREMENT = 20;

	private static final int INPUT_TO_OUTPUT_CHANNEL_INCREMENT = 20;

	private final int[] chanIndexes;
	private final Point[] chanPosis;
	private final String[] controlNames;
	private final ControlType[] controlTypes;
	private final Class<?>[] controlClasses;
	private final Rectangle[] controlBounds;

	public AudioToCvGenUiInstanceConfiguration( final AudioToCvGenInstanceConfiguration instanceConfiguration )
	{
		final int numConversionChannels = instanceConfiguration.getNumConversionChannels();
		final int numTotalChannels = instanceConfiguration.getNumTotalChannels();

		//Setup chan indexes and positions

		chanIndexes = new int[numTotalChannels];
		chanPosis = new Point[numTotalChannels];

		int curChannelIndex = 0;
		for( int il = 0 ; il < numConversionChannels ; il++ )
		{
			chanIndexes[ curChannelIndex ] = il * 2;
			chanPosis[ curChannelIndex ] = new Point(
					INPUT_CHANNELS_START.x + (il * CHANNEL_TO_CHANNEL_INCREMENT),
					INPUT_CHANNELS_START.y );
			curChannelIndex++;

			chanIndexes[ curChannelIndex ] = (il * 2) + 1;
			chanPosis[ curChannelIndex ] = new Point(
					INPUT_CHANNELS_START.x + (il * CHANNEL_TO_CHANNEL_INCREMENT),
					INPUT_CHANNELS_START.y + INPUT_TO_OUTPUT_CHANNEL_INCREMENT );
			curChannelIndex++;
		}

		// Now the controls.

		final int numControls = 0;
		controlNames = new String[numControls];
		controlTypes = new ControlType[numControls];
		controlClasses = new Class<?>[numControls];
		controlBounds = new Rectangle[numControls];
	}

	public int[] getChanIndexes()
	{
		return chanIndexes;
	}

	public Point[] getChanPosis()
	{
		return chanPosis;
	}

	public String[] getControlNames()
	{
		return controlNames;
	}

	public ControlType[] getControlTypes()
	{
		return controlTypes;
	}

	public Class<?>[] getControlClasses()
	{
		return controlClasses;
	}

	public Rectangle[] getControlBounds()
	{
		return controlBounds;
	}

}
