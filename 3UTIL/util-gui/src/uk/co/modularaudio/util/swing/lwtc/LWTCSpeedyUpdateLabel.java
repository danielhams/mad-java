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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

public class LWTCSpeedyUpdateLabel extends JPanel
{
	private static final long serialVersionUID = 9194691044648583845L;

//	private static Log log = LogFactory.getLog( LWTCSpeedyUpdateTextField.class.getName() );

	private String currentText;
	private char[] currentCharArray;

	public LWTCSpeedyUpdateLabel()
	{
		this( LWTCControlConstants.STD_LABEL_COLOURS );
	}

	public LWTCSpeedyUpdateLabel( final LWTCLabelColours colours )
	{
//		super( colours );
		setBackground( colours.getBackground() );
		setForeground( colours.getForeground() );
	}

	@Override
	public void paint( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		final int width = getWidth();
		final int height = getHeight();

		final Font f = getFont();

		// So we can see what is being painted
		g2d.setColor( getBackground() );
		g2d.fillRect( 0, 0, width, height );

		super.paintBorder( g2d );

		g2d.setColor( getForeground() );
		g2d.setFont( f );

		final Rectangle2D stringBounds = f.getStringBounds( currentCharArray, 0, currentCharArray.length, g2d.getFontRenderContext() );

		final int rightOffset = 3;
		final int bottomOffset = 2;
		final int xOffset = (int)(width - stringBounds.getWidth()) - rightOffset;
		final int yOffset = (int)(height - (stringBounds.getHeight()/2)) + bottomOffset;

		g2d.drawChars( currentCharArray, 0, currentCharArray.length, xOffset, yOffset );
	}

	public void setText( final String newText )
	{
		currentText = newText;
		currentCharArray = currentText.toCharArray();
		repaint();
	}
}
