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

package uk.co.modularaudio.mads.base.mono_compressor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;

import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class AttenuationMeterLevelMarks extends JComponent
{
	private static final long serialVersionUID = -1692780518068920924L;

	public final static int METER_LABEL_WIDTH = 16;

	private final Dimension preferredSize = new Dimension( METER_LABEL_WIDTH, 50 );

	public final static int METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS = 4;

	private final DbToLevelComputer dbToLevelComputer;

	private static float[] levelsToMark = new float[] { 0.0f, -5.0f, -10.0f, -15.0f, -20.0f, -60.0f, Float.NEGATIVE_INFINITY };

	private final FontMetrics fm;

	public AttenuationMeterLevelMarks( final DbToLevelComputer dbToLevelComputer, final Font f )
	{
		this.dbToLevelComputer = dbToLevelComputer;

		setPreferredSize( preferredSize );
		setFont( f );
		fm = getFontMetrics( f );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();
//		g.setColor( Color.GREEN );
//		g.fillRect( 0, 0, width, height );

		g.setColor( Color.BLACK );

		// If show clip box is set, we need to subtract that from the height too
		final int heightForMarks = height - (2 * METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS) - 2;

		final int fontHeight = fm.getAscent();

		for( int i = 0 ; i < levelsToMark.length ; i++ )
		{
			final float levelToMark = levelsToMark[ i ];
			final float normalisedLevel = dbToLevelComputer.toNormalisedSliderLevelFromDb( levelToMark );
			final float yValForMark = normalisedLevel * heightForMarks;

			final int offsetY = (height - 2) - ( ((int)yValForMark) + METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS );
			// Draw a black line at the appropriate height
			g.drawLine( 0, offsetY, 1, offsetY );
//			g.drawLine( width - 2, offsetY, width - 1, offsetY );

			final String labelStr = MathFormatter.fastFloatPrint( levelToMark, 0, false );

			final int stringWidth = fm.stringWidth( labelStr );
			g.drawString( labelStr, (width - stringWidth) / 2, (int)(offsetY + (fontHeight / 2.0)) - 1 );
		}
	}
}
