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

package test.uk.co.modularaudio.util.swing.rollpainter.buffer;

import java.awt.Color;

import test.uk.co.modularaudio.util.swing.rollpainter.RPConstants;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterBufferClearer;

public class BufferClearer implements RollPainterBufferClearer<Buffer>
{
	private final static Color darkRed = new Color( 0.2f, 0.01f, 0.01f );
	private final static Color darkBlue = new Color( 0.01f, 0.01f, 0.2f );
	private final static Color lightRed = new Color( 0.8f, 0.01f, 0.01f );
	private final static Color lightBlue = new Color( 0.01f, 0.01f, 0.8f );

	@Override
	public void clearBuffer( int bufNum, Buffer bufferToClear )
	{
		if( bufNum == 0 )
		{
			bufferToClear.graphics.setColor( darkRed );
		}
		else
		{
			bufferToClear.graphics.setColor( darkBlue );
		}
//		bufferToClear.graphics.setColor( Color.BLACK );
		bufferToClear.graphics.fillRect( 0, 0, RPConstants.RP_CANVAS_WIDTH-1, RPConstants.RP_CANVAS_HEIGHT-1 );
		if( bufNum == 0 )
		{
			bufferToClear.graphics.setColor( lightRed );
		}
		else
		{
			bufferToClear.graphics.setColor( lightBlue );
		}
		bufferToClear.graphics.drawRect( 0, 0, RPConstants.RP_CANVAS_WIDTH-1, RPConstants.RP_CANVAS_HEIGHT-1 );
	}

}
