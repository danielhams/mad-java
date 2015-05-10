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

import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import javax.swing.JPanel;

public abstract class AbstractLWTCButton extends JPanel implements FocusListener
{
	private static final long serialVersionUID = -7622401208667882019L;

//	private static Log log = LogFactory.getLog( AbstractMadButton.class.getName() );

	protected enum MadButtonState
	{
		OUT_NO_MOUSE,
		OUT_MOUSE,
		IN_NO_MOUSE,
		IN_MOUSE,
		NUM_STATES
	};

	private static final int OUTLINE_ARC_WIDTH = 6;
	private static final int OUTLINE_ARC_HEIGHT = OUTLINE_ARC_WIDTH;

	private static final int INSIDE_ARC_WIDTH = 1;
	private static final int INSIDE_ARC_HEIGHT = INSIDE_ARC_WIDTH;

	protected final LWTCButtonColours colours;

	protected MadButtonState pushedState = MadButtonState.OUT_NO_MOUSE;

	protected LWTCButtonStateColours[] coloursForState;
	protected GradientPaint[] gradientPaintsForState;

	protected String text = "";

	protected int fontHeight = 0;
	protected FontMetrics fm;

	public AbstractLWTCButton( final LWTCButtonColours colours )
	{
		this( colours, null );
	}

	public AbstractLWTCButton( final LWTCButtonColours colours, final String textContent )
	{
		setUI( LWTCLookAndFeelHelper.getInstance().getComponentUi( this ) );
		this.colours = colours;

		this.coloursForState = new LWTCButtonStateColours[MadButtonState.NUM_STATES.ordinal()];
		this.gradientPaintsForState = new GradientPaint[MadButtonState.NUM_STATES.ordinal()];
		for( int i = 0 ; i < MadButtonState.NUM_STATES.ordinal() ; ++i )
		{
			final LWTCButtonStateColours stateColours = colours.getButtonColoursForState( MadButtonState.values()[i] );
			coloursForState[i] = stateColours;
			gradientPaintsForState[i] = null;
		}

		this.text = textContent;
		setOpaque( false );

		this.setFont( LWTCControlConstants.RACK_FONT );

		fm = this.getFontMetrics( LWTCControlConstants.RACK_FONT );
		fontHeight = fm.getHeight();

		setFocusable( true );

		this.addMouseListener( getMouseListener() );
		this.addFocusListener( this );
	}

	protected abstract MouseListener getMouseListener();

	private final void paintButton( final Graphics2D g2d,
			final LWTCButtonStateColours stateColours,
			final GradientPaint gp,
			final int width,
			final int height )
	{
		// outline
		g2d.setColor( stateColours.getControlOutline() );
		g2d.fillRoundRect( 0, 0, width, height, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		// Background with gradient
		g2d.setPaint( gp );
		g2d.fillRoundRect( 1, 1, width-2, height-2, INSIDE_ARC_WIDTH, INSIDE_ARC_HEIGHT );

		// Highlight
		g2d.setColor( stateColours.getHighlight() );
		g2d.drawLine( 2, 1, width-3, 1 );

		// Focus outline
		if( hasFocus() )
		{
			g2d.setColor( stateColours.getFocus() );
			g2d.drawRect( 5, 5, width-11, height-11 );
		}

		if( text != null )
		{
			g2d.setColor( stateColours.getForegroundText() );
			final Rectangle stringBounds = fm.getStringBounds( text, g2d ).getBounds();
			final FontRenderContext frc = g2d.getFontRenderContext();
			final GlyphVector glyphVector = getFont().createGlyphVector( frc, text );
			final Rectangle visualBounds = glyphVector.getLogicalBounds().getBounds();

			final int stringWidth = stringBounds.width;
			final int textLeft = (width - stringWidth) / 2;

			final int textBottom = (height - visualBounds.height) / 2 - visualBounds.y;

			g2d.drawString( text, textLeft, textBottom );
		}
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

//		log.debug("Paint called we are in state " + pushedState.toString() );

		final int stateIndex = pushedState.ordinal();

		final LWTCButtonStateColours stateColours = coloursForState[ stateIndex ];

		GradientPaint gpGrad = gradientPaintsForState[ stateIndex ];

		if( gpGrad == null )
		{
			gpGrad = new GradientPaint( 0, 0,
					stateColours.getContentGradStart(),
					0, height,
					stateColours.getContentGradEnd() );
			gradientPaintsForState[ stateIndex ] = gpGrad;
		}

		paintButton( g2d,
				stateColours,
				gpGrad,
				width, height );
	}

	@Override
	public void focusGained( final FocusEvent fe )
	{
//		log.debug("Gained focus repaint");
		repaint();
	}

	@Override
	public void focusLost( final FocusEvent fe )
	{
//		log.debug("Lost focus repaint");
		repaint();
	}
}
