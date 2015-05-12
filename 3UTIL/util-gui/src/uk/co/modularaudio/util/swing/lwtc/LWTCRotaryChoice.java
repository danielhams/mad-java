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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

public class LWTCRotaryChoice extends JPanel
{
	private static final long serialVersionUID = 7820164670930837222L;

//	private static Log log = LogFactory.getLog( LWTCRotaryChoice.class.getName() );

	private static final int OUTLINE_ARC_WIDTH = 6;
	private static final int OUTLINE_ARC_HEIGHT = OUTLINE_ARC_WIDTH;

//	private static final int INSIDE_ARC_WIDTH = 1;
//	private static final int INSIDE_ARC_HEIGHT = INSIDE_ARC_WIDTH;

	private static final int ROTARY_WIDTH = 28;

	private final DefaultComboBoxModel<String> model;

	private final LWTCRotaryChoiceColours colours;

	private final LWTCRotaryChoiceMouseListener mouseListener;

	private GradientPaint gpGrad;

	private enum FlecheIndex
	{
		NONE,
		BACKWARDS,
		FORWARDS
	};

	private int flecheDown;

	public LWTCRotaryChoice( final LWTCRotaryChoiceColours colours,
			final DefaultComboBoxModel<String> model,
			final boolean opaque )
	{
		this.colours = colours;
		this.model = model;
		setOpaque( opaque );
		setFont( LWTCControlConstants.RACK_FONT );
		setFocusable( true );

		addFocusListener( new FocusListener()
		{

			@Override
			public void focusLost( final FocusEvent e )
			{
				repaint();
			}

			@Override
			public void focusGained( final FocusEvent e )
			{
				repaint();
			}
		} );

		mouseListener = new LWTCRotaryChoiceMouseListener( this );
		this.addMouseListener( mouseListener );
		this.addMouseMotionListener( mouseListener );
	}


