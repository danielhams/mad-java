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

package uk.co.modularaudio.mads.base.notemultiplexer.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadInstance;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadInstanceConfiguration;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class NoteMultiplexerMadUiDefinition extends
	AbstractConfigurableMadUiDefinition<NoteMultiplexerMadDefinition, NoteMultiplexerMadInstance, NoteMultiplexerMadUiInstance>
{
	private static final Class<NoteMultiplexerMadUiInstance> INSTANCE_CLASS = NoteMultiplexerMadUiInstance.class;

	public static final Point INPUT_CHANNELS_START = new Point( 40, 40 );
	public static final int CHANNEL_TO_CHANNEL_INCREMENT = 20;

	public static final int INPUT_TO_OUTPUT_CHANNEL_INCREMENT = 40;

	private final static Span DEFAULT_SPAN = new Span(1,1);

	public NoteMultiplexerMadUiDefinition(
			final BufferedImageAllocator bia,
			final NoteMultiplexerMadDefinition definition,
			final ComponentImageFactory cif, final String imageRoot )
			throws DatastoreException
	{
		super( bia, cif, imageRoot, definition.getId(), definition, INSTANCE_CLASS );
	}

	private Point[] getUiChannelPositionsForAui( final int numInputChannels, final int numOutputChannels, final int numTotalChannels )
	{
		Point[] retVal;

		int curChannelIndex = 0;
		retVal = new Point[numTotalChannels];
		for (int ic = 0; ic < numInputChannels; ic++)
		{
			retVal[curChannelIndex++] = new Point( INPUT_CHANNELS_START.x
					+ (ic * CHANNEL_TO_CHANNEL_INCREMENT),
					INPUT_CHANNELS_START.y );
		}
		final Point lastInputChannelPoint = retVal[curChannelIndex - 1];
		for (int ot = 0; ot < numOutputChannels; ot++)
		{
			retVal[curChannelIndex++] = new Point( lastInputChannelPoint.x + INPUT_TO_OUTPUT_CHANNEL_INCREMENT,
					INPUT_CHANNELS_START.y + (ot * CHANNEL_TO_CHANNEL_INCREMENT) );
		}
		return retVal;
	}

	private int[] getUiChannelInstanceIndexesForAui( final int numInputChannels, final int numOutputChannels, final int numTotalChannels )
	{
		int[] retVal;

		int curChannelIndex = 0;
		retVal = new int[numTotalChannels];
		for (int ic = 0; ic < numInputChannels; ic++)
		{
			retVal[curChannelIndex++] = 0;
		}
		for (int ot = 0; ot < numOutputChannels; ot++)
		{
			retVal[curChannelIndex++] = 1 + ot;
		}
		return retVal;
	}

	@Override
	protected MadUiInstanceConfiguration getUiInstanceConfiguration( final NoteMultiplexerMadInstance instance )
	{
		final NoteMultiplexerMadInstanceConfiguration instanceConfiguration = instance.getInstanceConfiguration();

		final int numInputChannels = 1;
		final int numOutputChannels = instanceConfiguration.getNumOutputChannels();
		final int numTotalChannels = instanceConfiguration.getNumTotalChannels();

		final int[] uiChannelInstanceIndexes = getUiChannelInstanceIndexesForAui( numInputChannels, numOutputChannels, numTotalChannels );

		final Point[] uiChannelPositions = getUiChannelPositionsForAui( numInputChannels, numOutputChannels, numTotalChannels );

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
		return DEFAULT_SPAN;
	}
}
