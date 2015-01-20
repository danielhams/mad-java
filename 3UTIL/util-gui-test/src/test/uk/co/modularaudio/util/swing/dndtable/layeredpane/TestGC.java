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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;

public class TestGC extends JPanel implements GuiDndTableComponent, LayeredPaneTableComponent
{
	private static final long serialVersionUID = 180722393429965919L;
	
//	private Color borderColor = new Color( 100, 100, 20 );

	public TestGC(TestTC tableModelComponent)
	{
		this.setOpaque( false );
		this.setForeground( Color.white );
		JLabel label = new JLabel(tableModelComponent.getName());
		label.setForeground( Color.WHITE );
		this.add( label );
	}

	@Override
	public boolean isPointLocalDragRegion(Point localPoint)
	{
//		return localPoint.y < (getHeight() - 20);
		return true;
	}
	
	public void paint( Graphics g )
	{
		Graphics2D g2d = (Graphics2D)g;
		Rectangle bounds = getBounds();
		Color transparentGrey = new Color( 0.5f, 0.5f, 0.5f, 0.95f );
		g2d.setColor( transparentGrey );
		RoundRectangle2D rr = new RoundRectangle2D.Float( 0, 0, bounds.width - 1, bounds.height - 1, 20, 20 );
		g2d.fill( rr );
//		g.fillRect( 0, 0, bounds.width - 1, bounds.height - 20 );
//		g.setColor( Color.MAGENTA );
//		// x y width height
//		g.fillRect( 0, 0, bounds.width, 8 );
//		g.fillRect( bounds.width - 8, 0, 8, bounds.height - 20 );
//		g.fillRect( 0, (bounds.height - 20) - 8, bounds.width, 8 );
//		g.fillRect( 0, 0, 8, bounds.height - 20 );

		super.paint( g );
	}

	@Override
	public void destroy()
	{
	}
}
