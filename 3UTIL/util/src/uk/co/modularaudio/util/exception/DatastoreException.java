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

package uk.co.modularaudio.util.exception;


/**
 * <p>An exception use to indicate that an error has occurred that
 * requires any pending transactions to be rolled back and the
 * current operation to be halted.</p>
 *
 * @author dan
 *
 */
public class DatastoreException extends Exception
{
	private static final long serialVersionUID = 5235508062602526306L;

	public DatastoreException( final String message, final Throwable cause )
	{
		super( message, cause );
	}

	public DatastoreException( final String message )
	{
		super( message );
	}

	public DatastoreException( final Throwable cause )
	{
		super( cause );
	}

}
