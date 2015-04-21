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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MadButton extends AbstractMadButton implements MouseListener
{
	private static final long serialVersionUID = -2594637398951298132L;

	private static Log log = LogFactory.getLog( MadButton.class.getName() );

	public MadButton( final MadButtonColours colours, final String text )
	{
		super( colours, text );
	}

	public String getControlValue()
	{
		return "";
	}

	public void receiveControlValue( final String strValue )
	{
	}

	public abstract void receiveClick();

	@Override
	public MouseListener getMouseListener()
	{
		return this;
	}

	@Override
	public void mouseClicked( final MouseEvent arg0 )
	{
	}

	@Override
	public void mouseEntered( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case OUT_NO_MOUSE:
			{
				pushedState = MadButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = MadButtonState.IN_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		repaint();
	}

	@Override
	public void mouseExited( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case OUT_MOUSE:
			{
				pushedState = MadButtonState.OUT_NO_MOUSE;
				break;
			}
			case IN_MOUSE:
			{
				pushedState = MadButtonState.IN_NO_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		repaint();
	}

	@Override
	public void mousePressed( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case IN_MOUSE:
			{
				pushedState = MadButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = MadButtonState.OUT_NO_MOUSE;
				break;
			}
			case OUT_MOUSE:
			{
				pushedState = MadButtonState.IN_MOUSE;
				break;
			}
			case OUT_NO_MOUSE:
			{
				pushedState = MadButtonState.IN_NO_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		if( !hasFocus() )
		{
			requestFocusInWindow();
		}
		repaint();
	}

	@Override
	public void mouseReleased( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case IN_MOUSE:
			{
				pushedState = MadButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = MadButtonState.OUT_NO_MOUSE;
				break;
			}
			case OUT_MOUSE:
			{
				pushedState = MadButtonState.IN_MOUSE;
				break;
			}
			case OUT_NO_MOUSE:
			{
				pushedState = MadButtonState.IN_NO_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		repaint();
		receiveClick();
	}
}
