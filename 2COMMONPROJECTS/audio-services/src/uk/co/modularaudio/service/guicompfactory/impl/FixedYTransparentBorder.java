package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class FixedYTransparentBorder extends JPanel
{
	private static final long serialVersionUID = -3847210402134143546L;

//	private static Log log = LogFactory.getLog( FixedYTransparentBorder.class.getName() );

	private final BufferedImage bi;

	public FixedYTransparentBorder( final BufferedImage bi )
	{
		setOpaque( true );
		this.bi = bi;

		final Dimension size = new Dimension( 1, bi.getHeight() );

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

//		log.debug("Fixed Y is " + width + ", " + height );

		g.drawImage( bi, 0, 0, width, height, null );
	}
}
