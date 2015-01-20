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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableDndPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.RegionHintType;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.popup.DndRackComponentPopup;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.popup.PopupActions;
import uk.co.modularaudio.service.gui.valueobjects.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.scrollpane.AutoScrollingMouseListener;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TablePosition;

public class DndRackDragPolicy implements RackTableDndPolicy
{
	private static Log log = LogFactory.getLog( DndRackDragPolicy.class.getName() );
	
	private RackService rackService = null;
	
	private RackDataModel dataModel = null;
	
	// SCROLL
	private AutoScrollingMouseListener scrollingMouseListener = new AutoScrollingMouseListener();
	
	private DndRackComponentPopup popup = null;

//	private NewDndRackDragDecorations decorations = null;
	private DndRackDragRegionHintDecoration regionHintDecorator = null;
	private DndRackDragGhostHintDecoration ghostHintDecorator = null;
	public DndRackDragPolicy( RackService rackService, GuiService guiService, RackDataModel dataModel, DndRackDragDecorations decorations )
	{
		this.rackService = rackService;
		this.dataModel = dataModel;
//		this.decorations = decorations;
		regionHintDecorator = decorations.getRegionHintDecorator();
		ghostHintDecorator = decorations.getGhostHintDecorator();
		
		PopupActions popupActions = new PopupActions( rackService, guiService );
		
		popup = new DndRackComponentPopup( popupActions );
	}
	
	@Override
	public boolean isMouseOverDndSource( LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent component,
			Point localPoint,
			Point tablePoint)
	{
		boolean haveComponent = component != null;
		boolean inComponentDragRegion = ( haveComponent ? component.isPointLocalDragRegion( localPoint ) : false );
		boolean havePreviousComponent = regionHintSourceAreaImageComponent != null;
		boolean wasInPreviousComponentDragRegion = (regionHintDecorator.isActive() );
		boolean isSameComponent = (haveComponent && havePreviousComponent ? component == regionHintSourceAreaImageComponent : false );

		// Setup the state variables we will use to decide what to update
		if( !haveComponent )
		{
			if( !havePreviousComponent )
			{
				// Neither current nor previous component set
//				log.debug("No component to no component");
			}
			else
			{
				regionHintSourceAreaImageComponent = null;
				if( wasInPreviousComponentDragRegion )
				{
					regionHintDecorator.setActive( false );
				}
//				log.debug("Moved from component to no component");
			}
		}
		else
		{
			// Current over a component
			if( havePreviousComponent && isSameComponent )
			{
				if( inComponentDragRegion )
				{
					setupRegionHintForComponent( table, component );
					regionHintSourceAreaImageComponent = component;
//					log.debug( "Same component to drag region" );
					if( !wasInPreviousComponentDragRegion )
					{
						regionHintDecorator.setActive( true );
					}
				}
				else
				{
					if( wasInPreviousComponentDragRegion )
					{
						regionHintDecorator.setActive( false );
					}
//					log.debug("Same component but not draggable");
				}
			}
			else if( havePreviousComponent && !isSameComponent )
			{
				setupRegionHintForComponent( table, component );
				regionHintSourceAreaImageComponent = component;
//				log.debug("Moved to new component with dragRegion=" + inComponentDragRegion );
				if( inComponentDragRegion && !wasInPreviousComponentDragRegion )
				{
					regionHintDecorator.setActive( true );
				}
				else if( !inComponentDragRegion && wasInPreviousComponentDragRegion )
				{
					regionHintDecorator.setActive( false );
				}
				else if( inComponentDragRegion )
				{
					// Was a drag region to drag region, update the region hint bounds
					regionHintDecorator.updateToNewBounds();
				}
			}
			else
			{
				// From no component to new component
				if( inComponentDragRegion )
				{
					setupRegionHintForComponent( table, component );
					regionHintSourceAreaImageComponent = component;
					regionHintDecorator.setActive( true );
				}
//				log.debug("Moved from no component to new component with isDraggable=" + inComponentDragRegion );
			}
			
		}
		
		dragSourceMouseOffset = tablePoint;
		return inComponentDragRegion;
	}

