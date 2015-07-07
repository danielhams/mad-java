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

import javax.swing.SwingConstants;

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderIntToFloatConverter;
import uk.co.modularaudio.util.swing.lwtc.LWTCSliderImageCache.ImagesForColours;

public class LWTCSliderPainter
{
//	private static Log log = LogFactory.getLog( LWTCSliderPainter.class.getName() );

	private final LWTCSliderKnobImage horizKnobImage;
	private final LWTCSliderKnobImage vertKnobImage;
	private final LWTCSliderGuidePainter guidePainter;

	public LWTCSliderPainter( final LWTCSliderColours colours )
	{
		final LWTCSliderImageCache imageCache = LWTCSliderImageCache.getInstance();
		final ImagesForColours sliderImages = imageCache.getImagesForColours( colours );
		horizKnobImage = sliderImages.horizKnobImage;
		vertKnobImage = sliderImages.vertKnobImage;
		guidePainter = new LWTCSliderGuidePainter( colours );
	}

	public void paintSlider( final Graphics2D g2d,
			final int orientation,
			final int width, final int height,
			final SliderDisplayModel model )
	{
		final int xCenter = (width / 2);
		final int yCenter = (height / 2);

		final SliderIntToFloatConverter sitfc = model.getIntToFloatConverter();

		final float curValue = model.getValue();
		final int curIntValue = sitfc.floatValueToSliderIntValue( model, curValue );
		final float minValue = model.getMinValue();
		final int minIntValue = sitfc.floatValueToSliderIntValue( model, minValue );
		final float maxValue = model.getMaxValue();
		final int maxIntValue = sitfc.floatValueToSliderIntValue( model, maxValue );
		final int intRange = maxIntValue - minIntValue;
		final float normalisedPos = (curIntValue - minIntValue) / (float)intRange;

		guidePainter.paint( g2d, width, height, orientation );

		switch( orientation )
		{
			case SwingConstants.HORIZONTAL:
			{
				final int sliderHeight = LWTCSliderKnobImage.H_KNOB_HEIGHT;
				final int yOffset = yCenter - (sliderHeight / 2) + 1;
				final int pixelPositionsAvailable = width - (3*2) - LWTCSliderKnobImage.H_KNOB_WIDTH + 1;
				final int pixelOffset = (int)(2 + (pixelPositionsAvailable * normalisedPos ));
				final BufferedImage hKnobImage = horizKnobImage.getKnobImage();
				g2d.drawImage( hKnobImage, pixelOffset+1, yOffset, null );
				break;
			}
			case SwingConstants.VERTICAL:
			default:
			{
				final int sliderWidth = LWTCSliderKnobImage.V_KNOB_WIDTH;
				final int xOffset = xCenter - (sliderWidth / 2) + 1;
				final int pixelPositionsAvailable = height - (3*2) - LWTCSliderKnobImage.V_KNOB_HEIGHT + 1;
				final int pixelOffset = (int)(pixelPositionsAvailable * normalisedPos );
				final int reversedOffset = pixelPositionsAvailable - pixelOffset + 3;
				final BufferedImage vKnobImage = vertKnobImage.getKnobImage();
				g2d.drawImage( vKnobImage, xOffset, reversedOffset, null );
				break;
			}
		}
	}
}
