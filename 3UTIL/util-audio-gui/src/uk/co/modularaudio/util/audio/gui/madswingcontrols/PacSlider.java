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

package uk.co.modularaudio.util.audio.gui.madswingcontrols;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class PacSlider extends JSlider implements ChangeListener
{
	private static final long serialVersionUID = 5049377386963678868L;

	private int previousValue = -1;

	public PacSlider()
	{
		this.addChangeListener( this );
	}

	public String getControlValue()
	{
		final int valueAsInt = getValue();
		return Integer.toString( valueAsInt ) + "";
	}

	public void receiveControlValue( final String strValue )
	{
		if( strValue != null && !strValue.equals(""))
		{
			try
			{
				final int value = Integer.parseInt( strValue );
				this.setValue( value );
			}
			catch (final NumberFormatException e)
			{
				// Ignore it... Probably
			}
		}
	}

	@Override
	public void stateChanged( final ChangeEvent e )
	{
		final int newValue = getValue();
		if( e.getSource() == this )
		{
			processValueChange( previousValue, newValue );
			previousValue = newValue;
		}
	}

	public abstract void processValueChange( int previousValue, int newValue );

}