	private void setupRegionHintForComponent( LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent component )
	{
		Rectangle currentRegionHintRectangle = regionHintDecorator.getRegionHintRectangle();
		RegionHintType currentRegionHintType = regionHintDecorator.getRegionHintType();
		RackComponent tableComponent = table.getTableModelComponentFromGui( component );
		Rectangle rr = component.getRenderedRectangle();
		TablePosition tp = dataModel.getContentsOriginReturnNull( tableComponent );
		Dimension gridSize = table.getGridSize();
		int hintXOffset = tp.x * gridSize.width;
		int hintYOffset = tp.y * gridSize.height;
		Span componentSpan = tableComponent.getCellSpan();
		int hintWidth = componentSpan.x * gridSize.width;
		int hintHeight = componentSpan.y * gridSize.height;
		newRegionHintRectangle.setBounds( hintXOffset, hintYOffset, hintWidth, hintHeight );
		
		if( component != regionHintSourceAreaImageComponent || !newRegionHintRectangle.equals( currentRegionHintRectangle ) ||
				currentRegionHintType != RegionHintType.SOURCE )
		{
			// It's a different hint
			regionHintDecorator.setRegionHintRectangle( RegionHintType.SOURCE, newRegionHintRectangle, rr );
//			log.debug("Over same component, but moved into draggable region from non-drag, new rectangle required");
		}
		else
		{
//			log.debug("Over same component, but moved into draggable region from non-drag, no new rectangle required");
		}
	}
	
	// Stuff used to highlight the possible source of a drag region
	private Rectangle newRegionHintRectangle = new Rectangle(-1, -1);
	private AbstractGuiAudioComponent regionHintSourceAreaImageComponent = null;
	
	// Copies of data the policy hangs on to to help with tests, or with the move itself.
	private AbstractGuiAudioComponent dragSourceGuiComponent = null;
	private RackComponent dragSourceTableComponent = null;
	private Span dragSourceCellSpan = null;
	private Point dragSourceMouseOffset = null;
	private Point dragSourceOriginalOffset = null;
	
	// Setup by start drag
	private DndRackDragPolicyDragTargetHelper dragTargetHelper = null;
	private DndRackDragMatch dragMatch = null;

	@Override
	public void startDrag( LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent component,
			Point dragLocalPoint,
			Point dragTablePoint)
	{
//		log.debug("Drag begun.");
		this.dragSourceGuiComponent = component;
		dragSourceTableComponent = table.getTableModelComponentFromGui( dragSourceGuiComponent );
		dragSourceCellSpan = dragSourceTableComponent.getCellSpan();
		dragSourceOriginalOffset = dragSourceGuiComponent.getLocation();

		dragSourceMouseOffset = new Point( -dragLocalPoint.x, -dragLocalPoint.y );
		ghostHintDecorator.setComponentAndOffset( dragSourceGuiComponent, dragSourceMouseOffset );
		ghostHintDecorator.setActive( true );
		
		// Make sure no position is currently hinted (from a previous failed drag)
		regionHintDecorator.setActive( true );
		
		dragTargetHelper = new DndRackDragPolicyDragTargetHelper( table,
				dataModel,
				dragSourceTableComponent,
				dragSourceCellSpan, 
				dragSourceMouseOffset);

		// SCROLL
		table.addMouseMotionListener( scrollingMouseListener );
	}
	
