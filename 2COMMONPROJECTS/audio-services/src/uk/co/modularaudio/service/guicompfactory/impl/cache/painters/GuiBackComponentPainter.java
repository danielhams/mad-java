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

package uk.co.modularaudio.service.guicompfactory.impl.cache.painters;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentPainter;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

public class GuiBackComponentPainter implements GuiComponentPainter
{
//	private static Log log = LogFactory.getLog( GuiBackComponentPainter.class.getName() );

	public GuiBackComponentPainter()
	{
	}

	@Override
	public void drawComponentImage( final RackComponent rackComponent,
			final BufferedImage bufferedImage,
			final boolean useCustomImages,
			final int width,
			final int height )
	{
		final Graphics2D g2d = bufferedImage.createGraphics();
		final MadUiDefinition<?,?> uiDefinition = rackComponent.getUiDefinition();
		paintRoundedComponent( g2d, width, height );
		final BufferedImage rackBackImage = uiDefinition.getBackBufferedImage();
		g2d.setComposite( AlphaComposite.SrcIn );
		if( rackBackImage != null && useCustomImages )
		{
			g2d.drawImage( rackBackImage, 0, 0, width, height, null );
		}
		else
		{
			g2d.setColor( PaintedComponentDefines.BLANK_BACK_COLOR );
			g2d.fillRect( 0, 0, width, height );
		}
		g2d.setComposite( AlphaComposite.SrcOver );
		paintRoundedOutline( g2d, width, height );
	}

	private void paintRoundedComponent( final Graphics2D g2d, final int width, final int height )
	{
		final float arcWidth = PaintedComponentDefines.ARC;
		final float arcHeight = PaintedComponentDefines.ARC;
		final RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float( 0, 0, width, height, arcWidth, arcHeight );
		g2d.setColor( PaintedComponentDefines.HIGHLIGHT_COLOR );
		g2d.fill( roundedRectangle );
	}

	private void paintRoundedOutline( final Graphics2D g2d, final int width, final int height )
	{
		g2d.setStroke( new BasicStroke( 2 ) );
		final float arcWidth = PaintedComponentDefines.ARC;
		final float arcHeight = PaintedComponentDefines.ARC;
		final RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float( 0, 0, width, height, arcWidth, arcHeight );
		g2d.setColor( PaintedComponentDefines.CONTENTS_COLOR );
//		g2d.setColor( Color.ORANGE );
		g2d.draw( roundedRectangle );
	}

}
