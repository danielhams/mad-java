package uk.co.modularaudio.mads.base.notehistogram.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class NoteReceivedLight extends JPanel
	implements NoteReceivedListener
{
	private static final long serialVersionUID = 8276674761933079784L;

//	private static Log log = LogFactory.getLog( NoteReceivedLight.class.getName() );

	private final int lampLitFrames;

	private boolean shouldBeLit;

	private boolean isLit;
	private long lastLightTime;

	private final static Color LIT_COLOR = LWTCControlConstants.ROTARY_VIEW_INDICATOR_COLOR;
	private final static Color UNLIT_COLOR = LIT_COLOR.darker().darker().darker();

	public NoteReceivedLight()
	{
		setMinimumSize( new Dimension(14,14) );
		lampLitFrames = 8000;
	}

	@Override
	public void receivedNote()
	{
//		log.debug("Setting shouldBeLit to true");
		shouldBeLit = true;
	}

	public void doDisplayProcessing( final long currentGuiTime )
	{
		boolean doRepaint = false;

		if( shouldBeLit )
		{
//			log.debug("Should be lit");
			lastLightTime = currentGuiTime;
			shouldBeLit = false;
			if( !isLit )
			{
//				log.debug("Isn't currently lit");
				isLit = true;
				doRepaint = true;
			}
		}

		final long diff = currentGuiTime - lastLightTime;

		if( isLit && diff > lampLitFrames )
		{
//			log.debug("Is lit and expired.");
			isLit = false;
			doRepaint = true;
		}

		if( doRepaint )
		{
//			log.debug("Doing a repaint");
			repaint();
		}
	}

	@Override
	public void paint( final Graphics g )
	{
		if( isLit )
		{
			g.setColor( LIT_COLOR );
		}
		else
		{
			g.setColor( UNLIT_COLOR );
		}
		g.fillRect( 0, 0, getWidth(), getHeight() );
	}
}
