package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractMadButton extends JLabel implements FocusListener
{
	private static final long serialVersionUID = -7622401208667882019L;

	private static Log log = LogFactory.getLog( AbstractMadButton.class.getName() );

	protected enum ButtonState
	{
		OUT_NO_MOUSE,
		OUT_MOUSE,
		IN_NO_MOUSE,
		IN_MOUSE
	};

	private static final int OUTLINE_ARC_WIDTH = 6;
	private static final int OUTLINE_ARC_HEIGHT = OUTLINE_ARC_WIDTH;

	private static final int INSIDE_ARC_WIDTH = 1;
	private static final int INSIDE_ARC_HEIGHT = OUTLINE_ARC_WIDTH;

	protected final MadButtonColours colours;

	protected ButtonState pushedState = ButtonState.OUT_NO_MOUSE;

	public AbstractMadButton( final MadButtonColours colours )
	{
		this.colours = colours;
		setOpaque( false );

		this.setForeground( colours.getForegroundTextUnselected() );
		this.setFont( MadControlConstants.RACK_FONT );

		this.setText("Kill A");
		this.setHorizontalAlignment( SwingConstants.CENTER );
		this.setVerticalAlignment( SwingConstants.CENTER );

		setFocusable( true );

		this.addMouseListener( getMouseListener() );
		this.addFocusListener( this );
	}

	protected abstract MouseListener getMouseListener();

	private final static void paintButton( final Graphics2D g2d,
			final Color outlineColour,
			final GradientPaint gp,
			final Color highlight,
			final int width,
			final int height )
	{
		// outline
		g2d.setColor( outlineColour );
		g2d.fillRoundRect( 0, 0, width, height, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		// Background with gradient
		g2d.setPaint( gp );
		g2d.fillRoundRect( 1, 1, width-2, height-2, INSIDE_ARC_WIDTH, INSIDE_ARC_HEIGHT );

		// Highlight

		g2d.setColor( highlight );
		g2d.drawLine( 2, 1, width-3, 1 );
	}


	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		GradientPaint bgGrad;
		Color hilight;
		Color foreground;

		log.debug("Paint called we are in state " + pushedState.toString() );

		switch( pushedState )
		{
			case OUT_MOUSE:
			{
				bgGrad = new GradientPaint( 0, 0,
						colours.getNoMouseUnselectedGradStart().brighter(),
						0, height,
						colours.getNoMouseUnselectedGradEnd().brighter() );
				hilight = colours.getButtonHighlightUnselected().brighter();
				foreground = colours.getForegroundTextUnselected();
				break;
			}
			case IN_NO_MOUSE:
			{
				bgGrad = new GradientPaint( 0, 0,
						colours.getNoMouseSelectedGradStart().brighter(),
						0, height,
						colours.getNoMouseSelectedGradEnd().brighter() );
				hilight = colours.getNoMouseSelectedGradStart().brighter();
				foreground = colours.getForegroundTextSelected();
				break;
			}
			case IN_MOUSE:
			{
				bgGrad = new GradientPaint( 0, 0,
						colours.getNoMouseSelectedGradStart(),
						0, height,
						colours.getNoMouseSelectedGradEnd() );
				hilight = colours.getNoMouseSelectedGradStart();
				foreground = colours.getForegroundTextSelected();
				break;
			}
			case OUT_NO_MOUSE:
			default:
			{
				bgGrad = new GradientPaint( 0, 0,
						colours.getNoMouseUnselectedGradStart(),
						0, height,
						colours.getNoMouseUnselectedGradEnd() );
				hilight = colours.getButtonHighlightUnselected();
				foreground = colours.getForegroundTextUnselected();
				break;
			}
		}

		paintButton( g2d,
				colours.getControlOutline(),
				bgGrad,
				hilight,
				width, height );

		setForeground( foreground );

		super.paint( g );

		// highlight

		if( hasFocus() )
		{
			g2d.setColor( colours.getButtonFocusColour() );
			g2d.drawRect( 5, 5, width-10, height-10 );
		}
	}

	@Override
	public void focusGained( final FocusEvent arg0 )
	{
		repaint();
	}

	@Override
	public void focusLost( final FocusEvent arg0 )
	{
		repaint();
	}
}
