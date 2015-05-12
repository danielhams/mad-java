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

public abstract class LWTCToggleButton extends AbstractLWTCButton implements MouseListener
{
	private static final long serialVersionUID = -2594637398951298132L;

	private static Log log = LogFactory.getLog( LWTCToggleButton.class.getName() );

	protected boolean isImmediate;

	protected boolean isOn;

	public LWTCToggleButton( final LWTCButtonColours colours,
			final String text,
			final boolean isImmediate,
			final boolean defaultValue )
	{
		super( colours, text );
		this.isImmediate = isImmediate;
		pushedState = (defaultValue ? MadButtonState.IN_NO_MOUSE : MadButtonState.OUT_NO_MOUSE );
		isOn = defaultValue;
	}

	public void setSelected( final boolean selected )
	{
		final boolean previousValue = isOn;
		setSelectedNoPropogate( selected );
		receiveUpdateEvent( previousValue, isOn );
	}

	public void setSelectedNoPropogate( final boolean selected )
	{
		isOn = selected;
		setupPushedState();
	}

	public boolean isSelected()
	{
		return isOn;
	}

	public String getControlValue()
	{
		return Boolean.toString( isOn );
	}

	public void receiveControlValue( final String strValue )
	{
		setSelected( Boolean.parseBoolean( strValue ) );
	}

	private void setupPushedState()
	{
		switch( pushedState )
		{
			case IN_NO_MOUSE:
			case OUT_NO_MOUSE:
			{
				pushedState = (isOn ? MadButtonState.IN_NO_MOUSE : MadButtonState.OUT_NO_MOUSE );
				break;
			}
			case IN_MOUSE:
			case OUT_MOUSE:
			default:
			{
				pushedState = (isOn ? MadButtonState.IN_MOUSE : MadButtonState.OUT_MOUSE );
				break;
			}
		}
	}

	public abstract void receiveUpdateEvent( boolean previousValue, boolean newValue );

	@Override
	public MouseListener getMouseListener()
	{
		return this;
	}

	@Override
	public void mouseClicked( final MouseEvent me ) // NOPMD by dan on 27/04/15 12:23
	{
		// Do nothing
	}

	@Override
	public void mouseEntered( final MouseEvent me )
	{
		final int onmask = MouseEvent.BUTTON1_DOWN_MASK;
	    if( (me.getModifiersEx() & onmask) != onmask)
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
					log.error( "menter oops - state issue" );
				}
			}
			repaint();
			me.consume();
	    }
	}

	@Override
	public void mouseExited( final MouseEvent me )
	{
		final int onmask = MouseEvent.BUTTON1_DOWN_MASK;
	    if( (me.getModifiersEx() & onmask) != onmask)
	    {
			switch( pushedState )
			{
				case OUT_MOUSE:
				case OUT_NO_MOUSE:
				{
					pushedState = MadButtonState.OUT_NO_MOUSE;
					break;
				}
				case IN_MOUSE:
				case IN_NO_MOUSE:
				{
					pushedState = MadButtonState.IN_NO_MOUSE;
					break;
				}
				default:
				{
					log.error( "mexit oops - state issue" );
				}
			}
			repaint();
			me.consume();
	    }
	}

	@Override
	public void mousePressed( final MouseEvent me )
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
				isOn = true;
				break;
			}
			case OUT_MOUSE:
			{
				pushedState = MadButtonState.IN_MOUSE;
				isOn = false;
				break;
			}
			case OUT_NO_MOUSE:
			{
				pushedState = MadButtonState.IN_NO_MOUSE;
				isOn = false;
				break;
			}
			default:
			{
				log.error( "mpressed oops - state issue" );
			}
		}
		if( !hasFocus() )
		{
			requestFocusInWindow();
		}

		if( isImmediate )
		{
			doStateSwitch();
		}
//		log.debug("mousePressed repaint");
		repaint();
		me.consume();
	}

	private void doStateSwitch()
	{
		// We only change state on press and stay in that
		// state until next click
		final boolean previousValue = isOn;
		switch( pushedState )
		{
			case IN_MOUSE:
			case IN_NO_MOUSE:
			{
				isOn = true;
				break;
			}
			case OUT_MOUSE:
			case OUT_NO_MOUSE:
			default:
			{
				isOn = false;
				break;
			}
		}
		if( isOn != previousValue )
		{
			receiveUpdateEvent( previousValue, isOn );
		}
	}

	@Override
	public void mouseReleased( final MouseEvent me )
	{
//		log.debug("mouseReleased repaint");
		if( !isImmediate )
		{
			doStateSwitch();
		}
		repaint();
		me.consume();
	}
}