	@Override
	public boolean isValidDragTarget( LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent component,
			Point dragLocalPoint,
			Point mouseDragTargetPoint)
	{
		boolean isValid = false;
		try
		{
			dragMatch = dragTargetHelper.lookupDragMatchUseCache( dragLocalPoint, 
					mouseDragTargetPoint );
			isValid = dragMatch.canMoveHere;
		}
		catch(DndRackDragTargetLookupException e )
		{
			log.error( e );
			return false;
		}
		catch (NoSuchContentsException e)
		{
			// The table is telling is that the drag source component isn't in the table anymore - perhaps removed by a different thread?
			log.error( e );
			return false;
		}
		// Drag match is setup and valid - now setup the target hint to show if we can drop or not
		int gridSizeWidth = table.getGridSize().width;
		int gridSizeHeight = table.getGridSize().height;
		int regionHintX = gridSizeWidth * dragMatch.colsOffset;
		int regionHintY = gridSizeHeight * dragMatch.rowsOffset;
		int regionHintWidth = dragSourceGuiComponent.getWidth();
		int regionHintHeight = dragSourceGuiComponent.getHeight();
		Rectangle regionHintRectangle = new Rectangle( regionHintX, regionHintY, regionHintWidth, regionHintHeight );
		Rectangle rr = dragSourceGuiComponent.getRenderedRectangle();
		regionHintDecorator.setRegionHintRectangle( (isValid ? RegionHintType.VALID : RegionHintType.INVALID), regionHintRectangle, rr );
		regionHintDecorator.setActive( true );
		
		return isValid;
	}

	@Override
	public void endDrag( LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent component,
			Point dragLocalPoint,
			Point dragEndPoint)
	{
//		log.debug("Drag ended.");
		
		// SCROLL
		// Remove the auto scroll behaviour
		scrollingMouseListener.stop();
		table.removeMouseMotionListener( scrollingMouseListener );
		
		// Now do the move in the underlying model - the gui should already be good enough :-)
		try
		{
//			dataModel.moveContentsToPosition( dragSourceTableComponent, dragMatch.colsOffset, dragMatch.rowsOffset );
			rackService.moveContentsToPosition( dataModel, dragSourceTableComponent, dragMatch.colsOffset, dragMatch.rowsOffset );
//			dataModel.removeContents( dragSourceTableComponent );
//			dataModel.addContentsAtPosition( dragSourceTableComponent, dragMatch.colsOffset, dragMatch.rowsOffset );
		}
		catch (Exception e)
		{
			String msg = "Exception caught finishing the drag: " + e.toString();
			log.error( msg, e);
		}

		cleanupAfterDrag();
	}
	
	@Override
	public void endInvalidDrag( LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent component,
			Point dragLocalPoint,
			Point dragEndPoint)
	{
//		log.debug("Invalid drag ended.");
		// Put the dragged component back to it's original position
		dragSourceGuiComponent.setLocation( dragSourceOriginalOffset );

		cleanupAfterDrag();
	}
	
	private void deactivateDecorators()
	{
		// Clean up GUI drag hint stuff before we do the actual move
		ghostHintDecorator.setActive( false );

		// Clear the region hint too
		regionHintDecorator.setActive( false );
		
		regionHintSourceAreaImageComponent = null;

	}
	
	private void cleanupAfterDrag()
	{
		// Finally clear up the internal variables we used during the move.
		dragSourceGuiComponent = null;
		dragSourceTableComponent = null;
		dragSourceCellSpan = null;
		dragSourceMouseOffset = null;
		
		deactivateDecorators();
	}

	@Override
	public void setRackDataModel(RackDataModel rackDataModel)
	{
		this.dataModel = rackDataModel;
		
	}

	@Override
	public boolean isMouseOverPopupSource(
			LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent component, Point localPoint,
			Point tablePoint )
	{
		boolean isPopupSource = false;
		if( component != null )
		{
			if( component.isPointLocalDragRegion( localPoint ) )
			{
				isPopupSource = true;
			}
		}
		return isPopupSource;
	}

	@Override
	public void doPopup( LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			AbstractGuiAudioComponent guiComponent,
			Point localPoint,
			Point tablePoint )
	{
		log.debug("Would do a rack drag style popup");
		RackComponent rackComponent = table.getTableModelComponentFromGui( guiComponent );
		popup.setPopupData( dataModel, rackComponent, guiComponent );
		popup.show( guiComponent, localPoint.x, localPoint.y );
	}

	@Override
	public void destroy()
	{
		dataModel = null;
	}
}
