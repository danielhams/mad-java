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

package uk.co.modularaudio.libmpg123wrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LibMpg123WrapperLoader
{
	private static Log log = LogFactory.getLog( LibMpg123WrapperLoader.class.getName() );

	static
	{
		log.debug("Loading libmpg123 wrapper library");
		System.loadLibrary( "mpg123_wrap" );
	}

	public static final void loadIt()
	{
		// Do nothing
	}
}
