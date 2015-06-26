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

package uk.co.modularaudio.mads.base.waveroller.ui;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterBufferClearer;

public class WaveRollerBufferCleaner implements RollPainterBufferClearer<WaveRollerBuffer>
{
	private final Rectangle bounds;

	public WaveRollerBufferCleaner( final Rectangle bounds )
	{
		this.bounds = bounds;
	}

	@Override
	public void clearBuffer( final int bufNum, final WaveRollerBuffer bufferToClear )
	{
		final Graphics2D g = bufferToClear.graphics;
//		if( bufNum == 0 )
//		{
//			g.setColor( Color.YELLOW );
//		}
//		else
//		{
//			g.setColor( Color.GREEN );
//		}
		g.setColor( WaveRollerColours.BACKGROUND_COLOR );
		g.fillRect( 0, 0, bounds.width, bounds.height );

		g.setColor( WaveRollerColours.SCALE_AXIS_DETAIL );
		g.drawLine( 0, WaveRollerScaleDisplay.SCALE_MARGIN, bounds.width, WaveRollerScaleDisplay.SCALE_MARGIN );
		final int bottomLineY = bounds.height - 1 - WaveRollerScaleDisplay.SCALE_MARGIN;
		g.drawLine( 0, bottomLineY, bounds.width, bottomLineY );
	}

}
