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

package uk.co.modularaudio.mads.base.soundfile_player2.ui.rollpainter;

import java.awt.Color;

import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterBufferClearer;

public class SoundfileDisplayBufferClearer implements
		RollPainterBufferClearer<SoundfileDisplayBuffer>
{

	private final int width;
	private final int height;
	
	public SoundfileDisplayBufferClearer( int width, int height )
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public void clearBuffer(int bufNum, SoundfileDisplayBuffer bufferToClear)
	{
//		if( bufNum == 0 )
//		{
//			bufferToClear.g.setColor( Color.blue.darker().darker() );
//		}
//		else
//		{
//			bufferToClear.g.setColor( Color.green.darker().darker() );
//		}
		bufferToClear.g.setColor( Color.black );
		bufferToClear.g.fillRect(0, 0, width, height);
	}

}
