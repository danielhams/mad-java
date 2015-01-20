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
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterBufferClearer;


public class WaveDisplayBufferedImageClearer implements RollPainterBufferClearer<WaveDisplayBufferedImage>
{
	private final int displayWidth;
	private final int displayHeight;
//	private final int displayWidthMinusOne;
//	private final int displayHeightMinusOne;

	public WaveDisplayBufferedImageClearer( int displayWidth, int displayHeight )
	{
		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;
//		this.displayWidthMinusOne = displayWidth -1;
///		this.displayHeightMinusOne = displayHeight - 1;
	}

	@Override
	public void clearBuffer( int bufNum, WaveDisplayBufferedImage bufferToClear)
	{
		Graphics g = bufferToClear.g;
		
		g.setColor( AAColours.BACKGROUND );
		g.fillRect( 0, 0, displayWidth, displayHeight );

//		if( bufNum == 0 )
//		{
//			g.setColor( Color.GREEN );
//		}
//		else 
//		{
//			g.setColor( Color.YELLOW );
//		}
//		g.drawRect( 0, 0, displayWidthMinusOne, displayHeightMinusOne );

		int ho2 = displayHeight / 2;
		g.setColor( AAColours.FOREGROUND_WHITISH);
		g.drawLine( 0,  ho2,  displayWidth, ho2);


		
	}

}
