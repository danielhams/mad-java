package uk.co.modularaudio.mads.base.mixern.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstanceConfiguration;
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
		final int numInputChannels = instanceConfiguration.getNumInputChannels();
		final int numInputLanes = instanceConfiguration.getNumInputLanes();
		final int numChannelsPerLane = numInputChannels / numInputLanes;
		final int numOutputChannels = instanceConfiguration.getNumOutputChannels();

		//Setup chan indexes and positions

		chanIndexes = new int[numTotalChannels];
		chanPosis = new Point[numTotalChannels];

		int curChannelIndex = 0;
		for( int il = 0 ; il < numInputLanes ; il++ )
		{
			for( int ic = 0 ; ic < numChannelsPerLane ; ic++ )
			{
				chanIndexes[ curChannelIndex ] = instanceConfiguration.getIndexForInputLaneChannel( il, ic );
				chanPosis[ curChannelIndex ] = new Point( INPUT_LANES_START.x + (il * LANE_TO_LANE_INCREMENT) +
						(ic * CHANNEL_TO_CHANNEL_INCREMENT ),
						INPUT_LANES_START.y );
				curChannelIndex++;
			}
		}

		final Point lastInputChannelPos = chanPosis[ curChannelIndex - 1 ];

		for( int oc = 0 ; oc < numOutputChannels ; oc++ )
		{
			chanIndexes[ curChannelIndex ] = instanceConfiguration.getIndexForOutputChannel( oc );
			chanPosis[ curChannelIndex ] = new Point( lastInputChannelPos.x + INPUT_TO_OUTPUT_CHANNEL_INCREMENT +
					(2 * CHANNEL_TO_CHANNEL_INCREMENT) +
					(oc * CHANNEL_TO_CHANNEL_INCREMENT ),
					OUTPUT_CHANNELS_Y );

			curChannelIndex++;
		}

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
