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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MadSliderGuideImages
{
	private static Log log = LogFactory.getLog( MadSliderGuideImages.class.getName() );

	private final static int PLAIN_SIZE = 3;
	private final static int PERIMETER_SIZE = 1;
	private final static int SHADING_SIZE = 1;

	private final static int GUIDE_THICKNESS = PLAIN_SIZE +
			((PERIMETER_SIZE + SHADING_SIZE) * 2);

	private final static int HORIZ_SG_WIDTH = 2;
	private final static int HORIZ_SG_HEIGHT = GUIDE_THICKNESS;
	private final static int HORIZ_G_WIDTH = 1;
	private final static int HORIZ_G_HEIGHT = GUIDE_THICKNESS;
	private final static int HORIZ_EG_WIDTH = 2;
	private final static int HORIZ_EG_HEIGHT = GUIDE_THICKNESS;

	private final static int VERT_SG_WIDTH = GUIDE_THICKNESS;
	private final static int VERT_SG_HEIGHT = 2;
	private final static int VERT_G_WIDTH = GUIDE_THICKNESS;
	private final static int VERT_G_HEIGHT = 1;
	private final static int VERT_EG_WIDTH = GUIDE_THICKNESS;
	private final static int VERT_EG_HEIGHT = 2;

	private final static Composite SIDE_ALPHA_COMPOSITE =
			AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.75f );
	private final static Composite SIDE_OPAQUE_COMPOSITE =
			AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 1.0f );

	private final BufferedImage horizStartGuideImage;
	private final BufferedImage horizGuideImage;
	private final BufferedImage horizEndGuideImage;

	private final BufferedImage vertStartGuideImage;
	private final BufferedImage vertGuideImage;
	private final BufferedImage vertEndGuideImage;

	public MadSliderGuideImages( final MadSliderColours colours )
	{
		horizStartGuideImage = buildHsgi( colours );
		horizGuideImage = buildHgi( colours );
		horizEndGuideImage = buildHegi( colours );

		vertStartGuideImage = buildVsgi( colours );
		vertGuideImage = buildVgi( colours );
		vertEndGuideImage = buildVegi( colours );
	}

	private BufferedImage buildVsgi( final MadSliderColours colours )
	{
		final BufferedImage i = new BufferedImage( VERT_SG_WIDTH, VERT_SG_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = i.createGraphics();

		// See through corners
		g2d.setComposite( SIDE_ALPHA_COMPOSITE );
		g2d.setColor( colours.getSideShade() );
		g2d.fillRect( 0, 0, PLAIN_SIZE + (PERIMETER_SIZE * 2) + 2, 2 );

		g2d.setComposite( SIDE_OPAQUE_COMPOSITE );

		// Unnecessary, already set
		//g2d.setColor( colours.getSideShade() );
		g2d.drawLine( 0, 0, PLAIN_SIZE + 2, 0 );
		g2d.drawLine( 0, 1, 1, 1 );
		g2d.setColor( colours.getSideLight() );
		final int lightXOffset = PLAIN_SIZE + (2*PERIMETER_SIZE) + 1;
		g2d.drawLine( lightXOffset-1, 1, lightXOffset, 1 );

		// Perimeter border
		g2d.setColor( colours.getValleyPerimeter() );
		g2d.drawLine( 1, 1, 1 + PLAIN_SIZE + PERIMETER_SIZE, 1 );

		return i;
	}

	private BufferedImage buildVgi( final MadSliderColours colours )
	{
		final BufferedImage i = new BufferedImage( VERT_G_WIDTH, VERT_G_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = i.createGraphics();

		// Sides
		g2d.setColor( colours.getSideShade() );
		g2d.drawLine( 0, 0, 0, 1 );
		g2d.setColor( colours.getSideLight() );
		final int lightXOffset = PLAIN_SIZE + (2*PERIMETER_SIZE) + 1;
		g2d.drawLine( lightXOffset, 0, lightXOffset, 1 );

		g2d.setColor( colours.getValleyPerimeter() );
		g2d.drawLine( 1, 0, 1 + PLAIN_SIZE + PERIMETER_SIZE, 0 );
		g2d.setColor( colours.getValleyPlain() );
		g2d.drawLine( 2, 0, 1 + PLAIN_SIZE, 0 );

		return i;
	}

	private BufferedImage buildVegi( final MadSliderColours colours )
	{
		final BufferedImage i = new BufferedImage( VERT_EG_WIDTH, VERT_EG_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = i.createGraphics();

		// See through corners
		g2d.setComposite( SIDE_ALPHA_COMPOSITE );
		g2d.setColor( colours.getSideShade() );
		g2d.fillRect( 0, 0, PLAIN_SIZE + (PERIMETER_SIZE * 2) + 2, 2 );

		g2d.setComposite( SIDE_OPAQUE_COMPOSITE );

		// Unnecessary, already set
		//g2d.setColor( colours.getSideShade() );
		g2d.setColor( colours.getSideLight() );
		g2d.drawLine( 1, 1, 1 + PLAIN_SIZE + 2, 1 );
		g2d.setColor( colours.getSideShade() );
		g2d.drawLine( 0, 0, 1, 0 );
		g2d.setColor( colours.getSideLight() );
		final int lightXOffset = PLAIN_SIZE + (2*PERIMETER_SIZE) + 1;
		g2d.drawLine( lightXOffset-1, 0, lightXOffset, 0 );

		// Perimeter border
		g2d.setColor( colours.getValleyPerimeter() );
		g2d.drawLine( 1, 0, 1 + PLAIN_SIZE + PERIMETER_SIZE, 0 );

		return i;
	}

	private BufferedImage buildHsgi( final MadSliderColours colours )
	{
		final BufferedImage i = new BufferedImage( HORIZ_SG_WIDTH, HORIZ_SG_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = i.createGraphics();

		// See through corners
		g2d.setComposite( SIDE_ALPHA_COMPOSITE );
		g2d.setColor( colours.getSideShade() );
		g2d.fillRect( 0, 0, 2, PLAIN_SIZE + (PERIMETER_SIZE * 2) + 2 );

		g2d.setComposite( SIDE_OPAQUE_COMPOSITE );

		// Unnecessary, already set
		//g2d.setColor( colours.getSideShade() );
		g2d.drawLine( 0, 0, 0, PLAIN_SIZE + 2 );
		g2d.drawLine( 1, 0, 1, 1 );
		g2d.setColor( colours.getSideLight() );
		final int lightYOffset = PLAIN_SIZE + (2*PERIMETER_SIZE) + 1;
		g2d.drawLine( 1, lightYOffset-1, 1, lightYOffset );

		// Perimeter border
		g2d.setColor( colours.getValleyPerimeter() );
		g2d.drawLine( 1, 1, 1, 1 + PLAIN_SIZE + PERIMETER_SIZE );

		return i;
	}

	private BufferedImage buildHgi( final MadSliderColours colours )
	{
		final BufferedImage i = new BufferedImage( HORIZ_G_WIDTH, HORIZ_G_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = i.createGraphics();

		// Sides
		g2d.setColor( colours.getSideShade() );
		g2d.drawLine( 0, 0, 1, 0 );
		g2d.setColor( colours.getSideLight() );
		final int lightYOffset = PLAIN_SIZE + (2*PERIMETER_SIZE) + 1;
		g2d.drawLine( 0, lightYOffset, 1, lightYOffset );

		g2d.setColor( colours.getValleyPerimeter() );
		g2d.drawLine( 0, 1, 0, 1 + PLAIN_SIZE + PERIMETER_SIZE );
		g2d.setColor( colours.getValleyPlain() );
		g2d.drawLine( 0, 2, 0, 1 + PLAIN_SIZE );

		return i;
	}

	private BufferedImage buildHegi( final MadSliderColours colours )
	{
		final BufferedImage i = new BufferedImage( HORIZ_EG_WIDTH, HORIZ_EG_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = i.createGraphics();

		// See through corners
		g2d.setComposite( SIDE_ALPHA_COMPOSITE );
		g2d.setColor( colours.getSideShade() );
		g2d.fillRect( 0, 0, 2, PLAIN_SIZE + (PERIMETER_SIZE * 2) + 2 );

		g2d.setComposite( SIDE_OPAQUE_COMPOSITE );

		// Unnecessary, already set
		//g2d.setColor( colours.getSideShade() );
		g2d.setColor( colours.getSideLight() );
		g2d.drawLine( 1, 1, 1, 1 + PLAIN_SIZE + 2 );
		g2d.setColor( colours.getSideShade() );
		g2d.drawLine( 0, 0, 0, 1 );
		g2d.setColor( colours.getSideLight() );
		final int lightYOffset = PLAIN_SIZE + (2*PERIMETER_SIZE) + 1;
		g2d.drawLine( 0, lightYOffset-1, 0, lightYOffset );

		// Perimeter border
		g2d.setColor( colours.getValleyPerimeter() );
		g2d.drawLine( 0, 1, 0, 1 + PLAIN_SIZE + PERIMETER_SIZE );

		return i;
	}

	public BufferedImage getHorizStartGuideImage()
	{
		return horizStartGuideImage;
	}

	public BufferedImage getHorizGuideImage()
	{
		return horizGuideImage;
	}

	public BufferedImage getHorizEndGuideImage()
	{
		return horizEndGuideImage;
	}

	public BufferedImage getVertStartGuideImage()
	{
		return vertStartGuideImage;
	}

	public BufferedImage getVertGuideImage()
	{
		return vertGuideImage;
	}

	public BufferedImage getVertEndGuideImage()
	{
		return vertEndGuideImage;
	}
}
