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

import java.awt.Point;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public class DndRackDragPolicyDragTargetHelper
{
	private static Log log = LogFactory.getLog( DndRackDragPolicyDragTargetHelper.class.getName() );

	private final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table;
	private final RackDataModel dataModel;

	// Working set during a drag
	int dataModelNumCols = -1;
	int dataModelNumRows = -1;
	int dragSourceColSpan = -1;
	int dragSourceRowSpan = -1;

	private final RackComponent dragSource;
	private final Span dragSourceSpan;
//	private Map<String, DndRackDragMatch> dragTargetToValidityMap =
//		new HashMap<String, DndRackDragMatch>();
	private final OpenIntObjectHashMap<DndRackDragMatch> dragTargetPointToValidityMap =
			new OpenIntObjectHashMap<DndRackDragMatch>();
	private final Point dragSourceMouseOffset;

	private final int[] tableIndexesPoint = new int[2];

	public DndRackDragPolicyDragTargetHelper( final LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent> table,
			final RackDataModel dataModel,
			final RackComponent dragSource,
			final Span dragSourceCellSpan,
			final Point dragSourceMouseOffset )
	{
		this.table = table;
		this.dataModel = dataModel;
		this.dragSource = dragSource;
		this.dragSourceSpan = dragSourceCellSpan;
		this.dragSourceMouseOffset = dragSourceMouseOffset;

		dataModelNumCols = dataModel.getNumCols();
		dataModelNumRows = dataModel.getNumRows();

		// Check the source components span and see if by iterating over all it's cells
		// we can fit it somehow in the current target position
		dragSourceColSpan = dragSourceCellSpan.x;
		dragSourceRowSpan = dragSourceCellSpan.y;
	}

	public DndRackDragMatch lookupDragMatchUseCache( final Point dragLocalPoint,
			final Point mouseDragTargetPoint )
		throws DndRackDragTargetLookupException, NoSuchContentsException
	{
		final int[] dragTargetPoint = new int[2];
		dragTargetPoint[0] = mouseDragTargetPoint.x;
		dragTargetPoint[1] = mouseDragTargetPoint.y;

		// Now add on half the thing being dragged's size so we are testing where the center of it
		offsetDragTargetPointBySourceSize( dragTargetPoint, dragLocalPoint );

//		log.debug("Converted mouse drag point into drag target point: " + dragTargetPoint );

		// We need to turn the drag target point into the underlying cell index in the table for our tests
		table.pointToTableIndexes( dragTargetPoint, tableIndexesPoint );

		// See if we already computed a target match for these table indexes - this way we only recompute
		// a match when the mouse passes over cells boundaries into a cell we haven't visited before.
		// This is a BIG performance boost since we aren't doing the full computation for every mouse event.
		final int tableIndexesPointHashcode = tableIndexesPoint.hashCode();
		DndRackDragMatch dragMatch = dragTargetPointToValidityMap.get( tableIndexesPointHashcode );

		if( dragMatch == null )
		{
//			log.debug("Didn't find a drag target cache match for " + tableIndexes );

			final DndRackDragMatch tmpMatch = new DndRackDragMatch();
			final boolean foundMatch = lookupDragMatchsInSourceSpan( tableIndexesPoint, dragSourceSpan, tmpMatch);

			if( !foundMatch )
			{
				// Fill in the match with the top left indexes computed before
				// but make sure they are constrained so we fit within the table (due to source span)
				int displayCol = tableIndexesPoint[0];
				int displayRow = tableIndexesPoint[1];
				if( displayCol + dragSourceColSpan > dataModelNumCols  - 1)
				{
					displayCol = dataModelNumCols - dragSourceColSpan;
				}
				if( displayRow + dragSourceRowSpan > dataModelNumRows  - 1)
				{
					displayRow = dataModelNumRows - dragSourceRowSpan;
				}
				tmpMatch.colsOffset = displayCol;
				tmpMatch.rowsOffset = displayRow;
			}

			dragTargetPointToValidityMap.put( tableIndexesPointHashcode, dragMatch );

			dragMatch = tmpMatch;
		}
		else
		{
			if( log.isTraceEnabled() )
			{
				log.trace("Found a drag target cache match for " + tableIndexesPoint );
			}
		}

		return dragMatch;
	}

	private boolean lookupDragMatchsInSourceSpan( final int[] tableIndex, final Span objectSpan, final DndRackDragMatch dragMatch )
			throws DndRackDragTargetLookupException, NoSuchContentsException
	{
		boolean foundMatch = false;

		// We want solutions to come from the columns first, so the inner loop should be on the columns
		for( int j = 0 ; !foundMatch && j < objectSpan.y ; j++ )
		{
			for( int i = 0 ; !foundMatch && i < objectSpan.x ; i++ )
			{
				final int pointToTryX = tableIndex[0] + i;
				final int pointToTryY = tableIndex[1] + j;
				if( pointToTryX < dataModelNumCols && pointToTryY < dataModelNumRows )
				{
					// TODO check this is correct
					// Also, we only want to test the "border" of the object
					if( (j == 0 || (j == objectSpan.y - 1)) ||
							(i == 0 || (i == objectSpan.x - 1) ) )
					{
						foundMatch = lookupDndDragMatchBySlidingObject( pointToTryX, pointToTryY, dragMatch);
					}
				}
			}
		}
		return foundMatch;
	}

	private boolean lookupDndDragMatchBySlidingObject( final int tableX, final int tableY, final DndRackDragMatch dragMatch)
			throws DndRackDragTargetLookupException, NoSuchContentsException
	{
		boolean foundMatch = false;

		final int x = tableX;
		final int y = tableY;
		if( (x >= 0 && x <= dataModelNumCols) &&
				( y >= 0 && y <= dataModelNumRows ) )
		{
			int curXOffset = 0;
			int curYOffset = 0;
			for( int i = 0 ; !foundMatch && i < dragSourceColSpan ; i++ )
			{
				for( int j = 0 ; !foundMatch && j < dragSourceRowSpan ; j++ )
				{
					curXOffset = -i;
					curYOffset = -j;
					try
					{
						final int testCol = x + curXOffset;
						final int testRow = y + curYOffset;
						if( dataModel.canMoveContentsToPosition( dragSource, testCol, testRow) )
						{
							dragMatch.colsOffset = testCol;
							dragMatch.rowsOffset = testRow;
							dragMatch.canMoveHere = true;
							foundMatch = true;
						}
					}
					catch (final TableIndexOutOfBoundsException e)
					{
						// Ignore
					}
				}
			}
		}
		else
		{
			final String msg = "Error converting drag target point to indexes in the table.";
			throw new DndRackDragTargetLookupException( msg );
		}
		return foundMatch;
	}

	private void offsetDragTargetPointBySourceSize( final int[] dragTargetPoint, final Point dragLocalPoint )
	{
		// First remove the current "drag local point" to get the "top left" of the dragged thing
		dragTargetPoint[0] += dragSourceMouseOffset.x;
		dragTargetPoint[1] += dragSourceMouseOffset.y;
		// Now add on half the grid size width and height to get the "center" of the top left cell of what we are dragging
		dragTargetPoint[0] += (table.getGridSize().width / 2 );
		dragTargetPoint[1] += (table.getGridSize().height / 2 );
	}
}
