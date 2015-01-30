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

package uk.co.modularaudio.service.gui.plugs;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GuiPlugImageCache
{
	private final Map<String, BufferedImage> idToPlugImage = new HashMap<String, BufferedImage>();

	public BufferedImage fetchOrCreatePlugImage( final String id, final int width, final int height, final GuiPlugImageCreator plugImageCreator )
	{
		BufferedImage retVal = idToPlugImage.get( id );

		if( retVal == null )
		{
			retVal = plugImageCreator.createPlugImage( width, height );
			idToPlugImage.put( id, retVal );
		}

		return retVal;
	}

}
