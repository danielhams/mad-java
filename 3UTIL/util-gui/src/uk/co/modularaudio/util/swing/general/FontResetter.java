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

package uk.co.modularaudio.util.swing.general;

import java.util.Enumeration;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class FontResetter
{
	public static void setUIFontFromString( final String fontName, final int fontStyle, final int fontSize )
	{
		final FontUIResource f = new FontUIResource( fontName, fontStyle, fontSize );
		setUIFont( f );
	}

	public static void setUIFont( final javax.swing.plaf.FontUIResource f )
	{
		final Enumeration<Object> keys = UIManager.getDefaults().keys();
		while( keys.hasMoreElements() )
		{
			final Object key = keys.nextElement();
			final Object value = UIManager.get( key );
			if( value != null && value instanceof javax.swing.plaf.FontUIResource )
			{
				UIManager.put( key,  f );
			}
		}
	}
}
