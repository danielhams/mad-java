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

package test.uk.co.modularaudio.util.audio.gui.buffervis;

import java.awt.Color;

import javax.swing.JFrame;

public class BufferVisualiserFrame extends JFrame
{
	private static final long serialVersionUID = -1841622140858949623L;

	public BufferVisualiserFrame( final BufferVisualiser bv )
	{
//		this.setBackground( Color.GREEN );
		this.setBackground( Color.BLACK );
		this.add( bv );
		this.pack();

		this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	}
}
