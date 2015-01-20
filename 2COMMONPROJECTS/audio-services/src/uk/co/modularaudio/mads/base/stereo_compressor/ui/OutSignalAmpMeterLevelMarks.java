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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class OutSignalAmpMeterLevelMarks extends JComponent
{
	private static final long serialVersionUID = -1692780518068920924L;
	
	public final static int METER_LABEL_WIDTH = 30;

	private Dimension preferredSize = new Dimension( METER_LABEL_WIDTH, 50 );
	
	public final static int METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS = 4;
	
	private DbToLevelComputer dbToLevelComputer = null;
	
	private static float[] levelsToMark = new float[] { 0.0f, -5.0f, -10.0f, -20.0f, -30.0f, -50.0f, -70.0f, Float.NEGATIVE_INFINITY };
	
	private boolean showClipBox = false;
	
	private FontMetrics fm = null;

	public OutSignalAmpMeterLevelMarks( DbToLevelComputer dbToLevelComputer, boolean showClipbox, Font f )
	{
		this.dbToLevelComputer = dbToLevelComputer;
		this.showClipBox = showClipbox;

		setPreferredSize( preferredSize );
		setFont( f );
		fm = getFontMetrics( f );
	}
	
	public void paint( Graphics og )
	{
		Graphics2D g = (Graphics2D)og;
		g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		
		int width = getWidth();
		int height = getHeight();
//		g.setColor( Color.GREEN );
//		g.fillRect( 0, 0, width, height );
		
		g.setColor( Color.BLACK );
		
		// If show clip box is set, we need to subtract that from the height too
		int heightForMarks = height - (2 * METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS) - ( showClipBox ? OutSignalAmpMeter.PREFERRED_WIDTH : 0 );

		int fontHeight = fm.getAscent();
		
		for( int i = 0 ; i < levelsToMark.length ; i++ )
		{
			float levelToMark = levelsToMark[ i ];
			float normalisedLevel = dbToLevelComputer.toNormalisedSliderLevelFromDb( levelToMark );
			float yValForMark = normalisedLevel * heightForMarks;
			
			int offsetY = (height - 2) - ( ((int)yValForMark) + METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS );
			// Draw a black line at the appropriate height
			g.drawLine( 0, offsetY, 1, offsetY );
			g.drawLine( width - 2, offsetY, width - 1, offsetY );
			
			String labelStr = null;
			if( levelToMark == Float.NEGATIVE_INFINITY )
			{
				labelStr = "-Inf";
			}
			else
			{
				labelStr = MathFormatter.fastFloatPrint( levelToMark, 0, false );
			}
			int stringWidth = fm.stringWidth( labelStr );
			g.drawString( labelStr, (width - stringWidth) / 2, (int)(offsetY + (fontHeight / 2.0)) - 1 );
		}
	}
}
