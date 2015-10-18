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

package test.uk.co.modularaudio.util.swing.dndtable.layeredpane.pacdndstuff;

import java.awt.Dimension;
import java.awt.Point;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.TestGC;
import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.TestTC;
import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.TestTP;
import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.pacdndstuff.TestDndRackDragRegionHintDecoration.RegionHintType;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTablePolicy;
import uk.co.modularaudio.util.swing.scrollpane.AutoScrollingMouseListener;
import uk.co.modularaudio.util.swing.table.GuiTableDataModel;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TablePosition;

public class TestDndRackDragPolicy
	implements LayeredPaneDndTablePolicy<TestTC, TestTP, TestGC>
{
	private static Log log = LogFactory.getLog( TestDndRackDragPolicy.class.getName() );

	private GuiTableDataModel<TestTC, TestTP> dataModel = null;

	// SCROLL
	private final AutoScrollingMouseListener scrollingMouseListener = new AutoScrollingMouseListener();

//	private TestDndRackDecorations decorations = null;
	private TestDndRackDragRegionHintDecoration regionHintDecorator = null;
	private TestDndRackDragGhostHintDecoration ghostHintDecorator = null;

	public TestDndRackDragPolicy( final GuiTableDataModel<TestTC, TestTP> dataModel,
			final TestDndRackDecorations decorations )
	{
		this.dataModel = dataModel;
//		this.decorations = decorations;
		regionHintDecorator = decorations.getRegionHintDecorator();
		ghostHintDecorator = decorations.getGhostHintDecorator();
	}

	@Override
	public boolean isMouseOverDndSource( final LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			final TestGC component,
			final Point localPoint,
			final Point tablePoint)
	{
		boolean isDraggable = false;
		if( component != null )
		{
			if( component.isPointLocalDragRegion( localPoint ) )
			{
				if( component != regionHintSourceAreaImageComponent )
				{
					regionHintSourceAreaImageComponent = component;
					// It's a different component - reset the decoration hint so we have the right dimensions

					final TestTC tableComponent = table.getTableModelComponentFromGui( component );
					final TablePosition tp = dataModel.getContentsOriginReturnNull( tableComponent );
					final Dimension gridSize = table.getGridSize();
					final int hintXOffset = tp.x * gridSize.width;
					final int hintYOffset = tp.y * gridSize.height;
					final Span componentSpan = tableComponent.getCellSpan();
					final int hintWidth = componentSpan.x * gridSize.width;
					final int hintHeight = componentSpan.y * gridSize.height;
					regionHintDecorator.setRegionHint( RegionHintType.SOURCE, hintXOffset, hintYOffset, hintWidth, hintHeight );
				}
				else
				{
					// Is same as we have previous "hinted" so don't update the decoration
				}

				regionHintDecorator.setActive( true );
				isDraggable = true;
			}
		}
		if( !isDraggable )
		{
			regionHintDecorator.setActive( false );
		}
		dragSourceMouseOffset = tablePoint;
		return isDraggable;
	}

	// Stuff used to highlight the possible source of a drag region
	private TestGC regionHintSourceAreaImageComponent = null;

	// Copies of data the policy hangs on to to help with tests, or with the move itself.
	private TestGC dragSourceGuiComponent = null;
	private TestTC dragSourceTableComponent = null;
	private Span dragSourceCellSpan = null;
	private Point dragSourceMouseOffset = null;
	private Point dragSourceOriginalOffset = null;

	// Setup by start drag
	private TestDndRackDragPolicyDragTargetHelper dragTargetHelper = null;
	private TestDndRackDragMatch dragMatch = null;

	@Override
	public void startDrag( final LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			final TestGC component,
			final Point dragLocalPoint,
			final Point dragTablePoint)
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

		dragTargetHelper = new TestDndRackDragPolicyDragTargetHelper( table,
				dataModel,
				dragSourceTableComponent,
				dragSourceCellSpan,
				dragSourceMouseOffset);

		// SCROLL
		table.addMouseMotionListener( scrollingMouseListener );
	}

	@Override
	public boolean isValidDragTarget( final LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			final TestGC component,
			final Point dragLocalPoint,
			final Point mouseDragTargetPoint)
	{
		boolean isValid = false;
		try
		{
			dragMatch = dragTargetHelper.lookupDragMatchUseCache( dragLocalPoint,
					mouseDragTargetPoint );
			isValid = dragMatch.canMoveHere;
		}
		catch(final TestDndRackDragTargetLookupException e )
		{
			log.error( e );
			return false;
		}
		catch (final NoSuchContentsException e)
		{
			// The table is telling is that the drag source component isn't in the table anymore - perhaps removed by a different thread?
			log.error( e );
			return false;
		}
		// Drag match is setup and valid - now setup the target hint to show if we can drop or not
		final int gridSizeWidth = table.getGridSize().width;
		final int gridSizeHeight = table.getGridSize().height;
		final int regionHintX = gridSizeWidth * dragMatch.colsOffset;
		final int regionHintY = gridSizeHeight * dragMatch.rowsOffset;
		final int regionHintWidth = dragSourceGuiComponent.getWidth();
		final int regionHintHeight = dragSourceGuiComponent.getHeight();
		regionHintDecorator.setRegionHint( (isValid ? RegionHintType.VALID : RegionHintType.INVALID),
				regionHintX, regionHintY, regionHintWidth, regionHintHeight );
		regionHintDecorator.setActive( true );

		return isValid;
	}

	@Override
	public void endDrag( final LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			final TestGC component,
			final Point dragLocalPoint,
			final Point dragEndPoint)
	{
//		log.debug("Drag ended.");

		// SCROLL
		// Remove the auto scroll behaviour
		scrollingMouseListener.stop();
		table.removeMouseMotionListener( scrollingMouseListener );

		// Now do the move in the underlying model - the gui should already be good enough :-)
		try
		{
			dataModel.moveContentsToPosition( dragSourceTableComponent, dragMatch.colsOffset, dragMatch.rowsOffset );
//			dataModel.removeContents( dragSourceTableComponent );
//			dataModel.addContentsAtPosition( dragSourceTableComponent, dragMatch.colsOffset, dragMatch.rowsOffset );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught finishing the drag: " + e.toString();
			log.error( msg, e);
		}

		cleanupAfterDrag();
	}

	@Override
	public void endInvalidDrag( final LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			final TestGC component,
			final Point dragLocalPoint,
			final Point dragEndPoint)
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
	public boolean isMouseOverPopupSource(
			final LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			final TestGC component, final Point localPoint, final Point tablePoint )
	{
		log.debug("Would test if mouse if over popup source");
		if( component != null )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void doPopup( final LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			final TestGC component, final Point localPoint, final Point tablePoint )
	{
		try
		{
			log.debug("Would attempt to do a popup");
			final TestTC tableComponent = table.getTableModelComponentFromGui( component );
			final TablePosition positionBeforeDelete = dataModel.getContentsOriginReturnNull( tableComponent );
			dataModel.removeContents( tableComponent );
			// Now check it's really gone
			final TestTC testtc = dataModel.getContentsAtPosition( positionBeforeDelete.x, positionBeforeDelete.y );
			if( testtc != null )
			{
				log.error("It's still there and it shouldn't be!");
			}
		}
		catch (final NoSuchContentsException | DatastoreException e)
		{
			log.error( e );
		}
	}
}
