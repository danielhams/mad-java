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

package uk.co.modularaudio.util.atomicio;

import java.io.File;

import uk.co.modularaudio.util.atomicio.unix.UnixAtomicFileUtilities;
import uk.co.modularaudio.util.atomicio.windows.WindowsAtomicFileUtilities;
import uk.co.modularaudio.util.os.OperatingSystemIdentifiers;

/**
 * @author dan
 *
 */
public class FileUtilities
{
	public static AtomicFileUtilities getAtomicFileUtilities()
	{
		AtomicFileUtilities retVal = null;

		// Switch based on host type
		final String hostString = System.getProperty( "os.name" );

		if (hostString.equals( OperatingSystemIdentifiers.OS_SOLARIS ))
		{
			retVal = new UnixAtomicFileUtilities( hostString );
		}
		else if (hostString.equals( OperatingSystemIdentifiers.OS_LINUX ))
		{
			retVal = new UnixAtomicFileUtilities( hostString );
		}
		else if (hostString.equals( OperatingSystemIdentifiers.OS_MACOSX))
		{
			retVal = new UnixAtomicFileUtilities( hostString );
		}
		else if (hostString.equals( OperatingSystemIdentifiers.OS_WINDOWSNT )
				|| hostString.equals( OperatingSystemIdentifiers.OS_WINDOWSXP ))
		{
			retVal = new WindowsAtomicFileUtilities();
		}
		else
		{
			throw new RuntimeException( "Unknown OS type: " + hostString ); // NOPMD by dan on 08/07/15 13:35
		}

		return (retVal);
	}

	public static boolean isRelativePath( final String filePath )
	{
		final int filePathLength = filePath.length();
		return !( (filePathLength >= 1 && File.separatorChar == '/' && filePath.charAt(0) == '/')
			||
			(filePathLength >= 2 && File.separatorChar == '\\' && filePath.charAt(1) == ':')
			);
	}
}
