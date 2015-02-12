package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.image.BufferedImage;

public class ContainerImages
{
	public BufferedImage src;
	public BufferedImage aaSrc;
	public BufferedImage ltbi;
	public BufferedImage libi;
	public BufferedImage lbbi;
	public BufferedImage tibi;
	public BufferedImage rtbi;
	public BufferedImage ribi;
	public BufferedImage rbbi;
	public BufferedImage bibi;

	public ContainerImages(
			final BufferedImage src,
			final BufferedImage aaSrc,
			final BufferedImage ltbi,
			final BufferedImage libi,
			final BufferedImage lbbi,
			final BufferedImage tibi,
			final BufferedImage rtbi,
			final BufferedImage ribi,
			final BufferedImage rbbi,
			final BufferedImage bibi
			)
	{
		this.src = src;
		this.aaSrc = aaSrc;
		this.ltbi = ltbi;
		this.libi = libi;
		this.lbbi = lbbi;
		this.tibi = tibi;
		this.rtbi = rtbi;
		this.ribi = ribi;
		this.rbbi = rbbi;
		this.bibi = bibi;
	}

}
