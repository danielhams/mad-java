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

package uk.co.modularaudio.mads.base.imixern.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.imixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;

public class MixerNUiInstanceConfiguration
{
	public static final Point INPUT_LANES_START = new Point( 35, 280 );

	public static final int LANE_TO_LANE_INCREMENT = 115;
	public static final int CHANNEL_TO_CHANNEL_INCREMENT = 20;

	public static final int INPUT_TO_OUTPUT_CHANNEL_INCREMENT = 100;

	public static final int OUTPUT_CHANNELS_Y = 50;

	private final int[] chanIndexes;
	private final Point[] chanPosis;
	private final String[] controlNames;
	private final ControlType[] controlTypes;
	private final Class<?>[] controlClasses;
	private final Rectangle[] controlBounds;

	public MixerNUiInstanceConfiguration( final MixerNInstanceConfiguration instanceConfiguration,
			final Class<?> laneControl,
			final Class<?> masterControl )
	{
		final int numMixerLanes = instanceConfiguration.getNumMixerLanes();
		final int numTotalChannels = instanceConfiguration.getNumTotalChannels();
		final int numInputLanes = instanceConfiguration.getNumInputLanes();

		//Setup chan indexes and positions

		chanIndexes = new int[numTotalChannels];
		chanPosis = new Point[numTotalChannels];

		int curChannelIndex = 0;
		for( int il = 0 ; il < numInputLanes ; il++ )
		{
			chanIndexes[ curChannelIndex ] = (il + 1) * 2;
			chanPosis[ curChannelIndex ] = new Point( INPUT_LANES_START.x + (il * LANE_TO_LANE_INCREMENT),
					INPUT_LANES_START.y );
			curChannelIndex++;

			chanIndexes[ curChannelIndex ] = ((il + 1) * 2) + 1;
			chanPosis[ curChannelIndex ] = new Point( INPUT_LANES_START.x + (il * LANE_TO_LANE_INCREMENT) +
						CHANNEL_TO_CHANNEL_INCREMENT,
					INPUT_LANES_START.y );
			curChannelIndex++;
}

		final Point lastInputChannelPos = chanPosis[ curChannelIndex - 1 ];

		chanIndexes[ curChannelIndex ] = 0;
		chanPosis[ curChannelIndex ] = new Point( lastInputChannelPos.x + INPUT_TO_OUTPUT_CHANNEL_INCREMENT +
				(2 * CHANNEL_TO_CHANNEL_INCREMENT),
				OUTPUT_CHANNELS_Y );

		curChannelIndex++;

		chanIndexes[ curChannelIndex ] = 1;
		chanPosis[ curChannelIndex ] = new Point( lastInputChannelPos.x + INPUT_TO_OUTPUT_CHANNEL_INCREMENT +
					(2 * CHANNEL_TO_CHANNEL_INCREMENT) + CHANNEL_TO_CHANNEL_INCREMENT,
				OUTPUT_CHANNELS_Y );

		curChannelIndex++;

		// Now the controls.
		// We put the master IO first then the other channels get appropriate
		// indexes

		final int numControls = numMixerLanes + 1;
		controlNames = new String[numControls];
		controlTypes = new ControlType[numControls];
		controlClasses = new Class<?>[numControls];
		controlBounds = new Rectangle[numControls];

		int controlIndex = 0;

		final int masterStartX = INPUT_LANES_START.x + ((LANE_TO_LANE_INCREMENT) * (numInputLanes-1) ) +
				(INPUT_TO_OUTPUT_CHANNEL_INCREMENT + CHANNEL_TO_CHANNEL_INCREMENT + 10);
		final int masterWidth = LANE_TO_LANE_INCREMENT - 4;
		final int masterStartY = 20;
		final int masterHeight = INPUT_LANES_START.y;

		controlNames[controlIndex] = "Master Lane";
		controlTypes[controlIndex] = ControlType.CUSTOM;
		controlClasses[controlIndex] = masterControl;
		controlBounds[controlIndex] = new Rectangle( masterStartX, masterStartY, masterWidth, masterHeight );

		controlIndex++;

		final int laneStartX = 8;
		final int laneStartY = 30;
		for( int i = 0 ; i < numMixerLanes ; ++i )
		{
			controlNames[controlIndex] = "Mixer Lane " + i;
			controlTypes[controlIndex] = ControlType.CUSTOM;
			controlClasses[controlIndex] = laneControl;
			controlBounds[controlIndex] = new Rectangle( laneStartX + (LANE_TO_LANE_INCREMENT * i ),
					laneStartY,
					LANE_TO_LANE_INCREMENT - 4,
					INPUT_LANES_START.y -10 );

			controlIndex++;
		}

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
