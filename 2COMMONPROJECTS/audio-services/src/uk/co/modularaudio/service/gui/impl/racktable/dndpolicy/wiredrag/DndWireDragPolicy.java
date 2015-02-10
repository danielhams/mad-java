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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiRackBackActionListener;
import uk.co.modularaudio.service.gui.impl.racktable.RackTable;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableDndPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.back.RackWirePositionHelper;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.RegionHintType;
import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.scrollpane.AutoScrollingMouseListener;
import uk.co.modularaudio.util.table.TablePosition;

public class DndWireDragPolicy implements RackTableDndPolicy
{
	private static Log log = LogFactory.getLog( DndWireDragPolicy.class.getName() );

	private RackDataModel dataModel;
	private final GuiRackBackActionListener actionListener;

	// SCROLL
	private final AutoScrollingMouseListener scrollingMouseListener = new AutoScrollingMouseListener();

	private final DndWireDragStaticRegionDecorationHint regionHintDecorator;
	private final DndWireDragCurrentWireHint currentWireDecorationHint;
	private final DndWireDragPlugNameTooltipHint plugNameTooltipHint;

	// State concerning currently being dragged
//	private Rectangle dragSourceRenderedRectangle;

	// Used to highlight the possible source of a drag
	private GuiChannelPlug regionHintSourceGuiChannelPlug;

	private Rectangle currentPlugHintRectangle = new Rectangle( 0, 0, 0, 0 );

	private RackComponent dragStartRackComponent;
	private GuiChannelPlug dragStartChannelPlug;
	private Point dragStartPosition;
	private MadChannelInstance dragStartChannelIsSink;
	private MadChannelInstance dragStartChannelIsSource;

	private RackComponent dragEndRackComponent;
	private GuiChannelPlug dragEndChannelPlug;
	@SuppressWarnings("unused")
	private Point targetPosition;
	private MadChannelInstance dragEndChannelIsSink;
	private MadChannelInstance dragEndChannelIsSource;

	public DndWireDragPolicy( final RackService rackService,
			final RackDataModel dataModel,
			final DndWireDragDecorations decorations,
			final GuiRackBackActionListener actionListener )
	{
//		this.rackService = rackService;
		this.dataModel = dataModel;
		this.actionListener = actionListener;
		currentWireDecorationHint = decorations.getCurrentWireDecorationHint();
		regionHintDecorator = decorations.getSourcePlugDecorationHint();
		plugNameTooltipHint = decorations.getPlugNameTooltipHint();
	}

	@Override
	public boolean isMouseOverDndSource( final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component,
			final Point localPoint,
			final Point tablePoint )
	{
		boolean changed = false;
		boolean isPlug = false;
		boolean canDrag = false;

		if( component != null )
		{
			// Find if the mouse is over a component. If it is, and the mouse is over one of the
			// locations of the "plugs", then it is over a DnD source
			final GuiChannelPlug channelPlug = component.getPlugFromPosition( localPoint );

			if( channelPlug != null )
			{
				isPlug = true;
				plugNameTooltipHint.setPlugName( channelPlug.getUiChannelInstance().getChannelInstance().definition.name );
				final Rectangle newDragSourcerenderedRectangle = component.getRenderedRectangle();
				if( newDragSourcerenderedRectangle == null )
				{
					log.error( "Oops" );
				}
				final RackComponent tableComponent = table.getTableModelComponentFromGui( component );
				final TablePosition tp = dataModel.getContentsOriginReturnNull( tableComponent );
				final Dimension gridSize = table.getGridSize();
				final Rectangle plugBounds = channelPlug.getBounds();
				final int hintXOffset = tp.x * gridSize.width + newDragSourcerenderedRectangle.x + plugBounds.x - 2;
				final int hintYOffset = tp.y * gridSize.height + newDragSourcerenderedRectangle.y + plugBounds.y - 2;
				final int hintWidth = plugBounds.width + 3;
				final int hintHeight = plugBounds.height + 3;
				final Rectangle hintRectangle = new Rectangle( hintXOffset, hintYOffset, hintWidth, hintHeight );

				if( channelPlug != regionHintSourceGuiChannelPlug || !currentPlugHintRectangle .equals( hintRectangle ) )
				{
					regionHintSourceGuiChannelPlug = channelPlug;
					// It's a different plug from the previous one - reset the decoration hint with the appropriate data
//					dragSourceRenderedRectangle = newDragSourcerenderedRectangle;

					final Rectangle rr = new Rectangle( hintWidth, hintHeight );
					regionHintDecorator.setRegionHint( RegionHintType.SOURCE, hintRectangle, rr );
					currentPlugHintRectangle = hintRectangle;
					changed = true;
				}
				else
				{
					// Is the same as we have previously hinted. don't update the decoration
				}
				regionHintDecorator.setActive( true );
				canDrag = true;
			}
			else if( regionHintSourceGuiChannelPlug != null )
			{
				// Changed from active to inactive
				regionHintSourceGuiChannelPlug = null;
				changed = true;
			}
		}

		if( changed )
		{
			if( !canDrag )
			{
//				log.debug("Setting region hint to false");
				regionHintDecorator.setActive( false );
			}

			plugNameTooltipHint.setActive( isPlug );
		}

		return canDrag;
	}

