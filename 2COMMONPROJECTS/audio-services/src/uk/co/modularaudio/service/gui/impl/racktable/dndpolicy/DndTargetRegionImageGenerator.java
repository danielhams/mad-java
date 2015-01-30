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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


public class DndTargetRegionImageGenerator extends JPanel
{
//	private static Log log = LogFactory.getLog( DndTargetRegionImageGenerator.class.getName() );

	private static final long serialVersionUID = 6183543013092757291L;

	private static final float STRONG_CHANNEL_VALUE = 1.0f;

	private static final float WEAK_CHANNEL_VALUE = 0.1f;

	private static final Color VALID_REGION_HINT_COLOUR = new Color( WEAK_CHANNEL_VALUE, STRONG_CHANNEL_VALUE, WEAK_CHANNEL_VALUE );
	private static final Color INVALID_REGION_HINT_COLOUR = new Color( STRONG_CHANNEL_VALUE, WEAK_CHANNEL_VALUE, WEAK_CHANNEL_VALUE );
	private static final Color SOURCE_REGION_HINT_COLOUR = Color.ORANGE;

	private RegionHintType regionType = RegionHintType.SOURCE;

	private final RegionImageCache regionImageCache = new RegionImageCache();

	private static final float REGION_HINT_BACKGROUND_TRANSPARENCY = 0.4f;
	private static final float REGION_HINT_OUTLINE_TRANSPARENCY = 0.7f;

	public DndTargetRegionImageGenerator()
	{
		// We are transparent, so make us non-opaque
		setOpaque( false );
		// Use a layout manager that will expand us to the appropriate size
//		setLayout( new GridLayout( 1, 1, 0, 0 ) );
//		this.add( new JLabel("Hello dan"));
//		this.setSize( 50, 50 );
	}

	@Override
	public void paint( final Graphics g )
	{
//		log.debug("The paint is called.");
		final Rectangle bounds = getBounds();
		 final String id = bounds.width + ":" + bounds.height + ":" + regionType.ordinal();
		 BufferedImage bi = null;
		 if( (bi = regionImageCache.getImageFromId( id ) ) == null )
		 {
//			 log.debug("Generating new region image");
			 bi = generateImageFor( bounds, regionType );
			 regionImageCache.putImageForId(  id, bi );
		 }
		 else
		 {
//			 log.debug("Using existing region image");
		 }
		 g.drawImage( bi, 0, 0, null );
//		Graphics2D g2d = (Graphics2D)g;
	}

	private BufferedImage generateImageFor(final Rectangle bounds, final RegionHintType rt )
	{
		final BufferedImage retVal = new BufferedImage( bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = retVal.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				REGION_HINT_BACKGROUND_TRANSPARENCY));
		Color regionColour = null;
		switch( rt )
		{
			case SOURCE:
				return generateRegionHintOutlineImage( bounds );
//				regionColour = SOURCE_REGION_HINT_COLOUR;
//				break;
			case INVALID:
				regionColour = INVALID_REGION_HINT_COLOUR;
				break;
			case VALID:
				regionColour = VALID_REGION_HINT_COLOUR;
				break;
		}
		g2d.setColor( regionColour );
		g2d.fillRect( 0, 0, bounds.width - 1, bounds.height - 1 );
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				REGION_HINT_OUTLINE_TRANSPARENCY));
		g2d.drawRect( 0, 0, bounds.width - 1, bounds.height - 1);

		return retVal;
	}

	private BufferedImage generateRegionHintOutlineImage( final Rectangle bounds )
	{
		final int width = bounds.width;
		final int height = bounds.height;

		final BufferedImage retVal = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = retVal.createGraphics();
		g2d.setColor( SOURCE_REGION_HINT_COLOUR );
		final Stroke dashedStroke = new BasicStroke( 1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.0f, new float[] {3.0f}, 1.0f );
		g2d.setStroke( dashedStroke );
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
//				0.2f));
				REGION_HINT_OUTLINE_TRANSPARENCY));
		g2d.drawRect( 0, 0, width -1, height - 1);
//		g2d.fillRect( 0, 0, width, height);
		return retVal;
	}

	public void setRegionType( final RegionHintType hintType )
	{
		this.regionType = hintType;
	}
}

