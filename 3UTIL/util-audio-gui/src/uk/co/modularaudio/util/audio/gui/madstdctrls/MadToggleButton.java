package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MadToggleButton extends AbstractMadButton implements MouseListener
{
	private static final long serialVersionUID = -2594637398951298132L;

	private static Log log = LogFactory.getLog( MadToggleButton.class.getName() );

	private boolean isOn = false;

	public MadToggleButton( final MadButtonColours colours, final String text, final boolean defaultValue )
	{
		super( colours, text );
		pushedState = (defaultValue ? MadButtonState.IN_NO_MOUSE : MadButtonState.OUT_NO_MOUSE );
		isOn = defaultValue;
	}

	public String getControlValue()
	{
		return Boolean.toString( isOn );
	}

	public void receiveControlValue( final String strValue )
	{
		isOn = Boolean.parseBoolean( strValue );
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
//		log.debug("mouseEntered repaint");
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
//		log.debug("mouseExited repaint");
		repaint();
	}

	@Override
	public void mousePressed( final MouseEvent arg0 )
	{
//		log.debug("Mouse press beginning");
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
				log.error( "Oops - state issue" );
			}
		}
		if( !hasFocus() )
		{
			requestFocusInWindow();
		}

//		log.debug("mousePressed repaint");
		repaint();
	}

	@Override
	public void mouseReleased( final MouseEvent arg0 )
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
//		log.debug("moseReleased repaint");
		repaint();
	}
}
