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

package test.uk.co.modularaudio.util.swing.dndtable.layeredpane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.util.swing.table.GuiTableEmptyCellPainter;

public class TestECP implements GuiTableEmptyCellPainter
{
	
	private Color bgc = new Color( 40, 40, 40 );

	@Override
	public BufferedImage getSingleBlitBufferedImage(Dimension gridSize, int numCols, int numRows)
	{
		return null;
	}

	@Override
	public boolean needSingleBlit()
	{
		return false;
	}

	@Override
	public void paintEmptyCell(Graphics emptyG, int x, int y, int width, int height)
	{
		emptyG.setColor( bgc );
//		emptyG.fillRect( x + 2, y + 2, width - 4, height - 4 );
		emptyG.fillRect( x, y, width, height);
	}

}
