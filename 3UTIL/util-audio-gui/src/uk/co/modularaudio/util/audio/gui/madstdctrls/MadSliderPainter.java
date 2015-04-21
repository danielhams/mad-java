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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MadSliderPainter
{
	private static Log log = LogFactory.getLog( MadSliderPainter.class.getName() );

	private final MadSliderKnobImage horizKnobImage;
	private final MadSliderKnobImage vertKnobImage;
	private final MadSliderGuideImages guideImages;

	public MadSliderPainter( final MadSliderColours colours )
	{
		horizKnobImage = new MadSliderKnobImage( colours, SwingConstants.HORIZONTAL );
		vertKnobImage = new MadSliderKnobImage( colours, SwingConstants.VERTICAL );
		guideImages = new MadSliderGuideImages( colours );
	}

	public void paintSlider( final Graphics2D g2d, final int orientation, final int width, final int height )
	{
		final int xCenter = (width / 2);
		final int yCenter = (height / 2);
		switch( orientation )
		{
			case SwingConstants.HORIZONTAL:
			{
				final int sliderHeight = MadSliderKnobImage.H_KNOB_HEIGHT;
				final int yOffset = yCenter - (sliderHeight / 2) + 5;
				g2d.drawImage( guideImages.getHorizStartGuideImage(),
						2, yOffset, null );
				g2d.drawImage( guideImages.getHorizEndGuideImage(),
						width - 4, yOffset, null );
				g2d.drawImage( guideImages.getHorizGuideImage(),
						4, yOffset, width - 8, 7, null );

				// Quick hack for now
				final int center = width / 2;
				final BufferedImage hKnobImage = horizKnobImage.getKnobImage();
				final int xOffset = center - (hKnobImage.getWidth() / 2);
				g2d.drawImage( hKnobImage, xOffset, yOffset - 4, null );
				break;
			}
			case SwingConstants.VERTICAL:
			default:
			{
				final int sliderWidth = MadSliderKnobImage.V_KNOB_WIDTH;
				final int xOffset = xCenter - (sliderWidth / 2) + 5;
				g2d.drawImage( guideImages.getVertStartGuideImage(),
						xOffset, 2, null );
				g2d.drawImage( guideImages.getVertEndGuideImage(),
						xOffset, height - 4, null );
				g2d.drawImage( guideImages.getVertGuideImage(),
						xOffset, 4, 7, height - 8, null );
//				g2d.drawImage( guideImages.getVertGuideImage(),
//						xOffset, 4, null );

				// Quick hack for now
				final int center = height / 2;
				final BufferedImage vKnobImage = vertKnobImage.getKnobImage();
				final int yOffset = center - (vKnobImage.getHeight() / 2);
				g2d.drawImage( vKnobImage, xOffset - 4, yOffset, null );
				break;
			}
		}
	}
}
