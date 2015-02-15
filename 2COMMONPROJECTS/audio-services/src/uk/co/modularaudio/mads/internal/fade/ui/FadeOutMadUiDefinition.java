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

package uk.co.modularaudio.mads.internal.fade.ui;

import java.awt.Point;
import java.util.ArrayList;

import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class FadeOutMadUiDefinition extends MadUiDefinition<FadeOutMadDefinition, FadeOutMadInstance>
{
	private static final Span SPAN = new Span(2,1);

	private static final Point CONSUMER_CHANNEL_CENTER = new Point( 120, 30 );

	private static final Point PRODUCER_CHANNEL_CENTER = new Point( 160, 30 );

	public FadeOutMadUiDefinition( final BufferedImageAllocator bia,
			final FadeOutMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia, cif, imageRoot, MadUIStandardBackgrounds.STD_2X1_LIGHTGRAY, definition );
	}

	@Override
	public AbstractMadUiInstance<?,?> createNewUiInstance( final FadeOutMadInstance instance )
		throws DatastoreException
	{
		try
		{
			// Setup where the channels live
			final ArrayList<MadUiChannelInstance> uiChannelInstances = new ArrayList<MadUiChannelInstance>();
			final MadChannelInstance[] channelInstances = instance.getChannelInstances();
			final MadChannelInstance consumerChannelInstance = channelInstances[ FadeOutMadDefinition.CONSUMER ];
			uiChannelInstances.add(  new MadUiChannelInstance( CONSUMER_CHANNEL_CENTER, consumerChannelInstance ) );
			final MadChannelInstance producerChannelInstance = channelInstances[ FadeOutMadDefinition.PRODUCER ];
			uiChannelInstances.add( new MadUiChannelInstance( PRODUCER_CHANNEL_CENTER, producerChannelInstance ) );

			// We don't have any controls...

			final AbstractMadUiInstance<?,?> retVal = new FadeOutMadUiInstance( instance,
					this );

			retVal.setUiControlsAndChannels( new AbstractMadUiControlInstance<?,?,?>[ 0 ],
					 new AbstractMadUiControlInstance<?,?,?>[ 0 ],
					 uiChannelInstances.toArray( new MadUiChannelInstance[ uiChannelInstances.size() ] ) );
			return retVal;
		}
		catch(final Exception e)
		{
			final String msg = "Exception caught creating new ui instance: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public Span getCellSpan()
	{
		return SPAN;
	}
}
