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

package uk.co.modularaudio.util.atomicio.windows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.modularaudio.util.atomicio.AtomicFileUtilities;

/**
 * @author dan
 *
 */
public class WindowsAtomicFileUtilities implements AtomicFileUtilities
{
	private final static int BUF_SIZE = 10 * 1024;

	/**
	 * @see uk.co.modularaudio.util.atomicio.AtomicFileUtilities#copyFile(String,
	 *      String)
	 */
	@Override
	public boolean copyFile( final String fromPath, final String toPath ) throws IOException
	{
		boolean retVal = false;
		final byte buffer[] = new byte[BUF_SIZE];

		// Set the temporary directory to be the same directory as the topath
		final File outFile = new File( toPath );
		final String outPath = outFile.getParent();
		final File outDir = new File( outPath );

		// Make sure the temporary file is created in the same directory as the
		// destination file - since that makes the rename atomic.
		final File tmpFile = File.createTempFile( "uafu", null, outDir );

		// Now copy the from to the tmpfile
		final File inFile = new File( fromPath );
		final FileInputStream fis = new FileInputStream( inFile );
		final FileOutputStream fos = new FileOutputStream( tmpFile );

		int numRead = 0;

		while ((numRead = fis.read( buffer )) != -1)
		{
			fos.write( buffer, 0, numRead );
		}
		fos.close();
		fis.close();

		// Now move this to the new filename
		retVal = this.moveFile( tmpFile.getAbsolutePath(), toPath );
		return (retVal);
	}

	/**
	 * @see uk.co.modularaudio.util.atomicio.AtomicFileUtilities#moveFile(String,
	 *      String)
	 */
	@Override
	public boolean moveFile( final String fromPath, final String toPath ) throws IOException
	{
		boolean retVal = false;
		final File fromFile = new File( fromPath );
		if (!fromFile.exists())
		{
			throw new IOException( "File does not exist." );
		}

		if (!fromFile.isFile())
		{
			throw new IOException( "Path specified is not a file." );
		}

		if (!fromFile.renameTo( new File( toPath ) ))
		{
			throw new IOException( "Unable to move file " + fromPath + " to " + toPath );
		}
		else
		{
			retVal = true;
		}
		return (retVal);
	}

}
