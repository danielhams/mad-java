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
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.guicompfactory.impl.components.ComponentNameLabel;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableFrontContainerMiddle extends JPanel
{
	private static final long serialVersionUID = 5700599707006370407L;

	private static Log log = LogFactory.getLog( ResizableFrontContainerMiddle.class.getName() );

	private final FixedYTransparentBorder tBorder;
	private final FixedYTransparentBorder bBorder;

	private final ComponentNameLabel componentNameLabel;
	private final BufferedImage backgroundImage;
	private final RackComponent rc;

	public ResizableFrontContainerMiddle( final ContainerImages ci, final RackComponent rc )
	{
		this.setOpaque( false );
		this.backgroundImage = rc.getUiDefinition().getFrontBufferedImage();
		this.rc = rc;

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
		this.add( new RealComponentFront( rc ), "grow, wrap" );
		this.add( bBorder, "growx" );

		componentNameLabel = new ComponentNameLabel( rc, this );
	}

	@Override
	public void paint( final Graphics g )
	{
//		final Rectangle paintRect = g.getClipBounds();
//		log.debug( "Painting rect " + paintRect.toString() );
		if( backgroundImage != null )
		{
			final int imageWidth = backgroundImage.getWidth();
			final int imageHeight = backgroundImage.getHeight();
			final int width = getWidth();
			final int height = getHeight();

			if( imageWidth != width || imageHeight != height )
			{
				final StringBuilder sb = new StringBuilder("Component ");
				sb.append( rc.getInstance().getDefinition().getId() );
				sb.append( " has badly sized front image: (" );
				sb.append( imageWidth );
				sb.append( ", " );
				sb.append( imageHeight );
				sb.append( ") - component size(" );
				sb.append( width );
				sb.append( ", " );
				sb.append( height );
				sb.append( ")" );
				final String msg = sb.toString();
				log.warn( msg );
			}
//			log.debug("Drawing background image of dims " + backgroundImage.getWidth() + " " + backgroundImage.getHeight() );
			g.drawImage( backgroundImage, 0, 0, width, height, null );
		}
		super.paint( g );
		g.translate( 0, PaintedComponentDefines.FRONT_BOTTOM_TOP_INSET );
		componentNameLabel.paint( g );
	}
}
