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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

public class LWTCSpeedyUpdateTextField extends LWTCTextField
{
	private static final long serialVersionUID = 9194691044648583845L;

//	private static Log log = LogFactory.getLog( LWTCSpeedyUpdateTextField.class.getName() );

	private boolean showSpeedyTextField;
	private String currentText;
	private char[] currentCharArray;

	private int prevCaretPosition = -1;

	private final LWTCSpeedyUpdateTextFieldEventListener ml;

	private class LWTCSpeedyUpdateTextFieldEventListener implements MouseListener, FocusListener
	{
		private final LWTCSpeedyUpdateTextField sutf;

		private boolean hasFocus = false;

		public LWTCSpeedyUpdateTextFieldEventListener( final LWTCSpeedyUpdateTextField sutf )
		{
			this.sutf = sutf;
		};

		@Override
		public void mouseReleased( final MouseEvent e )
		{
			final int modifiers = e.getModifiers();
			if( modifiers == 0 && sutf.isShowingSpeedyTextField() )
			{
				if( hasFocus )
				{
					sutf.setupForActualTextbox();
				}
			}
		}

		@Override
		public void mousePressed( final MouseEvent e )
		{
			final int modifiers = e.getModifiers();
			if( (modifiers & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK &&
					sutf.isShowingSpeedyTextField() )
			{
				if( hasFocus )
				{
					sutf.setupForActualTextbox();
				}
			}
		}

		@Override
		public void mouseExited( final MouseEvent e )
		{
			final int modifiers = e.getModifiers();
			if( modifiers == 0 && !sutf.isShowingSpeedyTextField() )
			{
				if( !hasFocus )
				{
					sutf.setupForSpeedyTextbox();
				}
			}
		}

		@Override
		public void mouseEntered( final MouseEvent e )
		{
			final int modifiers = e.getModifiers();
			if( modifiers == 0 && sutf.isShowingSpeedyTextField() )
			{
				if( hasFocus )
				{
					sutf.setupForActualTextbox();
				}
			}
		}

		@Override
		public void mouseClicked( final MouseEvent e )
		{
		}

		@Override
		public void focusGained( final FocusEvent e )
		{
			hasFocus = true;
		}

		@Override
		public void focusLost( final FocusEvent e )
		{
			hasFocus = false;
		}
	}

	public LWTCSpeedyUpdateTextField()
	{
		this( LWTCControlConstants.STD_TEXTFIELD_COLOURS );
	}

	public LWTCSpeedyUpdateTextField( final LWTCTextFieldColours colours )
	{
		super( colours );

		ml = new LWTCSpeedyUpdateTextFieldEventListener( this );

		this.addMouseListener( ml );

		this.addFocusListener( ml );
	}

	public void setupForSpeedyTextbox()
	{
		showSpeedyTextField = true;
		currentText = getText();
		currentCharArray = currentText.toCharArray();
		prevCaretPosition = getCaretPosition();
		repaint();
	}

	public void setupForActualTextbox()
	{
		showSpeedyTextField = false;
		updateTextfieldText( currentText );
		if( prevCaretPosition != -1 && prevCaretPosition < currentText.length() )
		{
			setCaretPosition( prevCaretPosition );
		}
		repaint();
	}

	@Override
	public void paint( final Graphics g )
	{
		if( !showSpeedyTextField )
		{
			super.paint( g );
		}
		else
		{
			final Graphics2D g2d = (Graphics2D)g;
			final int width = getWidth();
			final int height = getHeight();

			final Font f = getFont();

			// So we can see what is being painted
			g2d.setColor( getBackground() );
			g2d.fillRect( 0, 0, width, height );

			super.paintBorder( g2d );

			g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
			g2d.setColor( getForeground() );
			g2d.setFont( f );

			final Rectangle2D stringBounds = f.getStringBounds( currentCharArray, 0, currentCharArray.length, g2d.getFontRenderContext() );

			final int rightOffset = 3;
			final int bottomOffset = 2;
			final int xOffset = (int)(width - stringBounds.getWidth()) - rightOffset;
			final int yOffset = (int)(height - (stringBounds.getHeight()/2)) + bottomOffset;

			g2d.drawChars( currentCharArray, 0, currentCharArray.length, xOffset, yOffset );
		}
	}

	private void updateTextfieldText( final String newText )
	{
		super.setText( currentText );
	}

	@Override
	public void setText( final String newText )
	{
		currentText = newText;
		currentCharArray = currentText.toCharArray();
		if( !showSpeedyTextField )
		{
			updateTextfieldText( newText );
		}
		else
		{
			repaint();
		}
	}

	public boolean isShowingSpeedyTextField()
	{
		return showSpeedyTextField;
	}
}
