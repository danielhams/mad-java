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

package uk.co.modularaudio.mads.internal.audiosystemtester.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadDefinition;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadInstance;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadInstanceConfiguration;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class AudioSystemTesterMadUiDefinition
	extends AbstractConfigurableMadUiDefinition<AudioSystemTesterMadDefinition, AudioSystemTesterMadInstance, AudioSystemTesterMadUiInstance>
{
	private static final Class<AudioSystemTesterMadUiInstance> INSTANCE_CLASS = AudioSystemTesterMadUiInstance.class;

	public static final Point OUTPUT_CHANNELS_START = new Point( 40, 40 );
	public static final int CHANNEL_TO_CHANNEL_INCREMENT = 20;

	private static final Span SPAN = new Span(1,1);

	public AudioSystemTesterMadUiDefinition( final BufferedImageAllocator bia,
			final AudioSystemTesterMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia, cif, imageRoot, MadUIStandardBackgrounds.STD_2X1_LIGHTGRAY, definition, INSTANCE_CLASS );
	}

	private Point[] getUiChannelPositionsForAui( final AudioSystemTesterMadInstanceConfiguration instanceConfiguration,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		int curChannelIndex = 0;
		final Point[] retVal = new Point[ numTotalChannels ];
		for( int ic = 0 ; ic < numOutputChannels ; ic++ )
		{
			retVal[ curChannelIndex++ ] = new Point( OUTPUT_CHANNELS_START.x + (ic * CHANNEL_TO_CHANNEL_INCREMENT), OUTPUT_CHANNELS_START.y );
		}

		return retVal;
	}

	private int[] getUiChannelInstanceIndexesForAui( final AudioSystemTesterMadInstanceConfiguration instanceConfiguration,
			final int numOutputChannels,
			final int numTotalChannels )
	{
		int curChannelIndex = 0;
		final int[] retVal = new int[ numTotalChannels ];
		for( int ic = 0 ; ic < numOutputChannels ; ic++ )
		{
			retVal[ curChannelIndex++ ] = instanceConfiguration.getIndexForOutputChannel( ic );
		}

		return retVal;
	}

	@Override
	protected MadUiInstanceConfiguration getUiInstanceConfiguration( final AudioSystemTesterMadInstance instance )
	{
		final AudioSystemTesterMadInstanceConfiguration instanceConfiguration = instance.getInstanceConfiguration();

		final int numOutputChannels = instanceConfiguration.getNumOutputChannels();
		final int numTotalChannels = instanceConfiguration.getNumTotalChannels();

		final int[] uiChannelInstanceIndexes = getUiChannelInstanceIndexesForAui( instanceConfiguration,
				numOutputChannels,
				numTotalChannels );

		final Point[] uiChannelPositions = getUiChannelPositionsForAui( instanceConfiguration,
				numOutputChannels,
				numTotalChannels );

		return new MadUiInstanceConfiguration( uiChannelPositions,
				uiChannelInstanceIndexes,
				new Rectangle[0],
				new Class[0],
				new ControlType[0],
				new String[0] );
	}

	@Override
	public Span getCellSpan()
	{
		return SPAN;
	}
}
