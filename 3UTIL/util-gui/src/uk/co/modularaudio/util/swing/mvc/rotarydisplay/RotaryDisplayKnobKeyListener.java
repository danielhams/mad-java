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

package uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;

public class RotaryDisplayKnobKeyListener implements KeyListener
{
//	private static Log log = LogFactory.getLog( RotaryDisplayKnobKeyListener.class.getName() );

	private final RotaryDisplayController controller;

	public RotaryDisplayKnobKeyListener( final RotaryDisplayController controller )
	{
		this.controller = controller;
	}

	@Override
	public void keyPressed( final KeyEvent me )
	{
		final int keyCode = me.getKeyCode();
		final int modMask = me.getModifiers();

		if( modMask != 0 )
		{
			return;
		}
		switch( keyCode )
		{
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_DOWN:
			{
				controller.moveByMinorTick( this, -1 );
				me.consume();
				break;
			}
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_UP:
			{
				controller.moveByMinorTick( this, +1 );
				me.consume();
				break;
			}
			case KeyEvent.VK_PAGE_DOWN:
			{
				controller.moveByMajorTick( this, -1 );
				me.consume();
				break;
			}
			case KeyEvent.VK_PAGE_UP:
			{
				controller.moveByMajorTick( this, +1 );
				me.consume();
				break;
			}
			default:
			{
				break;
			}
		}
	}

	@Override
	public void keyReleased( final KeyEvent me )
	{
//		log.debug("Key released: " + arg0.toString() );
	}

	@Override
	public void keyTyped( final KeyEvent me )
	{
//		log.debug("Key typed: " + arg0.toString() );
	}
}
