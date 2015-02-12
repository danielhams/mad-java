package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FixedSizeTransparentCorner extends JPanel
{
	private static final long serialVersionUID = 2547428763384640114L;

	private static Log log = LogFactory.getLog( FixedSizeTransparentCorner.class.getName() );

	private final BufferedImage bi;

	public FixedSizeTransparentCorner( final BufferedImage bi )
	{
		setOpaque( false );
		this.bi = bi;

		final Dimension size = new Dimension( bi.getWidth(), bi.getHeight() );

		this.setSize( size );
		this.setMinimumSize( size );
		this.setPreferredSize( size );

		log.debug("Set fixed size corner size to " + size.toString() );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

		log.debug("FixedCorner size is: " + width + ", " + height);

		g.setColor( Color.GREEN );
		g.fillRect( 0, 0, width, height );

		g.drawImage( bi, 0, 0, null );
	}

}
