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

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;

public interface LayeredPaneDndTablePolicy<A extends RackModelTableSpanningContents,
	B extends SpanningContentsProperties,
	C extends Component & LayeredPaneTableComponent & GuiDndTableComponent>
{
	/////////////////////////
	// Drag and drops
	/////////////////////////
	
	// How a drag is decided to begin
	boolean isMouseOverDndSource( LayeredPaneDndTable<A,B,C> table, C component, Point localPoint, Point tablePoint );
	void startDrag( LayeredPaneDndTable<A,B,C> table, C component, Point dragLocalPoint, Point dragStartPoint ) throws RecordNotFoundException, DatastoreException;
	
	// How we determine if a particular area can be "dragged" on to.
	boolean isValidDragTarget( LayeredPaneDndTable<A,B,C> table, C component, Point dragLocalPoint, Point dragTablePoint );
	
	// End the drag and do the necessary things
	void endDrag( LayeredPaneDndTable<A, B, C> table, C component, Point dragLocalPoint, Point dragEndPoint ) throws RecordNotFoundException, DatastoreException, MAConstraintViolationException;
	void endInvalidDrag( LayeredPaneDndTable<A, B, C> table, C component, Point dragLocalPoint, Point dragEndPoint );
	
	/////////////////////////
	// Popups
	/////////////////////////
	boolean isMouseOverPopupSource( LayeredPaneDndTable<A,B,C> table, C component, Point localPoint, Point tablePoint );
	void doPopup( LayeredPaneDndTable<A,B,C> table, C component, Point localPoint, Point tablePoint );
	
}
