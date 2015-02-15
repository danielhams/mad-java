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

package uk.co.modularaudio.mads.rackmasterio.ui;

import java.awt.Point;
import java.util.ArrayList;

import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;
import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class RackMasterIOMadUiDefinition extends MadUiDefinition<RackMasterIOMadDefinition, RackMasterIOMadInstance>
{
//	private static Log log = LogFactory.getLog( RackMasterIOMadUiDefinition.class.getName());
	private static final Span SPAN = new Span(4,1);

	private static final int CHANNEL_START_X = 40;
	private static final int CHANNEL_START_Y = 40;

	private static final int CONSUMER_TO_PRODUCER_X_INCR = 180;

	private static final int CHANNEL_SPACING_X = 20;

	private static final int NEW_TYPE_X_INCR = 360;

	private static final Point CONSUMER_AUDIO_CHANNEL_START =
			new Point( CHANNEL_START_X, CHANNEL_START_Y );
	private static final Point PRODUCER_AUDIO_CHANNEL_START =
			new Point( CONSUMER_AUDIO_CHANNEL_START.x + CONSUMER_TO_PRODUCER_X_INCR, CHANNEL_START_Y );

	private static final Point CONSUMER_CV_CHANNEL_START =
			new Point( CONSUMER_AUDIO_CHANNEL_START.x + NEW_TYPE_X_INCR, CHANNEL_START_Y );
	private static final Point PRODUCER_CV_CHANNEL_START =
			new Point( CONSUMER_CV_CHANNEL_START.x + CONSUMER_TO_PRODUCER_X_INCR, CHANNEL_START_Y );

	private static final Point CONSUMER_NOTE_CHANNEL_START =
			new Point( CONSUMER_CV_CHANNEL_START.x + NEW_TYPE_X_INCR, CHANNEL_START_Y );
	private static final Point PRODUCER_NOTE_CHANNEL_START =
			new Point( CONSUMER_NOTE_CHANNEL_START.x + CONSUMER_TO_PRODUCER_X_INCR, CHANNEL_START_Y );

	public RackMasterIOMadUiDefinition( final BufferedImageAllocator bia,
			final RackMasterIOMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		// master io is not draggable.
		super( bia, cif, imageRoot, MadUIStandardBackgrounds.STD_4X1_DARKGRAY, definition, false, false );
	}

	@Override
	public AbstractMadUiInstance<?, ?> createNewUiInstance( final RackMasterIOMadInstance instance )
		throws DatastoreException
	{
		// Setup where the channels live
		final ArrayList<MadUiChannelInstance> uiChannelInstances = new ArrayList<MadUiChannelInstance>();
		final MadChannelInstance[] channelInstances = instance.getChannelInstances();

		for( int c = 0 ; c < channelInstances.length ; c++ )
		{
			final MadChannelInstance auci = channelInstances[ c ];
			uiChannelInstances.add( new MadUiChannelInstance( computeCenterForChannel( c, auci), auci ) );
		}

		// We don't have any controls...

		final AbstractMadUiInstance<?,?> retVal = new RackMasterIOMadUiInstance( instance,
				this );

		retVal.setUiControlsAndChannels( new AbstractMadUiControlInstance<?,?,?>[ 0 ],
				new AbstractMadUiControlInstance<?,?,?>[ 0 ],
				uiChannelInstances.toArray( new MadUiChannelInstance[ uiChannelInstances.size() ] ) );
		return retVal;
	}

	private Point computeCenterForChannel( final int c, final MadChannelInstance auci )
	{
		int x;
		final int y = 0;
		final MadChannelDefinition aucd = auci.definition;
		final MadChannelDirection audirection = aucd.direction;
		final MadChannelType autype = aucd.type;
		int xOffset = -1;
		int yOffset = -1;
		int chanNum = c;
		switch( autype )
		{
			case AUDIO:
			{
				switch( audirection )
				{
					case CONSUMER:
					{
						xOffset = CONSUMER_AUDIO_CHANNEL_START.x;
						yOffset = CONSUMER_AUDIO_CHANNEL_START.y;
						break;
					}
					case PRODUCER:
					{
						xOffset = PRODUCER_AUDIO_CHANNEL_START.x;
						yOffset = PRODUCER_AUDIO_CHANNEL_START.y;

						chanNum = chanNum - RackMasterIOMadDefinition.ChanIndexes.PRODUCER_AUDIO_OUT_1.ordinal();
						break;
					}
				}
				break;
			}
			case CV:
			{
				switch( audirection )
				{
					case CONSUMER:
					{
						xOffset = CONSUMER_CV_CHANNEL_START.x;
						yOffset = CONSUMER_CV_CHANNEL_START.y;
						chanNum = chanNum - RackMasterIOMadDefinition.ChanIndexes.CONSUMER_CV_IN_1.ordinal();
						break;
					}
					case PRODUCER:
					{
						xOffset = PRODUCER_CV_CHANNEL_START.x;
						yOffset = PRODUCER_CV_CHANNEL_START.y;
						chanNum = chanNum - RackMasterIOMadDefinition.ChanIndexes.PRODUCER_CV_OUT_1.ordinal();
						break;
					}
				}
				break;
			}
			case NOTE:
			{
				switch( audirection )
				{
					case CONSUMER:
					{
						xOffset = CONSUMER_NOTE_CHANNEL_START.x;
						yOffset = CONSUMER_NOTE_CHANNEL_START.y;
						chanNum = chanNum - RackMasterIOMadDefinition.ChanIndexes.CONSUMER_NOTE_IN_1.ordinal();
						break;
					}
					case PRODUCER:
					{
						xOffset = PRODUCER_NOTE_CHANNEL_START.x;
						yOffset = PRODUCER_NOTE_CHANNEL_START.y;
						chanNum = chanNum - RackMasterIOMadDefinition.ChanIndexes.PRODUCER_NOTE_OUT_1.ordinal();
						break;
					}
				}
				break;
			}
		}
		x = chanNum * CHANNEL_SPACING_X;

		final Point retVal = new Point( xOffset + x, yOffset + y );
//		log.debug("For channel " + c + " direction " + audirection + " computed " + retVal.toString() );
		return retVal;
	}

	@Override
	public Span getCellSpan()
	{
		return SPAN;
	}
}
