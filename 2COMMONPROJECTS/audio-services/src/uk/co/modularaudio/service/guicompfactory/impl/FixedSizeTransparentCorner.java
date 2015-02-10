package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class FixedSizeTransparentCorner extends JPanel
{
	private static final long serialVersionUID = 2547428763384640114L;

	private final BufferedImage bi;

	public FixedSizeTransparentCorner( final BufferedImage bi )
	{
		setOpaque( false );
		this.bi = bi;

		final Dimension size = new Dimension( bi.getWidth(), bi.getHeight() );

		this.setSize( size );
		this.setMinimumSize( size );
		this.setPreferredSize( size );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.drawImage( bi, 0, 0, null );
	}

}
