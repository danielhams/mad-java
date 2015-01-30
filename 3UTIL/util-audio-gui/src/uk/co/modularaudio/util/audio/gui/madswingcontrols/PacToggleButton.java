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

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JToggleButton;

public abstract class PacToggleButton extends JToggleButton
{
	private static final long serialVersionUID = 3894878069518305522L;

//	private static final Log log = LogFactory.getLog( PacToggleButton.class.getName() );

	protected boolean previousValue;

	public PacToggleButton( final boolean defaultValue )
	{
		previousValue = defaultValue;
		model.setSelected( defaultValue );

		this.setSelected( defaultValue );
		// Make it an anonymous listener so we don't interfere with derived classes wishing
		// to implement action listener
		this.addItemListener( new ItemListener()
		{

			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				final boolean newValue = isSelected();
				receiveUpdateEvent( previousValue, newValue );
				previousValue = newValue;
				updateColours();
			}
		});

		updateColours();
	}

	public String getControlValue()
	{
		final boolean isSelected = isSelected();
		return Boolean.toString( isSelected );
	}

	public void receiveControlValue( final String strValue )
	{
		final boolean isSelected = Boolean.parseBoolean( strValue );
		if( isSelected )
		{
			doClick();
		}
	}

	public abstract void receiveUpdateEvent( boolean previousValue, boolean newValue );

	protected final void updateColours()
	{
		final boolean newValue = isSelected();
		if( newValue )
		{
			setForeground( Color.RED );
		}
		else
		{
			setForeground( null );
		}
	}

	public void setButtonSelected( final boolean selected )
	{
		setSelected( selected );
		previousValue = selected;
		updateColours();
	}

}
