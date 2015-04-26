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

package uk.co.modularaudio.mads.base.imixern.ui.lane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.audio.math.MixdownSliderDbToLevelComputer;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownSliderModel;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCSliderKnobImage;

public class MixerFaderLabels extends JPanel
{
	private static final long serialVersionUID = 8804239906450285191L;

//	private static Log log = LogFactory.getLog( MixerFaderLabels.class.getName() );

	private static final float[] valuesToLabel = new float[] {
		10,
		5,
		0,
		-5,
		-10,
		-20,
		-30,
		-50,
		-70,
		Float.NEGATIVE_INFINITY
	};

	private static final DbToLevelComputer dbToLevelComputer = new MixdownSliderDbToLevelComputer( 1000 );

	private final FontMetrics fm;
	private final int fontHeight;

	public MixerFaderLabels( final MixdownSliderModel faderModel,
			final Color foregroundColour,
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
		final int heightForMarks = height - LWTCSliderKnobImage.V_KNOB_HEIGHT;
		final int knobOffset = LWTCSliderKnobImage.V_KNOB_HEIGHT / 2;
		if( isOpaque() )
		{
			g2d.setColor( getBackground() );
			g2d.fillRect( 0, 0, width, height );
		}

		g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		g2d.setColor( getForeground() );
		g2d.setFont( getFont() );

		for( final float levelToMark : valuesToLabel )
		{
			final float normalisedLevel = dbToLevelComputer.toNormalisedSliderLevelFromDb( levelToMark );
			final float yValForMark = normalisedLevel * heightForMarks;

//			final int offsetY = (height - 2) - ( (int)yValForMark );
			final int offsetY = (height - knobOffset) - ( (int)yValForMark );
			// Draw a black line at the appropriate height
			g.drawLine( 0, offsetY, 1, offsetY );
//			g.drawLine( width - 2, offsetY, width - 1, offsetY );

			String labelStr = null;
			if( levelToMark == Float.NEGATIVE_INFINITY )
			{
				labelStr = "-Inf";
			}
			else
			{
				labelStr = MathFormatter.fastFloatPrint( levelToMark, 0, false );
			}
			final int stringWidth = fm.stringWidth( labelStr );
			g.drawString( labelStr, (width - stringWidth) / 2, (int)(offsetY + (fontHeight / 2.0)) - 1 );

		}
	}
}