	@Override
	public void startDrag(final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component, final Point dragLocalPoint, final Point dragStartPoint)
		throws RecordNotFoundException, DatastoreException
	{
//		log.debug("Drag beginning.");
		regionHintDecorator.setActive( false );
		// Store the channel from which a drag has begun - we need to be able to show
		// that a channel can or cannot be the destination based on matching source/sink pairs of the same
		// type ( audio / CV / note )
		if( component != null )
		{
			// Find if the mouse is over a component. If it is, and the mouse is over one of the
			// locations of the "plugs", then it is over a DnD source
			dragStartChannelPlug = component.getPlugFromPosition( dragLocalPoint );

			// Calculate the start position for the wire we are dragging
			dragStartRackComponent = table.getTableModelComponentFromGui( component );
			AbstractGuiAudioComponent dragStartGuiComponent = table.getGuiComponentFromTableModel( dragStartRackComponent );
			final MadUiChannelInstance dragStartUiChannelInstance = dragStartChannelPlug.getUiChannelInstance();
			final MadInstance<?,?> dragStartRackComponentInstance = dragStartRackComponent.getInstance();
			final MadChannelInstance dragStartChannelInstance = dragStartUiChannelInstance.getChannelInstance();

			final RackLink existingLink = checkForExistingChannelLink( dragStartChannelPlug, dragStartRackComponentInstance );
			if( existingLink != null )
			{
				// Is involved in a link already
				final MadChannelInstance linkProducerChannelInstance = existingLink.getProducerChannelInstance();
				if( linkProducerChannelInstance == dragStartChannelInstance )
				{
					dragStartChannelIsSink = existingLink.getConsumerChannelInstance();
					dragStartRackComponent = existingLink.getConsumerRackComponent();
					dragStartGuiComponent = table.getGuiComponentFromTableModel( dragStartRackComponent );
					dragStartChannelPlug = dragStartGuiComponent.getPlugFromMadChannelInstance( dragStartChannelIsSink );
				}
				else
				{
					dragStartChannelIsSource = existingLink.getProducerChannelInstance();
					dragStartRackComponent = existingLink.getProducerRackComponent();
					dragStartGuiComponent = table.getGuiComponentFromTableModel( dragStartRackComponent );
					dragStartChannelPlug = dragStartGuiComponent.getPlugFromMadChannelInstance( dragStartChannelIsSource );
				}
				dragStartPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
						dataModel,
						dragStartGuiComponent,
						dragStartRackComponent,
						dragStartChannelPlug );
				try
				{
					actionListener.guiRemoveRackLink( existingLink );
				}
				catch( final Exception e )
				{
					final String msg = "Exception caught removing rack link: " + e.toString();
					throw new DatastoreException( msg, e );
				}
			}
			else
			{
				final RackComponent mici = dataModel.getContentsAtPosition( 0, 0 );
				// Check for existing IOLink
				final RackIOLink existingIOLink = checkForExistingChannelIOLink( dragStartChannelPlug, dragStartRackComponentInstance );
				if( existingIOLink != null )
				{
					// Is involved in a link already
					final MadChannelInstance rackChannelInstance = existingIOLink.getRackChannelInstance();
					final boolean ioLinkIsConsumer = (rackChannelInstance.definition.direction == MadChannelDirection.CONSUMER );
					if( rackChannelInstance == dragStartChannelInstance )
					{
						// Drag point is the masterIO
						// So set the drag start as being the other end
						if( ioLinkIsConsumer )
						{
							dragStartChannelIsSink = existingIOLink.getRackComponentChannelInstance();
							dragStartRackComponent = existingIOLink.getRackComponent();
							dragStartGuiComponent = table.getGuiComponentFromTableModel( dragStartRackComponent );
							dragStartChannelPlug = dragStartGuiComponent.getPlugFromMadChannelInstance( dragStartChannelIsSink );
						}
						else
						{
							dragStartChannelIsSource = existingIOLink.getRackComponentChannelInstance();
							dragStartRackComponent = existingIOLink.getRackComponent();
							dragStartGuiComponent = table.getGuiComponentFromTableModel( dragStartRackComponent );
							dragStartChannelPlug = dragStartGuiComponent.getPlugFromMadChannelInstance( dragStartChannelIsSource );
						}
					}
					else
					{
						// Drag point is another component
						// so set the drag start as the master IO
						if( ioLinkIsConsumer )
						{
							dragStartChannelIsSink = existingIOLink.getRackChannelInstance();
							dragStartRackComponent = mici;
							dragStartGuiComponent = table.getGuiComponentFromTableModel( dragStartRackComponent );
							dragStartChannelPlug = dragStartGuiComponent.getPlugFromMadChannelInstance( dragStartChannelIsSink );
						}
						else
						{
							dragStartChannelIsSource = existingIOLink.getRackChannelInstance();
							dragStartRackComponent = mici;
							dragStartGuiComponent = table.getGuiComponentFromTableModel( dragStartRackComponent );
							dragStartChannelPlug = dragStartGuiComponent.getPlugFromMadChannelInstance( dragStartChannelIsSource );
						}
					}
					dragStartPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
							dataModel,
							dragStartGuiComponent,
							dragStartRackComponent,
							dragStartChannelPlug );
					try
					{
						actionListener.guiRemoveRackIOLink( existingIOLink );
					}
					catch(final Exception e)
					{
						final String msg = "Exception caught removing rack IO link: " + e.toString();
						throw new DatastoreException( msg, e );
					}
				}
				else
				{
					// Plug is currently empty, start a new drag from here.
					final MadChannelDirection dragStartChannelDirection = dragStartChannelInstance.definition.direction;
					if( dragStartChannelDirection == MadChannelDirection.CONSUMER )
					{
						dragStartChannelIsSink = dragStartChannelInstance;
						dragStartPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
								dataModel,
								dragStartGuiComponent,
								dragStartRackComponent,
								dragStartChannelPlug );
					}
					else if( dragStartChannelDirection == MadChannelDirection.PRODUCER )
					{
						dragStartChannelIsSource = dragStartChannelInstance;
						dragStartPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
								dataModel,
								dragStartGuiComponent,
								dragStartRackComponent,
								dragStartChannelPlug );
					}
					else
					{
						log.error("Oops");
						dragStartPosition = new Point( 100, 100 );
						// At least it won't null pointer everywhere.
						// Note later added: Hahahaha, how charmingly naive, Dan
						// I still won't fix it, though.
					}
				}
			}

			// Start the mouse relative decoration hint up
			currentWireDecorationHint.setDragStartPosition( dragStartPosition );
			currentWireDecorationHint.setMousePosition( dragStartPoint );
			currentWireDecorationHint.signalAnimation();
			currentWireDecorationHint.setActive( true );
		}

		// SCROLL
		table.addMouseMotionListener( scrollingMouseListener );
	}

	private RackLink checkForExistingChannelLink( final GuiChannelPlug channelPlug, final MadInstance<?,?> componentInstance )
	{
		final MadUiChannelInstance uiChannelInstance = channelPlug.getUiChannelInstance();
		final MadChannelInstance channelInstance = uiChannelInstance.getChannelInstance();
		RackLink foundLink = null;
		for( int i = 0 ; foundLink == null && i < dataModel.getNumLinks() ; i++ )
		{
			final RackLink testLink = dataModel.getLinkAt( i );
			if( testLink.getProducerChannelInstance() == channelInstance ||
					testLink.getConsumerChannelInstance() == channelInstance )
			{
				foundLink = testLink;
			}
		}

		return foundLink;
	}

	@Override
	public boolean isValidDragTarget(
			final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component, final Point dragLocalPoint, final Point dragTablePoint)
	{
//		log.debug("Wire Drag policy checking if valid drag target with local point: " + dragLocalPoint );
		boolean isTarget = false;
		boolean changed = false;

		if( component != null )
		{
			// Find if the mouse is over a component. If it is, and the mouse is over one of the
			// locations of the "plugs", then it is over a DnD target
			final GuiChannelPlug testDragEndChannelPlug = component.getPlugFromPosition( dragLocalPoint );

			if( testDragEndChannelPlug != dragEndChannelPlug )
			{
				changed = true;
			}

			if( testDragEndChannelPlug != null )
			{
				dragEndChannelPlug = testDragEndChannelPlug;
				plugNameTooltipHint.setPlugName( dragEndChannelPlug.getUiChannelInstance().getChannelInstance().definition.name );
				dragEndRackComponent = table.getTableModelComponentFromGui( component );

				// Don't allow local links or links between channels that aren't of the same type
				if( dragEndRackComponent == dragStartRackComponent ||
						testDragEndChannelPlug.getClass() != dragStartChannelPlug.getClass() )
				{
//					log.debug("Failing valid drag target due to same component or different channeltype");
				}
				else
				{
					// Calculate the start position for the wire we are dragging
					final RackComponent masterIO = dataModel.getContentsAtPosition( 0, 0 );

					final boolean targetIsMasterIO = ( dragEndRackComponent == masterIO );
					final boolean sourceIsMasterIO = ( dragStartRackComponent == masterIO );
					final MadUiChannelInstance uiChannelInstance = testDragEndChannelPlug.getUiChannelInstance();
					final MadInstance<?,?> targetRackComponentInstance = dragEndRackComponent.getInstance();
					final MadChannelInstance channelInstance = uiChannelInstance.getChannelInstance();

					if( targetIsMasterIO )
					{
						isTarget = isValidIOLinkTarget( table,
								component,
								targetRackComponentInstance,
								channelInstance );
					}
					else if( sourceIsMasterIO )
					{
						isTarget = isValidIOLinkTarget( table,
								component,
								targetRackComponentInstance,
								channelInstance );
					}
					else
					{
						isTarget = isValidLinkTarget( table,
								component,
								targetRackComponentInstance,
								channelInstance );
					}
				}
			}
			else if( dragEndChannelPlug != null )
			{
				changed = true;
				dragEndChannelPlug = null;
			}
		}

		if( changed )
		{
			plugNameTooltipHint.setActive( isTarget );
		}

		return isTarget;
	}

	private boolean isValidIOLinkTarget( final LayeredPaneDndTable<RackComponent, RackComponentProperties,
			AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component,
			final MadInstance<?,?> targetRackComponentInstance,
			final MadChannelInstance channelInstance )
	{
		boolean retVal = false;

		final MadChannelDirection channelDirection = channelInstance.definition.direction;

		if( channelDirection == MadChannelDirection.CONSUMER )
		{
			if( dragStartChannelIsSink != null )
			{
				dragEndChannelIsSource = channelInstance;
				targetPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
						dataModel,
						component,
						dragEndRackComponent,
						dragEndChannelPlug );
				retVal = true;
			}
			else
			{
				// Can't connect  source to sink
			}
		}
		else
		{
			final RackIOLink existingLink = checkForExistingChannelIOLink( dragEndChannelPlug, targetRackComponentInstance );

			if( existingLink != null )
			{
	//			log.debug("Found existing IO link, not valid IO link target");
				// Is involved in a link already
				retVal = false;
			}
			else
			{
	//			log.debug("No existing IO link, looking for target plug");
				// Plug is currently empty, figure out what values to extract as the target
				if( channelDirection == MadChannelDirection.PRODUCER )
				{
					if( dragStartChannelIsSource != null)
					{
						dragEndChannelIsSink = channelInstance;
						targetPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
								dataModel,
								component,
								dragEndRackComponent,
								dragEndChannelPlug );
						retVal = true;
					}
					else
					{
					}
				}
				else if( channelDirection == MadChannelDirection.CONSUMER )
				{
					if( dragStartChannelIsSink != null )
					{
						dragEndChannelIsSource = channelInstance;
						targetPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
								dataModel,
								component,
								dragEndRackComponent,
								dragEndChannelPlug );
						retVal = true;
					}
					else
					{
					}
				}
				else
				{
					log.error("Oops");
					dragStartPosition = new Point( 100, 100 );
					// At least it won't null pointer everywhere.
				}
			}
		}
		return retVal;
	}

	private RackIOLink checkForExistingChannelIOLink( final GuiChannelPlug channelPlug, final MadInstance<?,?> componentInstance )
	{
		final MadUiChannelInstance uiChannelInstance = channelPlug.getUiChannelInstance();
		final MadChannelInstance channelInstance = uiChannelInstance.getChannelInstance();
		RackIOLink foundLink = null;
		for( int i = 0 ; foundLink == null && i < dataModel.getNumIOLinks() ; i++ )
		{
			final RackIOLink testLink = dataModel.getIOLinkAt( i );
			if( testLink.getRackChannelInstance() == channelInstance || testLink.getRackComponentChannelInstance() == channelInstance )
			{
				foundLink = testLink;
			}
		}

		return foundLink;
	}

	private boolean isValidLinkTarget( final LayeredPaneDndTable<RackComponent, RackComponentProperties,
			AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component,
			final MadInstance<?,?>targetRackComponentInstance,
			final MadChannelInstance channelInstance )
	{
		boolean retVal = false;

		final MadChannelDirection channelDirection = channelInstance.definition.direction;
		if( channelDirection == MadChannelDirection.PRODUCER )
		{
			if( dragStartChannelIsSink != null )
			{
				dragEndChannelIsSource = channelInstance;
				targetPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
						dataModel,
						component,
						dragEndRackComponent,
						dragEndChannelPlug );
				retVal = true;
			}
			else
			{
				// Can't connect sink to sink
			}
		}
		else
		{
			final RackLink existingLink = checkForExistingChannelLink( dragEndChannelPlug, targetRackComponentInstance );
			if( existingLink != null )
			{
				// Is involved in a link already
//				log.debug("Found existing link, not valid link target");
				retVal = false;
			}
			else
			{
//				log.debug("No existing link, looking for target plug");
				// Plug is currently empty, figure out what values to extract as the target
				if( channelDirection == MadChannelDirection.CONSUMER )
				{
					if( dragStartChannelIsSource != null)
					{
						dragEndChannelIsSink = channelInstance;
						targetPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
								dataModel,
								component,
								dragEndRackComponent,
								dragEndChannelPlug );
						retVal = true;
					}
					else
					{
						// Can't connect source to source
					}
				}
				else if( channelDirection == MadChannelDirection.PRODUCER )
				{
					if( dragStartChannelIsSink != null )
					{
						dragEndChannelIsSource = channelInstance;
						targetPosition = RackWirePositionHelper.calculateCenterForComponentPlug( (RackTable)table,
								dataModel,
								component,
								dragEndRackComponent,
								dragEndChannelPlug );
						retVal = true;
					}
					else
					{
						// Can't connect sink to sink
					}
				}
				else
				{
					log.error("Oops");
					dragStartPosition = new Point( 100, 100 );
					// At least it won't null pointer everywhere.
				}
			}
		}
		return retVal;
	}

	@Override
	public void endDrag(final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component, final Point dragLocalPoint, final Point dragEndPoint)
		throws RecordNotFoundException, DatastoreException, MAConstraintViolationException
	{
		final MadChannelInstance sourceChannel = ( dragStartChannelIsSource != null ? dragStartChannelIsSource : dragEndChannelIsSource );
		final RackComponent source = (dragStartChannelIsSource != null ? dragStartRackComponent : dragEndRackComponent );
		final MadChannelInstance sinkChannel = ( dragStartChannelIsSink != null ? dragStartChannelIsSink : dragEndChannelIsSink );
		final RackComponent sink = ( dragStartChannelIsSink != null ? dragStartRackComponent : dragEndRackComponent );

		// Now if either the source or destination component is the master IO component, we should add an IO link
		final RackComponent mic = dataModel.getContentsAtPosition( 0, 0 );
		if( source == mic )
		{
			actionListener.guiAddRackIOLink( sourceChannel, sink, sinkChannel );
		}
		else if( sink == mic )
		{
			actionListener.guiAddRackIOLink( sinkChannel, source, sourceChannel );
		}
		else
		{
			actionListener.guiAddRackLink( source, sourceChannel, sink, sinkChannel );
		}

		cleanupAfterDrag( table );
	}

	@Override
	public void endInvalidDrag(final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component, final Point dragLocalPoint, final Point dragEndPoint)
	{
		cleanupAfterDrag( table );
	}

	private void cleanupAfterDrag( final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table )
	{
		// SCROLL
		// Remove the auto scroll behaviour
		scrollingMouseListener.stop();
		table.removeMouseMotionListener( scrollingMouseListener );

		currentWireDecorationHint.setActive( false );

		// Clear the region hint too
		regionHintDecorator.setActive( false );

		// Finally clear up the internal variables we used during the move.
//		dragSourceRenderedRectangle = null;
		dragStartRackComponent = null;
		dragStartChannelPlug = null;
		dragStartPosition = null;
		dragStartChannelIsSink = null;
		dragStartChannelIsSource = null;
		dragEndRackComponent = null;
		dragEndChannelPlug = null;
		targetPosition = null;
		dragEndChannelIsSink = null;
		dragEndChannelIsSource = null;
	}

	@Override
	public void setRackDataModel(final RackDataModel rackDataModel)
	{
		this.dataModel = rackDataModel;
	}

	@Override
	public boolean isMouseOverPopupSource(
			final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component, final Point localPoint,
			final Point tablePoint )
	{
		// No popups for wire drag for the moment
		return false;
	}

	@Override
	public void doPopup( final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final AbstractGuiAudioComponent component, final Point localPoint,
			final Point tablePoint )
	{
	}

	@Override
	public void destroy()
	{
		dataModel = null;
	}

}
