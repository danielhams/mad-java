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

package uk.co.modularaudio.util.swing.dndtable.jpanel;

import java.awt.Component;
import java.awt.Point;
import java.util.List;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;

public abstract class JPanelDndTableCompoundPolicy<A extends RackModelTableSpanningContents,
B extends SpanningContentsProperties,C extends Component & GuiDndTableComponent>
	implements JPanelDndTablePolicy<A,B,C>
{
	// Basically the first to "win"  a match in a call to "isMouseOverDndSource" gets to be the policy for the duration of the drag
	private JPanelDndTablePolicy<A,B,C> currentDragPolicy = null;
	
	private Point resetPoint = new Point(-1,-1);
	
	public JPanelDndTableCompoundPolicy()
	{
	}
	
	public abstract List<JPanelDndTablePolicy<A,B,C>> getPolicyList();

	@Override
	public boolean isMouseOverDndSource( JPanelDndTable<A,B,C> table, C component, Point localPoint, Point tablePoint)
	{
		List<JPanelDndTablePolicy<A,B,C>> policyList = getPolicyList();

		boolean matchedOne = false;
		
		for( int i = 0 ; !matchedOne && i < policyList.size() ; i++ )
		{
			JPanelDndTablePolicy<A,B,C> testPolicy = policyList.get( i );
			
			if( testPolicy.isMouseOverDndSource( table, component, localPoint, tablePoint) )
			{
				currentDragPolicy = testPolicy;
				matchedOne = true;
			}
		}
		
		if( matchedOne )
		{
			// Run a re-pass setting other policies to -1,-1
			for( JPanelDndTablePolicy<A,B,C> resetPolicy : policyList )
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
	public void startDrag( JPanelDndTable<A,B,C> table, C component, Point dragLocalPoint, Point dragStartPoint)
	{
		if( currentDragPolicy != null )
		{
			currentDragPolicy.startDrag(table, component, dragLocalPoint, dragStartPoint);
		}
	}

	@Override
	public boolean isValidDragTarget( JPanelDndTable<A,B,C> table, C component, Point dragLocalPoint, Point dragTablePoint)
	{
		if( currentDragPolicy != null )
		{
			return currentDragPolicy.isValidDragTarget( table, component, dragLocalPoint, dragTablePoint);
		}
		return false;
	}

	@Override
	public void endDrag( JPanelDndTable<A,B,C> table, C component, Point dragLocalPoint, Point dragEndPoint)
	{
		if( currentDragPolicy != null )
		{
			currentDragPolicy.endDrag( table, component, dragLocalPoint, dragEndPoint);
		}
		// Now clear out the currentDragPolicy for the next time around
		currentDragPolicy = null;
	}

	@Override
	public void endInvalidDrag( JPanelDndTable<A,B,C> table, C component, Point dragLocalPoint, Point dragEndPoint)
	{
		if( currentDragPolicy != null )
		{
			currentDragPolicy.endInvalidDrag( table, component, dragLocalPoint, dragEndPoint);
		}
		// Now clear out the currentDragPolicy for the next time around
		currentDragPolicy = null;
	}
}
