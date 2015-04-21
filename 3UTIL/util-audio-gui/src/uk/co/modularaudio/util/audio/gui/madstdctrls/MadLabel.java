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

package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class MadLabel extends JLabel
{
	private static final long serialVersionUID = 6138681925643386194L;

	public MadLabel( final MadLabelColours colours, final String text )
	{
		super( text );

		this.setBackground( colours.getBackground() );
		this.setForeground( colours.getForeground() );
		final Color borderColor = colours.getBorder();

		this.setBorder( BorderFactory.createLineBorder( borderColor, 1 ) );
	}

}
