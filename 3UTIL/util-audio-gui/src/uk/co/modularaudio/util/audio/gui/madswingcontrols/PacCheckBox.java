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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

public abstract class PacCheckBox extends JCheckBox implements ActionListener, ItemListener
{
	private static final long serialVersionUID = 8646941466131693776L;

//	private static Log log = LogFactory.getLog( PacCheckBox.class.getName() );

	private boolean previousValue;

	public PacCheckBox()
	{
		super();
		this.addItemListener( this );
//		this.addActionListener( this );
	}

	public String getControlValue()
	{
		final int valueAsInt = (this.isSelected() ? 1 : 0 );
		return Integer.toString( valueAsInt ) + "";
	}

	public void receiveControlValue( final String strValue )
	{
		final int value = Integer.parseInt( strValue );
		this.setSelected( value == 1 );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		final boolean newValue = this.isSelected();
		if( newValue != previousValue )
		{
			receiveUpdate( previousValue, newValue );
			previousValue = newValue;
		}
	}

	public abstract void receiveUpdate( boolean statusBefore, boolean newStatus );

	@Override
	public void itemStateChanged( final ItemEvent e )
	{
		final boolean newValue = this.isSelected();
		if( newValue != previousValue )
		{
			receiveUpdate( previousValue, newValue );
			previousValue = newValue;
		}
	}
}
