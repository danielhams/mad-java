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

package uk.co.modularaudio.util.swing.dndtable.layeredpane;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;

public abstract class LayeredPaneDndTableCompoundPolicy<A extends RackModelTableSpanningContents,
B extends SpanningContentsProperties,C extends Component & LayeredPaneTableComponent & GuiDndTableComponent>
	implements LayeredPaneDndTablePolicy<A,B,C>
{
//	private static Log log = LogFactory.getLog( LayeredPaneDndTableCompoundPolicy.class.getName() );

	// Basically the first to "win"  a match in a call to "isMouseOverDndSource" gets to be the policy for the duration of the drag
	private LayeredPaneDndTablePolicy<A,B,C> currentDragPolicy;

	private final Point resetPoint = new Point(-1,-1);

	public LayeredPaneDndTableCompoundPolicy()
	{
	}

	public abstract ArrayList<LayeredPaneDndTablePolicy<A,B,C>> getPolicyList();

	@Override
	public boolean isMouseOverDndSource( final LayeredPaneDndTable<A,B,C> table, final C component, final Point localPoint, final Point tablePoint)
	{
		final ArrayList<LayeredPaneDndTablePolicy<A,B,C>> policyList = getPolicyList();

		boolean matchedOne = false;

		for( final LayeredPaneDndTablePolicy<A,B,C> testPolicy : policyList )
		{
			if( testPolicy.isMouseOverDndSource( table, component, localPoint, tablePoint) )
			{
//				log.debug("Setting current drag policy to " + testPolicy.getClass().getSimpleName() );
				currentDragPolicy = testPolicy;
				matchedOne = true;
				break;
			}
		}

		if( matchedOne )
		{
			// Run a re-pass setting other policies to -1,-1
			for( final LayeredPaneDndTablePolicy<A,B,C> resetPolicy : policyList )
			{
				if( resetPolicy != currentDragPolicy )
				{
					resetPolicy.isMouseOverDndSource( table, null, resetPoint, resetPoint );
				}
			}
		}

		return matchedOne;
	}

	@Override
	public void startDrag( final LayeredPaneDndTable<A,B,C> table, final C component, final Point dragLocalPoint, final Point dragStartPoint) throws RecordNotFoundException, DatastoreException
	{
		if( currentDragPolicy != null )
		{
			currentDragPolicy.startDrag(table, component, dragLocalPoint, dragStartPoint);
		}
	}

	@Override
	public boolean isValidDragTarget( final LayeredPaneDndTable<A,B,C> table, final C component, final Point dragLocalPoint, final Point dragTablePoint)
	{
		if( currentDragPolicy != null )
		{
//			log.debug("Compound policy asking " + currentDragPolicy.getClass().getSimpleName() + " isValidDragTarget with dlp: " + dragLocalPoint );
			return currentDragPolicy.isValidDragTarget( table, component, dragLocalPoint, dragTablePoint);
		}
		return false;
	}

	@Override
	public void endDrag( final LayeredPaneDndTable<A,B,C> table, final C component, final Point dragLocalPoint, final Point dragEndPoint) throws RecordNotFoundException, DatastoreException, MAConstraintViolationException
	{
		if( currentDragPolicy != null )
		{
			currentDragPolicy.endDrag( table, component, dragLocalPoint, dragEndPoint);
		}
		// Now clear out the currentDragPolicy for the next time around
		currentDragPolicy = null;
	}

	@Override
	public void endInvalidDrag( final LayeredPaneDndTable<A,B,C> table, final C component, final Point dragLocalPoint, final Point dragEndPoint)
	{
		if( currentDragPolicy != null )
		{
			currentDragPolicy.endInvalidDrag( table, component, dragLocalPoint, dragEndPoint);
		}
		// Now clear out the currentDragPolicy for the next time around
		currentDragPolicy = null;
	}

	@Override
	public boolean isMouseOverPopupSource( final LayeredPaneDndTable<A, B, C> table,
			final C component, final Point localPoint, final Point tablePoint )
	{
		final ArrayList<LayeredPaneDndTablePolicy<A,B,C>> policyList = getPolicyList();

		boolean matchedOne = false;

		for( final LayeredPaneDndTablePolicy<A,B,C> testPolicy : policyList )
		{
			if( testPolicy.isMouseOverPopupSource( table, component, localPoint, tablePoint) )
			{
				currentDragPolicy = testPolicy;
				matchedOne = true;
				break;
			}
		}

		if( matchedOne )
		{
			// Run a re-pass setting other policies to -1,-1
			for( final LayeredPaneDndTablePolicy<A,B,C> resetPolicy : policyList )
			{
				if( resetPolicy != currentDragPolicy )
				{
					resetPolicy.isMouseOverPopupSource( table, null, resetPoint, resetPoint );
				}
			}
		}

		return matchedOne;
	}

	@Override
	public void doPopup( final LayeredPaneDndTable<A, B, C> table, final C component,
			final Point localPoint, final Point tablePoint )
	{
		if( currentDragPolicy != null )
		{
			currentDragPolicy.doPopup( table, component, localPoint, tablePoint );
		}
	}
}
