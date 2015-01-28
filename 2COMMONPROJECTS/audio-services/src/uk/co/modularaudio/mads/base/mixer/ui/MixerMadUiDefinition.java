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

package uk.co.modularaudio.mads.base.mixer.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import uk.co.modularaudio.mads.base.mixer.mu.MixerMadDefinition;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadInstance;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadInstanceConfiguration;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class MixerMadUiDefinition
	extends AbstractConfigurableMadUiDefinition<MixerMadDefinition, MixerMadInstance, MixerMadUiInstance>
{
	private static final Class<MixerMadUiInstance> INSTANCE_CLASS = MixerMadUiInstance.class;

	public static final Point INPUT_LANES_START = new Point( 35, 280 );

	public static final int LANE_TO_LANE_INCREMENT = 115;
	public static final int CHANNEL_TO_CHANNEL_INCREMENT = 20;

	public static final int INPUT_TO_OUTPUT_CHANNEL_INCREMENT = 100;

	public static final int OUTPUT_CHANNELS_Y = 50;

	public static final Color LANE_BG_COLOR = new Color( 57, 63, 63 );
	public static final Color MASTER_BG_COLOR = new Color( 0.6f, 0.6f, 0.6f );

	private static final Span DEFAULT_SPAN = new Span(1,1);

	public MixerMadUiDefinition( final BufferedImageAllocator bia,
			final MixerMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia, definition, cif, imageRoot, INSTANCE_CLASS );
	}

	protected Point[] getUiChannelPositionsForAui( final MixerMadInstanceConfiguration instanceConfiguration,
			final int numInputLanes,
			final int numChannelsPerLane,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		Point[] retVal;

		int curChannelIndex = 0;
		retVal = new Point[ numTotalChannels ];
		for( int il = 0 ; il < numInputLanes ; il++ )
		{
			for( int ic = 0 ; ic < numChannelsPerLane ; ic++ )
			{
				retVal[ curChannelIndex++ ] = new Point( INPUT_LANES_START.x + (il * LANE_TO_LANE_INCREMENT) +
						(ic * CHANNEL_TO_CHANNEL_INCREMENT ),
						INPUT_LANES_START.y );
			}
		}

		final Point lastInputChannelPoint = retVal[ curChannelIndex - 1 ];
		for( int oc = 0 ; oc < numOutputChannels ; oc++ )
		{
			retVal[ curChannelIndex++ ] = new Point( lastInputChannelPoint.x + INPUT_TO_OUTPUT_CHANNEL_INCREMENT +
					(2 * CHANNEL_TO_CHANNEL_INCREMENT) +
					(oc * CHANNEL_TO_CHANNEL_INCREMENT ),
					OUTPUT_CHANNELS_Y );
		}

		return retVal;
	}

	private int[] getUiChannelInstanceIndexesForAui( final MixerMadInstanceConfiguration instanceConfiguration,
			final int numInputLanes,
			final int numChannelsPerLane,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		int[] retVal;

		int curChannelIndex = 0;
		retVal = new int[ numTotalChannels ];
		for( int il = 0 ; il < numInputLanes ; il++ )
		{
			for( int ic = 0 ; ic < numChannelsPerLane ; ic++ )
			{
				retVal[ curChannelIndex++ ] = instanceConfiguration.getIndexForInputLaneChannel( il, ic );
			}
		}

		for( int oc = 0 ; oc < numOutputChannels ; oc++ )
		{
			retVal[ curChannelIndex++ ] = instanceConfiguration.getIndexForOutputChannel( oc );
		}

		return retVal;
	}

	@Override
	protected MadUiInstanceConfiguration getUiInstanceConfiguration( final MixerMadInstance instance )
	{
		final MixerMadInstanceConfiguration instanceConfiguration = instance.getInstanceConfiguration();

		final int numInputLanes = instanceConfiguration.getNumInputLanes();
		final int numChannelsPerLane = instanceConfiguration.getNumChannelsPerLane();
		final int numOutputChannels = instanceConfiguration.getNumOutputChannels();
		final int numTotalChannels = instanceConfiguration.getNumTotalChannels();

		final int[] uiChannelInstanceIndexes = getUiChannelInstanceIndexesForAui( instanceConfiguration, numInputLanes, numChannelsPerLane, numOutputChannels, numTotalChannels );

		final Point[] uiChannelPositions = getUiChannelPositionsForAui( instanceConfiguration, numInputLanes, numChannelsPerLane, numOutputChannels, numTotalChannels );

		final Rectangle[] uiComponentPositions = getUiComponentPositionsForAui( instanceConfiguration, numInputLanes, numChannelsPerLane, numOutputChannels, numTotalChannels );
		final Class<?>[] uiComponentClasses = getUiComponentClassesForAui( instanceConfiguration, numInputLanes, numChannelsPerLane, numOutputChannels, numTotalChannels );
		final ControlType[] uiComponentControlTypes = getUiComponentControlTypes( instanceConfiguration, numInputLanes, numChannelsPerLane, numOutputChannels, numTotalChannels );
		final String[] uiComponentNames = getUiComponentNamesForAui( instanceConfiguration, numInputLanes, numChannelsPerLane, numOutputChannels, numTotalChannels );

		return new MadUiInstanceConfiguration( uiChannelPositions,
				uiChannelInstanceIndexes,
				uiComponentPositions,
				uiComponentClasses,
				uiComponentControlTypes,
				uiComponentNames );
	}

	private String[] getUiComponentNamesForAui( final MixerMadInstanceConfiguration instanceConfiguration,
			final int numInputLanes,
			final int numChannelsPerLane,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		final List<String> retVal = new ArrayList<String>();

		retVal.add( "MasterMixer" );

		for( int i = 0 ; i < numInputLanes ; i++ )
		{
			retVal.add( "LaneMixer" + i );
		}

		return retVal.toArray( new String[ retVal.size() ] );
	}

	private ControlType[] getUiComponentControlTypes( final MixerMadInstanceConfiguration instanceConfiguration,
			final int numInputLanes,
			final int numChannelsPerLane,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		final List<ControlType> retVal = new ArrayList<ControlType>();

		retVal.add( ControlType.CUSTOM );

		for( int i = 0 ; i < numInputLanes ; i++ )
		{
			retVal.add( ControlType.CUSTOM );
		}

		return retVal.toArray( new ControlType[ retVal.size() ] );
	}

	private Class<?>[] getUiComponentClassesForAui( final MixerMadInstanceConfiguration instanceConfiguration,
			final int numInputLanes,
			final int numChannelsPerLane,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		final List<Class<?>> retVal = new ArrayList<Class<?>>();

		retVal.add( ChannelMasterMixerPanelUiInstance.class );

		for( int i = 0 ; i < numInputLanes ; i++ )
		{
			retVal.add( ChannelLaneMixerPanelUiInstance.class );
		}

		return retVal.toArray( new Class<?>[ retVal.size() ] );
	}

	private Rectangle[] getUiComponentPositionsForAui( final MixerMadInstanceConfiguration instanceConfiguration,
			final int numInputLanes,
			final int numChannelsPerLane,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		final List<Rectangle> retVal = new ArrayList<Rectangle>();

		final int masterStartX = INPUT_LANES_START.x + ((LANE_TO_LANE_INCREMENT) * (numInputLanes-1) ) +
				(INPUT_TO_OUTPUT_CHANNEL_INCREMENT + CHANNEL_TO_CHANNEL_INCREMENT + 10);
		final int masterWidth = LANE_TO_LANE_INCREMENT - 4;
		final int masterStartY = 20;
		final int masterHeight = INPUT_LANES_START.y;

		retVal.add( new Rectangle( masterStartX, masterStartY, masterWidth, masterHeight ) );

		final int laneStartX = 8;
		final int laneStartY = 30;

		for( int i = 0 ; i < numInputLanes ; i++ )
		{
			final Rectangle r = new Rectangle( laneStartX + (LANE_TO_LANE_INCREMENT * i ),
					laneStartY,
					LANE_TO_LANE_INCREMENT - 4,
					INPUT_LANES_START.y -10 );
			retVal.add( r );
		}

		return retVal.toArray( new Rectangle[ retVal.size() ] );
	}

	@Override
	public Span getCellSpan()
	{
		return DEFAULT_SPAN;
	}
}
