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

package uk.co.modularaudio.util.swing.lwtc;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class LWTCButton extends AbstractLWTCButton
{
	private static final long serialVersionUID = -2594637398951298132L;

//	private static Log log = LogFactory.getLog( LWTCButton.class.getName() );

	private final boolean isImmediate;

	private class ButtonMouseListener implements MouseListener, MouseMotionListener
	{
		@Override
		public void mouseEntered( final MouseEvent me )
		{
			final int onmask = MouseEvent.BUTTON1_DOWN_MASK;
			if( (me.getModifiersEx() & onmask) != onmask )
			{
				if( !mouseEntered )
				{
					mouseEntered = true;
				}
				repaint();
				me.consume();
			}
		}

		@Override
		public void mouseExited( final MouseEvent me )
		{
			final int onmask = MouseEvent.BUTTON1_DOWN_MASK;
			if( (me.getModifiersEx() & onmask) != onmask )
			{
				if( mouseEntered )
				{
					mouseEntered = false;
				}
				repaint();
				me.consume();
			}
		}

		@Override
		public void mousePressed( final MouseEvent me )
		{
			if( me.getButton() == MouseEvent.BUTTON1 )
			{
				if( !isPushed )
				{
					isPushed = true;
				}
				if( !hasFocus() )
				{
					requestFocusInWindow();
				}
				if( isImmediate )
				{
					receiveClick();
				}
				repaint();
				me.consume();
			}
		}

		@Override
		public void mouseReleased( final MouseEvent me )
		{
			if( me.getButton() == MouseEvent.BUTTON1 )
			{
				if( contains( me.getPoint() ) && isPushed )
				{
					isPushed = false;
					if( !isImmediate )
					{
						receiveClick();
					}
				}
				repaint();
				me.consume();
			}
		}

		@Override
		public void mouseDragged( final MouseEvent me )
		{
			if( isPushed )
			{
				if( !contains( me.getPoint() ) )
				{
					isPushed = false;
					repaint();
				}
			}
			else
			{
				if( contains( me.getPoint() ) )
				{
					isPushed = true;
					repaint();
				}
			}
		}

		@Override
		public void mouseClicked( final MouseEvent me ) {} // NOPMD by dan on 27/04/15 12:23

		@Override
		public void mouseMoved( final MouseEvent me ) {} // NOPMD by dan on 27/04/15 12:23
	};

	private class ButtonKeyListener implements KeyListener
	{
		@Override
		public void keyPressed( final KeyEvent ke )
		{
			final int keyCode = ke.getKeyCode();
			final int modMask = ke.getModifiers();

			if( modMask != 0 )
			{
				return;
			}
			switch( keyCode )
			{
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_ENTER:
				{
					isPushed = true;
					repaint();
					break;
				}
				default:
				{
					break;
				}
			}
			ke.consume();
		}

		@Override
		public void keyReleased( final KeyEvent ke )
		{
			final int keyCode = ke.getKeyCode();
			final int modMask = ke.getModifiers();

			if( modMask != 0 )
			{
				return;
			}
			switch( keyCode )
			{
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_ENTER:
				{
					isPushed = false;
					receiveClick();
					repaint();
					break;
				}
				default:
				{
					break;
				}
			}
			ke.consume();
		}

		@Override
		public void keyTyped( final KeyEvent me ) {} // NOPMD by dan on 27/04/15 12:23
	}

	public LWTCButton( final LWTCButtonColours colours, final String text,
			final boolean isImmediate )
	{
		super( colours, text );
		this.isImmediate = isImmediate;

		final int minWidth = getFontMetrics( getFont() ).stringWidth( text ) + 30;

		final int minHeight = 30;

		final Dimension minSize = new Dimension( minWidth, minHeight );

		this.setMinimumSize( minSize );

		final ButtonMouseListener ml = new ButtonMouseListener();
		this.addMouseListener( ml );
		this.addMouseMotionListener( ml );

		final ButtonKeyListener kl = new ButtonKeyListener();
		this.addKeyListener( kl );
	}

	public String getControlValue() // NOPMD by dan on 27/04/15 12:22
	{
		return "";
	}

	public void receiveControlValue( final String strValue ) // NOPMD by dan on 27/04/15 12:22
	{
	}

	public abstract void receiveClick();

}
