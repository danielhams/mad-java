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

package test.uk.co.modularaudio.util.audio.mad.audioenginestate;

import uk.co.modularaudio.util.exception.DatastoreException;

public class BadStateTransitionException extends DatastoreException
{
	private static final long serialVersionUID = -1383729715466584548L;

	public BadStateTransitionException( String msg )
	{
		super( msg );
	}

	public BadStateTransitionException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
