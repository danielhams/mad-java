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

package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.components.ComponentNameLabel;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableBackContainerMiddle extends JPanel
{
	private static final long serialVersionUID = 5700599707006370407L;

//	private static Log log = LogFactory.getLog( ResizableBackContainerMiddle.class.getName() );

	private final FixedYTransparentBorder tBorder;
	private final FixedYTransparentBorder bBorder;

	private final ComponentNameLabel componentNameLabel;

	private final RealComponentBack realComponentBack;

	public ResizableBackContainerMiddle( final ContainerImages ci, final RealComponentBack realComponentBack,
			final RackComponent rc )
	{
		this.realComponentBack = realComponentBack;

		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "inset 0" );
		msh.addLayoutConstraint( "gap 0" );
//		msh.addLayoutConstraint( "debug" );

		msh.addRowConstraint( "[][grow][]" );

		setLayout( msh.createMigLayout() );

		tBorder = new FixedYTransparentBorder( ci.tibi );
		bBorder = new FixedYTransparentBorder( ci.bibi );

		this.add( tBorder, "growx, wrap" );
		this.add( realComponentBack, "grow, wrap" );
		this.add( bBorder, "growx" );

		componentNameLabel = new ComponentNameLabel( rc, this );
	}

	@Override
	public void paint( final Graphics g )
	{
//		final Rectangle paintRect = g.getClipBounds();
//		log.debug( "Painting rect " + paintRect.toString() );
		super.paint( g );
		g.translate( 0, PaintedComponentDefines.FRONT_BOTTOM_TOP_INSET );
		componentNameLabel.paint( g );
	}

	public GuiChannelPlug getPlugFromPosition( final Point localPoint )
	{
//		log.debug("Asked for plug at position " + localPoint );
		return realComponentBack.getPlugFromPosition( localPoint );
	}

}
