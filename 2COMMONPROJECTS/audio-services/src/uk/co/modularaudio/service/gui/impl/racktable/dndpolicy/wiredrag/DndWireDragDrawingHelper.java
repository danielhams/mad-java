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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;

public class DndWireDragDrawingHelper
{
	static BufferedImage generateRegionHintImage( final AbstractGuiAudioComponent sourceGuiComponent,
			final GuiChannelPlug plug,
			final Color regionHintColor)
	{
		final Rectangle regionRectangle = plug.getBounds();
		final int width = regionRectangle.width + 2;
		final int height = regionRectangle.height + 2;

		final BufferedImage retVal = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = retVal.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				REGION_HINT_BACKGROUND_TRANSPARENCY));
		g2d.setColor( regionHintColor );
		g2d.fillRect( 0, 0, width + 1, height + 1);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				REGION_HINT_OUTLINE_TRANSPARENCY));
		g2d.drawRect( 0, 0, width + 1, height + 1 );
		return retVal;
	}

	static BufferedImage generateRegionHintOutlineImage( final AbstractGuiAudioComponent sourceGuiComponent,
			final GuiChannelPlug plug,
			final Color regionHintColor)
	{
		final Rectangle regionRectangle = plug.getBounds();
		final int width = regionRectangle.width + 1;
		final int height = regionRectangle.height + 1;

		final BufferedImage retVal = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = retVal.createGraphics();
		g2d.setColor( regionHintColor );
		final Stroke dashedStroke = new BasicStroke( 1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.0f, new float[] {3.0f}, 1.0f );
		g2d.setStroke( dashedStroke );
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				REGION_HINT_OUTLINE_TRANSPARENCY));
		g2d.drawRect( 0, 0, width - 1, height - 1 );
		return retVal;
	}

	private static final float REGION_HINT_BACKGROUND_TRANSPARENCY = 0.3f;
	private static final float REGION_HINT_OUTLINE_TRANSPARENCY = 0.5f;

}
