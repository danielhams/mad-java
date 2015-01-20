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
import java.io.IOException;

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
		throws IOException
	{
		AtomicFileUtilities retVal = null;

		// Switch based on host type
		String hostString = System.getProperty("os.name");

		if(hostString.equals(OperatingSystemIdentifiers.OS_SOLARIS))
		{
			retVal = new UnixAtomicFileUtilities(hostString);
		}
		else if (hostString.equals(OperatingSystemIdentifiers.OS_LINUX))
		{
			retVal = new UnixAtomicFileUtilities(hostString);
		}
		else if (hostString.equals(OperatingSystemIdentifiers.OS_WINDOWSNT) ||
			hostString.equals(OperatingSystemIdentifiers.OS_WINDOWSXP))
		{
			retVal = new WindowsAtomicFileUtilities();
		}
		else
		{
			throw new IOException("Unknown OS type: " + hostString);
		}

		return(retVal);
	}

	public static void recursiveMakeDir(String path)
		throws IOException
	{
		// Basically, check if our parent dir exists, if it doesn't, then call recursiveMakeDir on it, then create ourselves.]
		char pathSeparator = File.separatorChar;

		int lastPathSepInstance = path.lastIndexOf( pathSeparator );

		// We also need to know if its the first
		int firstPathSepInstance = path.indexOf( pathSeparator );

		if (firstPathSepInstance == lastPathSepInstance)
		{
			// This is the root directory. We don't need to make this - just return
			return;
		}
		else
		{
			String parentPath = path.substring(0, lastPathSepInstance);
			File parentDir = new File(parentPath);
			boolean exists = parentDir.exists();
			boolean isDir = parentDir.isDirectory();
			boolean isFile = parentDir.isFile();

			if (exists && isFile)
			{
				throw new IOException("Cannot make directory as " + parentDir + " is a file.");
			}
			else if (exists && isDir)
			{
				// Continue on to make the current directory.
			}
			else if (exists)
			{
				// Is not directory or file, but exists (could be special device, sym link etc.)
			}
			else
			{
				// Does not exist. Attempt to make it.
				FileUtilities.recursiveMakeDir( parentPath );
			}

			File realDir = new File(path);
			exists = realDir.exists();
			isDir = realDir.isDirectory();
			isFile = realDir.isFile();

			if (exists && isFile)
			{
				throw new IOException("Cannot make directory as " + realDir + " is a file.");
			}
			else if (exists && isDir)
			{
				// Already made, just return
				return;
			}
			else
			{
				if (!realDir.mkdir())
				{
					String message = "Unable to create directory: " + realDir;
					throw new IOException(message);
				}
			}
			return;
		}
	}
}
