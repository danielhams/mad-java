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

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.TestGC;
import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.TestTC;
import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.TestTP;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.table.GuiTableDataModel;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public class TestDndRackDragPolicyDragTargetHelper
{
	private static Log log = LogFactory.getLog( TestDndRackDragPolicyDragTargetHelper.class.getName() );
	
	private LayeredPaneDndTable<TestTC, TestTP, TestGC> table = null;
	private GuiTableDataModel<TestTC, TestTP> dataModel = null;
	
	// Working set during a drag
	int dataModelNumCols = -1;
	int dataModelNumRows = -1;
	int dragSourceColSpan = -1;
	int dragSourceRowSpan = -1;
	
	private TestTC dragSource = null;
	private Span dragSourceSpan = null;
	private Map<String, TestDndRackDragMatch> dragTargetToValidityMap =
		new HashMap<String, TestDndRackDragMatch>();
	private Point dragSourceMouseOffset = null;
	
	
	private int[] tableLookupIndexes = new int[2];
	
	public TestDndRackDragPolicyDragTargetHelper( LayeredPaneDndTable<TestTC, TestTP, TestGC> table,
			GuiTableDataModel<TestTC, TestTP> dataModel,
			TestTC dragSource,
			Span dragSourceCellSpan,
			Point dragSourceMouseOffset )
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
	
	public TestDndRackDragMatch lookupDragMatchUseCache( Point dragLocalPoint,
			Point mouseDragTargetPoint )
		throws TestDndRackDragTargetLookupException, NoSuchContentsException
	{
//		Point dragTargetPoint = new Point( mouseDragTargetPoint.x, mouseDragTargetPoint.y );
		int[] dragTargetIndexes = new int[2];
		dragTargetIndexes[0] = mouseDragTargetPoint.x;
		dragTargetIndexes[1] = mouseDragTargetPoint.y;
		
		// Now add on half the thing being dragged's size so we are testing where the center of it is
		offsetDragTargetPointBySourceSize( dragTargetIndexes, dragLocalPoint );
		
//		log.debug("Converted mouse drag point into drag target point: " + dragTargetPoint );

		// We need to turn the drag target point into the underlying cell index in the table for our tests
		table.pointToTableIndexes( dragTargetIndexes, tableLookupIndexes );
		
		// See if we already computed a target match for these table indexes - this way we only recompute
		// a match when the mouse passes over cells boundaries into a cell we haven't visited before.
		// This is a BIG performance boost since we aren't doing the full computation for every mouse event.
		TestDndRackDragMatch dragMatch = dragTargetToValidityMap.get( tableLookupIndexes.toString() );
		
		if( dragMatch == null )
		{
//			log.debug("Didn't find a drag target cache match for " + tableIndexes );

			TestDndRackDragMatch tmpMatch = new TestDndRackDragMatch();
			boolean foundMatch = lookupDragMatchsInSourceSpan( tableLookupIndexes, dragSourceSpan, tmpMatch);
			
			if( !foundMatch )
			{
				// Fill in the match with the top left indexes computed before
				// but make sure they are constrained so we fit within the table (due to source span)
				int displayCol = tableLookupIndexes[0];
				int displayRow = tableLookupIndexes[1];
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

			dragTargetToValidityMap.put( tableLookupIndexes.toString(), dragMatch );

			dragMatch = tmpMatch;
		}
		else
		{
			log.debug("Found a drag target cache match for " + tableLookupIndexes );
		}

		return dragMatch;
	}

	private boolean lookupDragMatchsInSourceSpan( int[] tableIndexes, Span objectSpan, TestDndRackDragMatch dragMatch )
			throws TestDndRackDragTargetLookupException, NoSuchContentsException
	{
		boolean foundMatch = false;
		
		// We want solutions to come from the columns first, so the inner loop should be on the columns
		for( int j = 0 ; !foundMatch && j < objectSpan.y ; j++ )
		{
			for( int i = 0 ; !foundMatch && i < objectSpan.x ; i++ )
			{
				int pointToTryX = tableIndexes[0] + i;
				int pointToTryY = tableIndexes[1] + j;
				Point pointToTry = new Point( pointToTryX, pointToTryY );
				foundMatch = lookupDndDragMatchBySlidingObject( pointToTry, dragMatch);
			}
		}
		return foundMatch;
	}

	private boolean lookupDndDragMatchBySlidingObject(Point tableIndexes, TestDndRackDragMatch dragMatch)
			throws TestDndRackDragTargetLookupException, NoSuchContentsException
	{
		boolean foundMatch = false;
		
		int x = tableIndexes.x;
		int y = tableIndexes.y;
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
						int testCol = x + curXOffset;
						int testRow = y + curYOffset;
						if( dataModel.canMoveContentsToPosition( dragSource, testCol, testRow) )
						{
							dragMatch.colsOffset = testCol;
							dragMatch.rowsOffset = testRow;
							dragMatch.canMoveHere = true;
							foundMatch = true;
						}
					}
					catch (TableIndexOutOfBoundsException e)
					{
						// Ignore
					}
				}
			}
		}
		else
		{
			String msg = "Error converting drag target point to indexes in the table.";
			throw new TestDndRackDragTargetLookupException( msg );
		}
		return foundMatch;
	}
	
	private void offsetDragTargetPointBySourceSize( int[] dragTargetIndexes, Point dragLocalPoint )
	{
		// First remove the current "drag local point" to get the "top left" of the dragged thing
		dragTargetIndexes[0] += dragSourceMouseOffset.x;
		dragTargetIndexes[1] += dragSourceMouseOffset.y;
		// Now add on half the grid size width and height to get the "center" of the top left cell of what we are dragging
		dragTargetIndexes[0] += (table.getGridSize().width / 2 );
		dragTargetIndexes[1] += (table.getGridSize().height / 2 );
	}
}
