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

package uk.co.modularaudio.mads.internal.blockingwritering.ui;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadDefinition;
import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class BlockingWriteRingMadUiDefinition extends MadUiDefinition<BlockingWriteRingMadDefinition, BlockingWriteRingMadInstance>
{
	private static final Span span = new Span(2,1);

	private static final Point PRODUCER_LEFT_CHANNEL_CENTER = new Point( 160, 30 );
	private static final Point PRODUCER_RIGHT_CHANNEL_CENTER = new Point( 180, 30 );
	
	private BufferedImage frontBufferedImage = null;
	private BufferedImage backBufferedImage = null;
	
	public BlockingWriteRingMadUiDefinition( BufferedImageAllocator bia, BlockingWriteRingMadDefinition definition,
			ComponentImageFactory cif, 
			String imageRoot ) throws DatastoreException
	{
		super( bia, definition );
		
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
	public MadUiInstance<?,?> createNewUiInstance( BlockingWriteRingMadInstance instance )
		throws DatastoreException
	{
		MadUiInstance<?,?> retVal = null;
		try
		{
			// Setup where the channels live
			ArrayList<MadUiChannelInstance> uiChannelInstances = new ArrayList<MadUiChannelInstance>();
			MadChannelInstance[] channelInstances = instance.getChannelInstances();

			MadChannelInstance leftProducerChannelInstance = channelInstances[ BlockingWriteRingMadDefinition.PRODUCER_LEFT ];
			uiChannelInstances.add( new MadUiChannelInstance( PRODUCER_LEFT_CHANNEL_CENTER, leftProducerChannelInstance ) );
			
			MadChannelInstance rightProducerChannelInstance = channelInstances[ BlockingWriteRingMadDefinition.PRODUCER_RIGHT ];
			uiChannelInstances.add( new MadUiChannelInstance( PRODUCER_RIGHT_CHANNEL_CENTER, rightProducerChannelInstance ) );
			
			// We don't have any controls...
			
			retVal = new BlockingWriteRingMadUiInstance( instance,
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
	
	public Span getCellSpan()
	{
		return span;
	}
}
