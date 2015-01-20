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

package uk.co.modularaudio.util.swing.table.layeredpane;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.table.GuiTableEmptyCellPainter;

public class LayeredPaneTableBackgroundComponent extends JPanel
{
	private static final long serialVersionUID = -2408915995418297022L;
	
//	private static Log log = LogFactory.getLog( LayeredPaneTableBackgroundComponent.class.getName() );
	
	private GuiTableEmptyCellPainter emptyCellPainter = null;
	
	private Dimension gridSize = null;
	private int numCols = -1;
	private int numRows = -1;

	public LayeredPaneTableBackgroundComponent( GuiTableEmptyCellPainter emptyCellPainter,
			Dimension gridSize, int numCols, int numRows )
	{
		this.emptyCellPainter = emptyCellPainter;
		this.gridSize = gridSize;
		this.numCols = numCols;
		this.numRows = numRows;
	}

	
	public void paint( Graphics g )
	{
//		Graphics emptyG = g.create();
		if( emptyCellPainter.needSingleBlit() )
		{
			// Use a cache buffered image rendered to the entire size of the table.
			BufferedImage bi = emptyCellPainter.getSingleBlitBufferedImage( gridSize, numCols, numRows );
			g.drawImage( bi, 0, 0, null );
		}
		else
		{
			// For optimisations sake we should only paint what is visible.
			// We do this by passing the current viewing region (clip)
			Rectangle clipBounds = g.getClipBounds();
			
			// Work out the span this gives us
			int spanStartX = (int)Math.floor( (double)(clipBounds.x) / gridSize.width );
			int spanEndX = Math.min( (int)Math.ceil( (double)(clipBounds.x + clipBounds.width) / gridSize.width ), numCols );
			int spanStartY = (int)Math.floor( (double)(clipBounds.y) / gridSize.height );
			int spanEndY = Math.min( (int)Math.ceil( (double)(clipBounds.y + clipBounds.height) / gridSize.height ), numRows );
			
//			SwingTableEmptyCellPaintingVisitor<A, B> visitor = new SwingTableEmptyCellPaintingVisitor<A, B>( g ,
//					gridSize,
//					clipBounds,
//					emptyCellPainter );
//
//			dataModel.visitCells(visitor,  spanStartX, spanStartY, spanEndX - spanStartX, spanEndY - spanStartY, true );
	
			// Using the visitor is expensive - just loop around the span we have
			for( int x = spanStartX ; x < spanEndX ; x++ )
			{
				for( int y = spanStartY ; y < spanEndY ; y++ )
				{
					emptyCellPainter.paintEmptyCell(g,  x * gridSize.width, y * gridSize.height, gridSize.width, gridSize.height );
				}
			}
		}
	}
}
