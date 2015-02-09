package uk.co.modularaudio.service.guicompfactory.impl.memreduce;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class FixedXTransparentBorder extends JPanel
{
	private static final long serialVersionUID = -721733939455076637L;
//	private static Log log = LogFactory.getLog( FixedXTransparentBorder.class.getName() );

	private final BufferedImage bi;

	public FixedXTransparentBorder( final BufferedImage bi )
	{
		setOpaque( true );
		this.bi = bi;

		final Dimension size = new Dimension( bi.getWidth(), 1 );

		this.setSize( size );
		this.setMinimumSize( size );
		this.setPreferredSize( size );
//		log.debug("Set size to " + size.width + "," + size.height );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

//		log.debug("Fixed X is " + width + ", " + height );

		g.drawImage( bi, 0, 0, width, height, null );
	}
}
