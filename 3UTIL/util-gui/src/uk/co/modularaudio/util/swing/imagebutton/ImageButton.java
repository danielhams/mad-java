package uk.co.modularaudio.util.swing.imagebutton;

import java.awt.GradientPaint;
import java.awt.Graphics2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCButtonColours;
import uk.co.modularaudio.util.swing.lwtc.LWTCButtonStateColours;

public abstract class ImageButton extends LWTCButton
{
	private static final long serialVersionUID = -2370440248293309619L;

	private final static Log LOG = LogFactory.getLog( ImageButton.class );

	public interface ImageButtonProducer
	{
		void draw( Graphics2D g2d );
	}

	private final ImageButtonProducer producer;

	public ImageButton( final LWTCButtonColours colours,
			final String text,
			final boolean isImmediate,
			final ImageButtonProducer producer )
	{
		super( colours, text, isImmediate );
		this.producer = producer;
	}

	@Override
	public void receiveClick()
	{
	}

	@Override
	protected void paintButton( final Graphics2D g2d,
			final LWTCButtonStateColours stateColours,
			final GradientPaint gp,
			final int width,
			final int height )
	{
		super.paintButton( g2d, stateColours, gp, width, height );
		producer.draw( g2d );
	}
}