	private final void paintControl( final Graphics2D g2d,
			final GradientPaint gp,
			final int width,
			final int height )
	{
		// outer outline
		g2d.setColor( colours.getControlOutline() );
		g2d.fillRoundRect( 0, 0, width, height, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		// Background with gradient
		g2d.setColor( colours.getChoiceBackground() );
		g2d.fillRoundRect( 1, 1, width-3, height-3, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		// Inner outline
		g2d.setColor( colours.getInnerOutline() );
		g2d.drawRoundRect( 1, 1, width-3, height-3, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		final int textpartWidth = width - ROTARY_WIDTH;

		// Now the rotary switch
		final int rotaryLeftBoundary = textpartWidth + 1;
		final int rotaryLeftEdge = rotaryLeftBoundary + 1;
		g2d.drawLine( rotaryLeftBoundary, 2, rotaryLeftBoundary, height-2 );

		g2d.setPaint( gpGrad );

		g2d.fillRoundRect( rotaryLeftEdge, 2, width - 2 - rotaryLeftEdge, height - 4,
				OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		g2d.fillRect( rotaryLeftEdge, 2, 4, height - 4 );

		final int halfHeight = (height / 2) - 1;

		g2d.setColor( colours.getInnerOutline() );
		g2d.drawLine( rotaryLeftBoundary, halfHeight, width - 3, halfHeight );
		g2d.drawLine( rotaryLeftBoundary, halfHeight+1, width - 3, halfHeight+1 );

		g2d.setColor( colours.getHighlight() );

		// Top fleche highlights
		g2d.drawLine( rotaryLeftEdge, 2, width - 5, 2 );
		g2d.drawLine( rotaryLeftEdge, 2, rotaryLeftEdge, halfHeight - 1 );
		// Bottom fleche highlights
		g2d.drawLine( rotaryLeftEdge, halfHeight + 2, width - 3, halfHeight + 2 );
		g2d.drawLine( rotaryLeftEdge, halfHeight + 2, rotaryLeftEdge, height - 3 );

		// Top shadow
		g2d.setColor( colours.getTopShadow() );
		g2d.drawLine( width - 3, 4, width - 3, halfHeight - 2 );
		g2d.drawLine( rotaryLeftEdge+1, halfHeight - 1, width - 3, halfHeight - 1 );
		// Bottom shadow
		g2d.setColor( colours.getBottomShadow() );
		g2d.drawLine( width - 3, halfHeight + 3, width - 3, height - 4 );
		g2d.drawLine( rotaryLeftEdge + 1, height - 3, width - 4, height - 3 );


		final String text = (String)model.getSelectedItem();
		final int index = model.getIndexOf( text );
		final int numChoices = model.getSize();

		boolean backwardsActive = false;
		boolean forwardsActive = false;

		if( numChoices > 1 )
		{
			if( index > 0 )
			{
				backwardsActive = true;
			}

			if( index < (numChoices - 1) )
			{
				forwardsActive = true;
			}
		}

		final int flecheDrawX = textpartWidth + 9;
		final int flecheYIndent = 6;

		drawFleche( g2d, flecheDrawX, flecheYIndent, FlecheIndex.BACKWARDS, backwardsActive );
		drawFleche( g2d, flecheDrawX, halfHeight - 1 + flecheYIndent, FlecheIndex.FORWARDS, forwardsActive );

		// Focus outline
		if( hasFocus() )
		{
			g2d.setColor( colours.getFocus() );
			g2d.drawRect( 5, 5, textpartWidth-8, height-11 );
		}

		if( text != null )
		{
			g2d.setColor( colours.getForegroundText() );

			final Font f = getFont();
			final FontRenderContext frc = g2d.getFontRenderContext();
//			final Rectangle2D stringBounds = f.getStringBounds( text.toCharArray(), 0, text.length(), frc);
			final GlyphVector glyphVector = f.createGlyphVector( frc, text );
			final Rectangle visualBounds = glyphVector.getLogicalBounds().getBounds();

			final int textLeft = 10;
			final int textBottom = (height - visualBounds.height) / 2 - visualBounds.y;

			g2d.drawString( text, textLeft, textBottom );
		}
	}

	private void drawFleche( final Graphics2D g2d,
			final int xOffset,
			final int yOffset,
			final FlecheIndex fleche,
			final boolean active )
	{
		final int[] xPoints = new int[3];
		final int[] yPoints = new int[3];

		if( flecheDown == fleche.ordinal() )
		{
			g2d.setColor( colours.getFlecheDown() );
		}
		else
		{
			if( active )
			{
				g2d.setColor( colours.getFlecheActive() );
			}
			else
			{
				g2d.setColor( colours.getFlecheInactive() );
			}
		}

		if( fleche == FlecheIndex.BACKWARDS )
		{
			xPoints[0] = xOffset;
			yPoints[0] = yOffset + 5;
			xPoints[1] = xOffset + 10;
			yPoints[1] = yOffset + 5;
			xPoints[2] = (xPoints[0] + xPoints[1])/2;
			yPoints[2] = yOffset;
		}
		else
		{
			xPoints[0] = xOffset;
			yPoints[0] = yOffset;
			xPoints[1] = xOffset + 10;
			yPoints[1] = yOffset;
			xPoints[2] = (xPoints[0] + xPoints[1])/2;
			yPoints[2] = yOffset + 5;
		}

		g2d.fillPolygon( xPoints, yPoints, 3 );

	}


	@Override
	public void paintComponent( final Graphics g )
	{
		super.paintComponent( g );
		final int width = getWidth();
		final int height = getHeight();
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		// Don't use - smudges edges too much
//		g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );

		if( gpGrad == null )
		{
			gpGrad = new GradientPaint( 0, 0,
					colours.getContentGradStart(),
					0, height,
					colours.getContentGradEnd() );
		}

		paintControl( g2d,
				gpGrad,
				width, height );
	}


	public int pointInFleche( final Point eventPoint )
	{
		final int width = getWidth();
		final int height = getHeight();
		final int eventX = eventPoint.x;
		final int eventY = eventPoint.y;
		final boolean xIs = (eventX > (width - ROTARY_WIDTH + 2) && eventX < width - 2 );
		final boolean yIs = (eventY > 1 && eventY < height - 2);

		if( xIs && yIs )
		{
			if( eventY < height / 2 )
			{
				return 1;
			}
			else
			{
				return 2;
			}
		}
		else
		{
			return 0;
		}
	}

	public void flechePress( final int flecheIndex )
	{

		final String text = (String)model.getSelectedItem();
		final int index = model.getIndexOf( text );
		final int numChoices = model.getSize();

		boolean backwardsActive = false;
		boolean forwardsActive = false;

		if( numChoices > 1 )
		{
			if( index > 0 )
			{
				backwardsActive = true;
			}

			if( index < (numChoices - 1) )
			{
				forwardsActive = true;
			}
		}

		if( flecheIndex == FlecheIndex.BACKWARDS.ordinal() && backwardsActive )
		{
			final String newElement = model.getElementAt( index - 1 );
			model.setSelectedItem( newElement );
			flecheDown = flecheIndex;
			repaint();
		}
		else if( flecheIndex == FlecheIndex.FORWARDS.ordinal() && forwardsActive )
		{
			final String newElement = model.getElementAt( index + 1 );
			model.setSelectedItem( newElement );
			flecheDown = flecheIndex;
			repaint();
		}

	}


	public void flecheRelease( final int flecheIndex )
	{
		flecheDown = 0;
		repaint();
	}


	public void setMouseOverFlecheIndex( final int flecheIndex )
	{
		// TODO highlight mouse over the fleche so it's obvious it's clickable.
	}
}
