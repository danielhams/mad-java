package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MadToggleButton extends AbstractMadButton implements MouseListener
{
	private static final long serialVersionUID = -2594637398951298132L;

	private static Log log = LogFactory.getLog( MadToggleButton.class.getName() );

	public MadToggleButton( final MadButtonColours colours, final boolean defaultValue )
	{
		super( colours );
		pushedState = (defaultValue ? ButtonState.IN_NO_MOUSE : ButtonState.OUT_NO_MOUSE );
	}

	public String getControlValue()
	{
		final boolean isSelected = (pushedState == ButtonState.IN_MOUSE ||
				pushedState == ButtonState.IN_NO_MOUSE );
		return Boolean.toString( isSelected );
	}

	public void receiveControlValue( final String strValue )
	{
		final boolean isSelected = Boolean.parseBoolean( strValue );
		switch( pushedState )
		{
			case IN_NO_MOUSE:
			case OUT_NO_MOUSE:
			{
				pushedState = (isSelected ? ButtonState.IN_NO_MOUSE : ButtonState.OUT_NO_MOUSE );
				break;
			}
			case IN_MOUSE:
			case OUT_MOUSE:
			default:
			{
				pushedState = (isSelected ? ButtonState.IN_MOUSE : ButtonState.OUT_MOUSE );
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
				pushedState = ButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = ButtonState.IN_MOUSE;
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
				pushedState = ButtonState.OUT_NO_MOUSE;
				break;
			}
			case IN_MOUSE:
			{
				pushedState = ButtonState.IN_NO_MOUSE;
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
				pushedState = ButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = ButtonState.OUT_NO_MOUSE;
				break;
			}
			case OUT_MOUSE:
			{
				pushedState = ButtonState.IN_MOUSE;
				break;
			}
			case OUT_NO_MOUSE:
			{
				pushedState = ButtonState.IN_NO_MOUSE;
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
		// We only change state on press and stay in that
		// state until next click
	}
}
