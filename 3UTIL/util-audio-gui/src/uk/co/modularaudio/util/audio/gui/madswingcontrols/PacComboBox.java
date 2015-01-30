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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

public abstract class PacComboBox<E> extends JComboBox<E> implements ItemListener
{
	private static final long serialVersionUID = -5893486140336953577L;

	private int previousIndex = -1;

	public PacComboBox()
	{
		super();
		this.addItemListener( this );
	}

	public String getControlValue()
	{
		final int selectedItemIndex = this.getSelectedIndex();
		return this.getItemAt( selectedItemIndex ).toString();
	}

	public void receiveControlValue( final String strValue )
	{
		int index = -1;
		boolean done = false;
		for( int i = 0 ; !done && i < this.getItemCount() ; i++ )
		{
			final E item = this.getItemAt( i );
			if( item.toString().equals( strValue ) )
			{
				index = i;
				done = true;
			}
		}
		if( done )
		{
			this.setSelectedIndex( index );
		}
	}

	@Override
	public void itemStateChanged( final ItemEvent e )
	{
		if( e.getSource() == this )
		{
			final int newIndex = getSelectedIndex();
			receiveIndexUpdate( previousIndex, newIndex );
			previousIndex = newIndex;
		}
	}

	protected abstract void receiveIndexUpdate( int previousIndex, int newIndex );

}
