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

package uk.co.modularaudio.mads.subrack.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
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

public class SubRackMadUiDefinition extends MadUiDefinition<SubRackMadDefinition, SubRackMadInstance>
{
//	private static Log log = LogFactory.getLog( SubRackMadUiDefinition.class.getName());
	private static final Span SPAN = new Span(2,1);

	private static final int PLUG_START_X = 130;
	private static final int PLUG_START_Y = 20;
	private static final int PLUG_DIRECTION_X_INCR = 250;
	private static final int PLUG_TYPE_X_INCR = 0;
	private static final int PLUG_TYPE_Y_INCR = 20;

	private static final Point CONSUMER_AUDIO_CHANNEL_START = new Point( PLUG_START_X, PLUG_START_Y );
	private static final Point PRODUCER_AUDIO_CHANNEL_START = new Point( CONSUMER_AUDIO_CHANNEL_START.x + PLUG_DIRECTION_X_INCR,
			CONSUMER_AUDIO_CHANNEL_START.y);

	private static final int CHANNEL_SPACING_X = 20;

	private static final Point CONSUMER_CV_CHANNEL_START = new Point( CONSUMER_AUDIO_CHANNEL_START.x + PLUG_TYPE_X_INCR,
			CONSUMER_AUDIO_CHANNEL_START.y + PLUG_TYPE_Y_INCR );
	private static final Point PRODUCER_CV_CHANNEL_START = new Point( CONSUMER_CV_CHANNEL_START.x + PLUG_DIRECTION_X_INCR,
			CONSUMER_CV_CHANNEL_START.y );

	private static final Point CONSUMER_NOTE_CHANNEL_START = new Point( CONSUMER_CV_CHANNEL_START.x + PLUG_TYPE_X_INCR,
			CONSUMER_CV_CHANNEL_START.y + PLUG_TYPE_Y_INCR );
	private static final Point PRODUCER_NOTE_CHANNEL_START = new Point( CONSUMER_NOTE_CHANNEL_START.x + PLUG_DIRECTION_X_INCR,
			CONSUMER_NOTE_CHANNEL_START.y );

	private static final Rectangle PATCH_NAME_BOUNDS = new Rectangle( 50, 25, 325, 30 );
	private static final Rectangle CHOOSE_PATCH_BOUNDS = new Rectangle( 375, 25, 40, 30 );
	private static final Rectangle EDIT_PATCH_BOUNDS = new Rectangle( 425, 25, 60, 30 );
	private static final Rectangle SAVE_PATCH_BOUNDS = new Rectangle( 490, 25, 60, 30 );

	public SubRackMadUiDefinition( final BufferedImageAllocator bia,
			final SubRackMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		// Sub rack is draggable
		super( bia, cif, imageRoot, MadUIStandardBackgrounds.STD_2X1_DARKGRAY, definition, true, false );
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AbstractMadUiInstance<?,?> createNewUiInstance( final SubRackMadInstance madInstance )
		throws DatastoreException
	{
		AbstractMadUiInstance<SubRackMadDefinition,SubRackMadInstance> retVal = null;
		try
		{
			// Setup where the channels live
			final ArrayList<MadUiChannelInstance> uiChannelInstances = new ArrayList<MadUiChannelInstance>();
			final MadChannelInstance[] channelInstances = madInstance.getChannelInstances();

			for( int c = 0 ; c < channelInstances.length ; c++ )
			{
				final MadChannelInstance auci = channelInstances[ c ];
				uiChannelInstances.add( new MadUiChannelInstance( computeCenterForChannel( c, auci), auci ) );
			}

			retVal = new SubRackMadUiInstance( madInstance, this );

			// And setup the controls
			final ArrayList<MadUiControlDefinition<?,?,?>> controlDefinitions = new ArrayList<MadUiControlDefinition<?,?,?>>();
			final SubRackShowPatchNameUiControlDefinition patchNameControlDef = new SubRackShowPatchNameUiControlDefinition( 0, PATCH_NAME_BOUNDS );
			controlDefinitions.add( patchNameControlDef );
			final SubRackChoosePatchButtonUiControlDefinition choosePatchControlDef = new SubRackChoosePatchButtonUiControlDefinition( 1, CHOOSE_PATCH_BOUNDS );
			controlDefinitions.add( choosePatchControlDef );
			final SubRackEditPatchButtonUiControlDefinition editPatchControlDef = new SubRackEditPatchButtonUiControlDefinition( 2, EDIT_PATCH_BOUNDS );
			controlDefinitions.add( editPatchControlDef );
			final SubRackSavePatchButtonUiControlDefinition savePatchControlDef = new SubRackSavePatchButtonUiControlDefinition( 3, SAVE_PATCH_BOUNDS );
			controlDefinitions.add( savePatchControlDef );

			final ArrayList<AbstractMadUiControlInstance<?,?,?>> uiControlInstances = new ArrayList<AbstractMadUiControlInstance<?,?,?>>();
			final ArrayList<AbstractMadUiControlInstance<?,?,?>> uiDisplayProcessingControlInstances = new ArrayList<AbstractMadUiControlInstance<?,?,?>>();
			for( final MadUiControlDefinition cd : controlDefinitions )
			{
				final AbstractMadUiControlInstance<?,?,?> uici = cd.createInstance( madInstance, retVal );
				uiControlInstances.add( uici );
				if( uici.needsDisplayProcessing() )
				{
					uiDisplayProcessingControlInstances.add( uici );
				}
			}

			retVal.setUiControlsAndChannels( uiControlInstances.toArray( new AbstractMadUiControlInstance<?,?,?>[ uiControlInstances.size() ] ),
					uiDisplayProcessingControlInstances.toArray( new AbstractMadUiControlInstance<?,?,?>[ uiDisplayProcessingControlInstances.size() ] ),
					uiChannelInstances.toArray( new MadUiChannelInstance[ uiChannelInstances.size() ] ) );
		}
		catch(final Exception e)
		{
			final String msg = "Exception caught creating new ui instance: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		return retVal;
	}

	private Point computeCenterForChannel( final int c, final MadChannelInstance auci )
	{
		int x = 0;
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
						chanNum = chanNum - SubRackMadDefinition.PRODUCER_AUDIO_OUT_1;
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
						chanNum = chanNum - SubRackMadDefinition.CONSUMER_CV_IN_1;
						break;
					}
					case PRODUCER:
					{
						xOffset = PRODUCER_CV_CHANNEL_START.x;
						yOffset = PRODUCER_CV_CHANNEL_START.y;
						chanNum = chanNum - SubRackMadDefinition.PRODUCER_CV_OUT_1;
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
						chanNum = chanNum - SubRackMadDefinition.CONSUMER_NOTE_IN_1;
						break;
					}
					case PRODUCER:
					{
						xOffset = PRODUCER_NOTE_CHANNEL_START.x;
						yOffset = PRODUCER_NOTE_CHANNEL_START.y;
						chanNum = chanNum - SubRackMadDefinition.PRODUCER_NOTE_OUT_1;
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
