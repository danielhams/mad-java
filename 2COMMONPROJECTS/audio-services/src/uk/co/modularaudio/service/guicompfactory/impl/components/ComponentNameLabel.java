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

package uk.co.modularaudio.service.guicompfactory.impl.components;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentNameChangeListener;

public class ComponentNameLabel extends JLabel implements RackComponentNameChangeListener
{
	private static final long serialVersionUID = 3688660710324108889L;
//	private static final Color COLOR_CREAM = new Color( 191f/256f, 188f/256f, 121f/256f );
//	private static final Color COLOR_BLACK = new Color( 0f, 0f, 0f );
	
//	private String text = null;
	
//	private RackComponent rackComponent = null;
	
	public ComponentNameLabel( RackComponent rackComponent )
	{
//		this.rackComponent = rackComponent;
		setOpaque( true );
		setFont( this.getFont().deriveFont( 9f ) );
//		setBackground( COLOR_CREAM );
		setText( rackComponent.getComponentName() );
		setBounds( 3, 3, 100, 15 );
		setBorder( new LineBorder( Color.BLACK, 1 ) );
		rackComponent.addNameChangeListener( this );
	}
	
	public void paint( Graphics g )
	{
		super.paint( g );
	}

	@Override
	public void receiveNewName( String newName )
	{
		setText( newName );
	}

}
