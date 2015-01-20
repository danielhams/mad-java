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
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;
import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
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
	private static final Span span = new Span(4,1);
	
	private static final int CHANNEL_START_X = 40;
	private static final int CHANNEL_START_Y = 40;
	
//	private static final int CONSUMER_TO_PRODUCER_X_INCR = 110;
	private static final int CONSUMER_TO_PRODUCER_X_INCR = 180;

	private static final int CHANNEL_SPACING_X = 20;
	
//	private static final int NEW_TYPE_X_INCR = 330;
	private static final int NEW_TYPE_X_INCR = 360;
	
	private static final Point CONSUMER_AUDIO_CHANNEL_START = new Point( 
			CHANNEL_START_X, CHANNEL_START_Y );
	private static final Point PRODUCER_AUDIO_CHANNEL_START = new Point( 
			CONSUMER_AUDIO_CHANNEL_START.x + CONSUMER_TO_PRODUCER_X_INCR, CHANNEL_START_Y );
	
	private static final Point CONSUMER_CV_CHANNEL_START = new Point( 
			CONSUMER_AUDIO_CHANNEL_START.x + NEW_TYPE_X_INCR, CHANNEL_START_Y );
	private static final Point PRODUCER_CV_CHANNEL_START = new Point( 
			CONSUMER_CV_CHANNEL_START.x + CONSUMER_TO_PRODUCER_X_INCR, CHANNEL_START_Y );
	
	private static final Point CONSUMER_NOTE_CHANNEL_START = new Point( 
			CONSUMER_CV_CHANNEL_START.x + NEW_TYPE_X_INCR, CHANNEL_START_Y );
	private static final Point PRODUCER_NOTE_CHANNEL_START = new Point(
			CONSUMER_NOTE_CHANNEL_START.x + CONSUMER_TO_PRODUCER_X_INCR, CHANNEL_START_Y );
	
	private BufferedImage frontBufferedImage = null;
	private BufferedImage backBufferedImage = null;
	
	public RackMasterIOMadUiDefinition( BufferedImageAllocator bia, RackMasterIOMadDefinition definition,
			ComponentImageFactory cif, 
			String imageRoot ) throws DatastoreException
	{
		// master io is not draggable.
		super( bia, definition, false, false );
		
		frontBufferedImage = cif.getBufferedImage( imageRoot,
				definition.getId() + "_front.png" );
		
		backBufferedImage = cif.getBufferedImage( imageRoot,
				definition.getId() + "_back.png");
	}

	public BufferedImage getFrontBufferedImage()
	{
		return frontBufferedImage;
	}

	public BufferedImage getBackBufferedImage()
	{
		return backBufferedImage;
	}

	@Override
	public MadUiInstance<?, ?> createNewUiInstance( RackMasterIOMadInstance instance )
		throws DatastoreException
	{
		MadUiInstance<?,?> retVal = null;
		try
		{
			// Setup where the channels live
			ArrayList<MadUiChannelInstance> uiChannelInstances = new ArrayList<MadUiChannelInstance>();
			MadChannelInstance[] channelInstances = instance.getChannelInstances();
			
			for( int c = 0 ; c < channelInstances.length ; c++ )
			{
				MadChannelInstance auci = channelInstances[ c ];
				uiChannelInstances.add( new MadUiChannelInstance( computeCenterForChannel( c, auci), auci ) );
			}
			
			// We don't have any controls...
			
			retVal = new RackMasterIOMadUiInstance( instance,
					this );
			
			retVal.setUiControlsAndChannels( new MadUiControlInstance<?,?,?>[ 0 ],
					new MadUiControlInstance<?,?,?>[ 0 ],
					uiChannelInstances.toArray( new MadUiChannelInstance[ uiChannelInstances.size() ] ) );
		}
		catch(Exception e)
		{
			String msg = "Exception caught creating new ui instance: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		return retVal;
	}

	private Point computeCenterForChannel( int c, MadChannelInstance auci )
	{
		int x = 0;
		int y = 0;
		MadChannelDefinition aucd = auci.definition;
		MadChannelDirection audirection = aucd.direction;
		MadChannelType autype = aucd.type;
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
						chanNum = chanNum - RackMasterIOMadDefinition.PRODUCER_AUDIO_OUT_1;
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
						chanNum = chanNum - RackMasterIOMadDefinition.CONSUMER_CV_IN_1;
						break;
					}
					case PRODUCER:
					{
						xOffset = PRODUCER_CV_CHANNEL_START.x;
						yOffset = PRODUCER_CV_CHANNEL_START.y;
						chanNum = chanNum - RackMasterIOMadDefinition.PRODUCER_CV_OUT_1;
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
						chanNum = chanNum - RackMasterIOMadDefinition.CONSUMER_NOTE_IN_1;
						break;
					}
					case PRODUCER:
					{
						xOffset = PRODUCER_NOTE_CHANNEL_START.x;
						yOffset = PRODUCER_NOTE_CHANNEL_START.y;
						chanNum = chanNum - RackMasterIOMadDefinition.PRODUCER_NOTE_OUT_1;
						break;
					}
				}
				break;
			}
		}
		x = chanNum * CHANNEL_SPACING_X;

		Point retVal = new Point( xOffset + x, yOffset + y );
//		log.debug("For channel " + c + " direction " + audirection + " computed " + retVal.toString() );
		return retVal;
	}
	
	public Span getCellSpan()
	{
		return span;
	}
}
