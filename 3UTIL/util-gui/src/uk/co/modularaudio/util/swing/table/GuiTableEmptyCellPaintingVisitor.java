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

package uk.co.modularaudio.util.swing.table;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableCellVisitor;
import uk.co.modularaudio.util.table.impl.TableCell;
import uk.co.modularaudio.util.table.impl.TableCellProperties;


public class GuiTableEmptyCellPaintingVisitor<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties>
	implements TableCellVisitor<A, B>
{
//	private static Log log = LogFactory.getLog( GuiTableEmptyCellPaintingVisitor.class.getName() );
	
	private Graphics g = null;
	private Dimension gridSize = null;
	private Rectangle clipBounds = null;
	private GuiTableEmptyCellPainter emptyCellPainter = null;

	public GuiTableEmptyCellPaintingVisitor(Graphics emptyG,
			Dimension gridSize,
			Rectangle clipBounds,
			GuiTableEmptyCellPainter emptyCellPainter )
	{
		this.g = emptyG;
		this.gridSize = gridSize;
		this.clipBounds = clipBounds;
		this.emptyCellPainter = emptyCellPainter;
	}

	@Override
	public void begin()
	{
	}

	@Override
	public void cleanup()
	{
	}

	@Override
	public boolean isAllDone()
	{
		return false;
	}

	@Override
	public void visit(TableCell<A> a, TableCellProperties<B> b, int indexInModel )
	{
		int cellX = a.getCellX();
		int cellY = a.getCellY();
		int startX = (cellX * gridSize.width );
		int startY = (cellY * gridSize.height );
		emptyCellPainter.paintEmptyCell( g, startX, startY, gridSize.width, gridSize.height );
	}

	public void oldVisit(TableCell<A> a, TableCellProperties<B> b, int indexInModel )
	{
		if( a.getCellContents() == null )
		{
			int cellX = a.getCellX();
			int cellY = a.getCellY();
			int startX = (cellX * gridSize.width );
			int startY = (cellY * gridSize.height );
			Rectangle cellRect = new Rectangle( startX, startY, startX + gridSize.width, startY + gridSize.height );
			if( cellRect.intersects( clipBounds ) )
			{
//				log.debug("Painting empty cell: " + cellRect.toString() );
				emptyCellPainter.paintEmptyCell( g, startX, startY, gridSize.width, gridSize.height );
			}
			else
			{
//				log.debug("Not painting rectangle: "+ cellRect.toString() );
			}
		}
	}
}
