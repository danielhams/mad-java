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

package uk.co.modularaudio.util.audio.gui.wavetablecombo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import uk.co.modularaudio.util.audio.lookuptable.LookupTable;

public class WaveTableIconCache
{
//	private static final Log log = LogFactory.getLog( WaveTableIconCache.class.getName() );

	private static HashMap<LookupTable, Icon> tableToIconMap = new HashMap<LookupTable, Icon>();

//	private static final Dimension ICON_DIMENSION = new Dimension( 28, 25 );
	private static final Dimension ICON_DIMENSION = new Dimension( 20, 17 );
	private static final Color BACKGROUND_COLOR = new Color( 1.0f, 1.0f, 1.0f );
	private static final Color FOREGROUND_COLOR = new Color( 0.0f, 0.0f, 0.0f );

	public static synchronized Icon getIconForWaveTable( final LookupTable wt, final boolean isBipolar )
	{
		Icon retVal = tableToIconMap.get( wt );
		if( retVal == null )
		{
			final BufferedImage generatedImage = generateWaveTableImage( wt, isBipolar );
			retVal = new ImageIcon( generatedImage );
			tableToIconMap.put( wt, retVal );
		}
		return retVal;
	}

	private static BufferedImage generateWaveTableImage( final LookupTable wt, final boolean isBipolar )
	{
		final int width = ICON_DIMENSION.width;
		final int height = ICON_DIMENSION.height;
		final BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		final Graphics2D g2d = bi.createGraphics();
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g2d.setColor( BACKGROUND_COLOR );
		g2d.fillRect( 0, 0, width, height );

		g2d.setColor( FOREGROUND_COLOR );

		final float offset = (isBipolar ? 1.0f : 0.0f );
		final float divisor = (isBipolar ? 2.0f : 1.0f );

		int previousX = -1;
		int previousY = -1;
		for( int s = 0 ; s < width ; s++ )
		{
			final float normalisedIndex = ((float)s / (width - 1));
			final float valAtIndex = wt.getValueAtNormalisedPosition( normalisedIndex );
			final float scaledForWindowVal = ((valAtIndex + offset) / divisor) * (height - 1);
			final int intHeight = (height - 1) - ((int)(scaledForWindowVal));
			if( previousX != -1 )
			{
				g2d.drawLine( previousX, previousY, s, intHeight );
			}

			previousX = s;
			previousY = intHeight;
		}

		return bi;
	}

}
