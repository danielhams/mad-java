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

package uk.co.modularaudio.util.swing.lwtc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingConstants;

public class LWTCSliderPainter
{
//	private static Log log = LogFactory.getLog( LWTCSliderPainter.class.getName() );

	private final LWTCSliderKnobImage horizKnobImage;
	private final LWTCSliderKnobImage vertKnobImage;
	private final LWTCSliderGuideImages guideImages;

	public LWTCSliderPainter( final LWTCSliderColours colours )
	{
		horizKnobImage = new LWTCSliderKnobImage( colours, SwingConstants.HORIZONTAL );
		vertKnobImage = new LWTCSliderKnobImage( colours, SwingConstants.VERTICAL );
		guideImages = new LWTCSliderGuideImages( colours );
	}

	public void paintSlider( final Graphics2D g2d,
			final int orientation,
			final int width, final int height,
			final BoundedRangeModel model )
	{
		final int xCenter = (width / 2);
		final int yCenter = (height / 2);


		final int curValue = model.getValue();
		final int minValue = model.getMinimum();
		final int maxValue = model.getMaximum();
		final int range = maxValue - minValue;
		final float normalisedPos = (curValue - minValue) / (float)range;

		switch( orientation )
		{
			case SwingConstants.HORIZONTAL:
			{
				final int sliderHeight = LWTCSliderKnobImage.H_KNOB_HEIGHT;
				final int yOffset = yCenter - (sliderHeight / 2) + 5;
				g2d.drawImage( guideImages.getHorizStartGuideImage(),
						2, yOffset, null );
				g2d.drawImage( guideImages.getHorizEndGuideImage(),
						width - 4, yOffset, null );
				g2d.drawImage( guideImages.getHorizGuideImage(),
						4, yOffset, width - 8, 7, null );

				final int pixelPositionsAvailable = width - (3*2) - LWTCSliderKnobImage.H_KNOB_WIDTH + 1;
				final int pixelOffset = (int)(3 + (pixelPositionsAvailable * normalisedPos ));
				final BufferedImage hKnobImage = horizKnobImage.getKnobImage();
				g2d.drawImage( hKnobImage, pixelOffset, yOffset - 4, null );
				break;
			}
			case SwingConstants.VERTICAL:
			default:
			{
				final int sliderWidth = LWTCSliderKnobImage.V_KNOB_WIDTH;
				final int xOffset = xCenter - (sliderWidth / 2) + 5;
				g2d.drawImage( guideImages.getVertStartGuideImage(),
						xOffset, 2, null );
				g2d.drawImage( guideImages.getVertEndGuideImage(),
						xOffset, height - 4, null );
				g2d.drawImage( guideImages.getVertGuideImage(),
						xOffset, 4, 7, height - 8, null );
//				g2d.drawImage( guideImages.getVertGuideImage(),
//						xOffset, 4, null );

				final int pixelPositionsAvailable = height - (3*2) - LWTCSliderKnobImage.V_KNOB_HEIGHT + 1;
				final int pixelOffset = (int)(pixelPositionsAvailable * normalisedPos );
				final int reversedOffset = pixelPositionsAvailable - pixelOffset + 3;
				final BufferedImage vKnobImage = vertKnobImage.getKnobImage();
				g2d.drawImage( vKnobImage, xOffset - 4, reversedOffset, null );
				break;
			}
		}
	}
}
