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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentNameChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class ComponentNameLabel extends LWTCLabel implements RackComponentNameChangeListener
{
	private static final long serialVersionUID = 3688660710324108889L;

	private final Rectangle bounds;
	private final Component parentForRefresh;

	public ComponentNameLabel( final RackComponent rackComponent, final Component parentForRefresh )
	{
		super( LWTCControlConstants.STD_LABEL_COLOURS, rackComponent.getComponentName() );
		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		setOpaque( true );

		this.parentForRefresh = parentForRefresh;
		bounds = new Rectangle( 3, 3, 100, 15 );
		setBounds( bounds );
		rackComponent.addNameChangeListener( this );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.translate( bounds.x, bounds.y );
		super.paint( g );
	}

	@Override
	public void receiveNewName( final String newName )
	{
		setText( newName );
		parentForRefresh.repaint();
	}

}
