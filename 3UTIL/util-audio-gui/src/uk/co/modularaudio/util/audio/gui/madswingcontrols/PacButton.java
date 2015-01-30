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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public abstract class PacButton extends JButton
{
	private static final long serialVersionUID = 5907687589303985605L;

	public PacButton()
	{
		this.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				receiveEvent( e );
			}
		} );
	}

	public String getControlValue()
	{
		return isSelected() ? "true" : "false";
	}

	public void receiveControlValue( final String value )
	{
		if( value.length() > 0 && value.equals( "true" ) )
		{
			setSelected( true );
		}
		else
		{
			setSelected( false );
		}
	}

	public abstract void receiveEvent( ActionEvent e);
}
