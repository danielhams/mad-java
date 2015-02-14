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
