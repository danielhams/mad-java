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

package uk.co.modularaudio.mads.base.common.ampmeter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownMeterModel;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.mvc.displayslider.SliderIntToFloatConverter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class AmpMeterMarks extends JPanel
{
	private static final long serialVersionUID = 8804239906450285191L;

//	private static Log log = LogFactory.getLog( MixerFaderLabels.class.getName() );

	private static final float[] VALUES_TO_LABEL = new float[] {
		0,
		-5,
		-10,
		-20,
		-30,
		-50,
		-70,
		Float.NEGATIVE_INFINITY
	};

	private final static MixdownMeterModel METER_MODEL = new MixdownMeterModel();
	private final static SliderIntToFloatConverter METER_VAL_CONVERTER = METER_MODEL.getIntToFloatConverter();

	public static final int METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS = 6;

	private final FontMetrics fm;
	private final int fontHeight;

	public AmpMeterMarks( final Color foregroundColour,
			final boolean opaque )
	{
		setOpaque( opaque );
		setForeground( foregroundColour );
		setMinimumSize( new Dimension( 30, 30 ) );
		final Font font = LWTCControlConstants.getLabelSmallFont();
		setFont( font );
		fm = getFontMetrics( font );
		fontHeight = fm.getAscent();
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		final int width = getWidth();
		final int height = getHeight();
		final int heightForMarks = height - AmpMeter.PREFERRED_METER_WIDTH - 6 - 2;
		final int knobOffset = 4;
		if( isOpaque() )
		{
			g2d.setColor( getBackground() );
			g2d.fillRect( 0, 0, width, height );
		}

		g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		g2d.setColor( getForeground() );
		g2d.setFont( getFont() );
		final float numModelSteps = METER_MODEL.getNumSliderSteps();

		for( final float levelToMark : VALUES_TO_LABEL )
		{
			final int sliderIntValue = METER_VAL_CONVERTER.floatValueToSliderIntValue(
					METER_MODEL, levelToMark );
			final float normalisedLevel = sliderIntValue / numModelSteps;
			final float yValForMark = 3 + normalisedLevel * heightForMarks;

			final int offsetY = (height - knobOffset) - ( (int)yValForMark );
			// Draw a marker line at the appropriate height
			g.drawLine( 0, offsetY, 2, offsetY );
			g.drawLine( width - 3, offsetY, width - 1, offsetY );

			final String labelStr = MathFormatter.fastFloatPrint( levelToMark, 0, false );

			final int stringWidth = fm.stringWidth( labelStr );
			g.drawString( labelStr, (width - stringWidth) / 2, (int)(offsetY + (fontHeight / 2.0)) - 1 );

		}
	}
}
