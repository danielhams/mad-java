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

import javax.swing.SwingConstants;

public class LWTCSliderGuidePainter
{
//	private static Log log = LogFactory.getLog( LWTCSliderGuidePainter.class.getName() );

	private final LWTCSliderColours colours;

	public final static int PLAIN_SIZE = 3;

	public final static int PERIMETER_SIZE = 1;

	public final static int SHADING_SIZE = 1;

	public final static int GUIDE_THICKNESS = PLAIN_SIZE + ((PERIMETER_SIZE + SHADING_SIZE) * 2);

	public static final int GUIDE_OFFSET = 3;

	public LWTCSliderGuidePainter( final LWTCSliderColours colours )
	{
		this.colours = colours;
	}

	public void paint( final Graphics2D g2d, final int width, final int height, final int orientation )
	{
		switch( orientation )
		{
			case SwingConstants.HORIZONTAL:
			{
				final int widthToPaint = width - LWTCSliderGuidePainter.GUIDE_OFFSET * 2;
				final int guideLastX = LWTCSliderGuidePainter.GUIDE_OFFSET + widthToPaint;
				final int heightToPaint = height - LWTCSliderGuidePainter.GUIDE_THICKNESS;

				final int yTopLineOffset = ((heightToPaint - 1) / 2) + 1;
				final int yBottomLineOffset = yTopLineOffset + LWTCSliderGuidePainter.GUIDE_THICKNESS - 1;

				// Now the main painted body bits
				//g2d.setComposite( LWTCSliderGuideImages.SIDE_OPAQUE_COMPOSITE );

				// Bottom line
				g2d.setColor( colours.getSideShade() );
				g2d.drawLine( LWTCSliderGuidePainter.GUIDE_OFFSET, yTopLineOffset,
						guideLastX, yTopLineOffset );
				// Left line
				g2d.drawLine( LWTCSliderGuidePainter.GUIDE_OFFSET, yTopLineOffset,
						LWTCSliderGuidePainter.GUIDE_OFFSET, yBottomLineOffset );

				// Top line
				g2d.setColor( colours.getSideLight() );
				g2d.drawLine( LWTCSliderGuidePainter.GUIDE_OFFSET, yBottomLineOffset,
						guideLastX, yBottomLineOffset );
				g2d.drawLine( guideLastX, yBottomLineOffset,
						guideLastX, yTopLineOffset );

				// Perimeter
				g2d.setColor( colours.getValleyPerimeter() );
				final int perimStartX = LWTCSliderGuidePainter.GUIDE_OFFSET;
				final int perimStartY = yTopLineOffset + 1;
				final int perimWidth = widthToPaint - 1;
				final int perimHeight = LWTCSliderGuidePainter.GUIDE_THICKNESS - 3;
				g2d.drawRect( perimStartX, perimStartY, perimWidth, perimHeight );

				g2d.setColor( colours.getValleyPlain() );
				final int valStartX = perimStartX + 1;
				final int valStartY = perimStartY + 1;
				final int valWidth = perimWidth - 1;
				final int valHeight = perimHeight - 1;
				g2d.fillRect( valStartX, valStartY, valWidth, valHeight );

				break;
			}
			case SwingConstants.VERTICAL:
			default:
			{
				final int heightToPaint = height - LWTCSliderGuidePainter.GUIDE_OFFSET * 2;
				final int guideLastY = LWTCSliderGuidePainter.GUIDE_OFFSET + heightToPaint;
				final int widthToPaint = width - LWTCSliderGuidePainter.GUIDE_THICKNESS;

				final int xTopLineOffset = ((widthToPaint - 1) / 2) + 1;
				final int xBottomLineOffset = xTopLineOffset + LWTCSliderGuidePainter.GUIDE_THICKNESS - 1;

				// Bottom line
				g2d.setColor( colours.getSideShade() );
				g2d.drawLine( xTopLineOffset, LWTCSliderGuidePainter.GUIDE_OFFSET,
						xTopLineOffset, guideLastY );
				// Left line
				g2d.drawLine( xTopLineOffset, LWTCSliderGuidePainter.GUIDE_OFFSET,
						xBottomLineOffset, LWTCSliderGuidePainter.GUIDE_OFFSET );

				// Top line
				g2d.setColor( colours.getSideLight() );
				g2d.drawLine( xBottomLineOffset, LWTCSliderGuidePainter.GUIDE_OFFSET,
						xBottomLineOffset, guideLastY );
				g2d.drawLine( xBottomLineOffset, guideLastY,
						xTopLineOffset, guideLastY );

				// Perimeter
				g2d.setColor( colours.getValleyPerimeter() );
				final int perimStartY = LWTCSliderGuidePainter.GUIDE_OFFSET;
				final int perimStartX = xTopLineOffset + 1;
				final int perimHeight = heightToPaint - 1;
				final int perimWidth = LWTCSliderGuidePainter.GUIDE_THICKNESS - 3;
				g2d.drawRect( perimStartX, perimStartY, perimWidth, perimHeight );

				g2d.setColor( colours.getValleyPlain() );
				final int valStartY = perimStartY + 1;
				final int valStartX = perimStartX + 1;
				final int valHeight = perimHeight - 1;
				final int valWidth = perimWidth - 1;
				g2d.fillRect( valStartX, valStartY, valWidth, valHeight );
				break;
			}
		}
	}
}
