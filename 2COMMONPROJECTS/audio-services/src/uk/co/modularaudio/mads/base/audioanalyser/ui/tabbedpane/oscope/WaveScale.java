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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import java.awt.Graphics;

import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AAColours;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;

public class WaveScale extends PacPanel
{
	private static final long serialVersionUID = 3681098208927058410L;
	
	private final boolean scaleToLeft;
	
	public WaveScale( boolean scaleToLeft )
	{
		this.scaleToLeft = scaleToLeft;
		setOpaque(true);
		setBackground(AAColours.BACKGROUND);
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();
//		if( scaleToLeft )
//		{
//			g.setColor( Color.RED );
//		}
//		else
//		{
//			g.setColor( Color.ORANGE );
//		}
//		g.drawRect( 0, 0, width-1, height-1 );

		g.setColor( AAColours.BACKGROUND );
		g.fillRect( 0, 0, width, height );
		
		g.setColor( AAColours.FOREGROUND_WHITISH );
		int markStartX, markEndX;
		if( scaleToLeft )
		{
			g.drawLine( width - 3,  0,  width - 3,  height - 1 );
			markStartX = width - 9;
			markEndX = width - 3;
		}
		else
		{
			g.drawLine( 2,  0, 2,  height - 1 );
			markStartX = 2;
			markEndX = 7;
		}
		
		drawMarks( g, height, markStartX, markEndX );
	}
	
	private void drawMarks( Graphics g, int height, int startX, int endX )
	{
		// Draw start and end and middle
		g.drawLine( startX,  0, endX, 0 );
		g.drawLine( startX,  height - 1, endX, height - 1 );
		int halfwayY = (height / 2);
		g.drawLine( startX, halfwayY, endX, halfwayY);
	}
}
