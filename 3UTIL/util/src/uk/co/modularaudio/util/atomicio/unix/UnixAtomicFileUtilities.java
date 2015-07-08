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

package uk.co.modularaudio.util.atomicio.unix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.atomicio.AtomicFileUtilities;
import uk.co.modularaudio.util.os.OperatingSystemIdentifiers;

/**
 * @author dan
 *
 */
public class UnixAtomicFileUtilities implements AtomicFileUtilities
{
	protected static Log log = LogFactory.getLog( UnixAtomicFileUtilities.class.getName() );

	private static final int BUF_SIZE = 10 * 1024;
	private String mvLocation;

	/**
	 * Constructor for UnixAtomicFileUtilities.
	 */
	public UnixAtomicFileUtilities( final String hostString )
	{
		if (hostString.equals( OperatingSystemIdentifiers.OS_SOLARIS ))
		{
			mvLocation = "/usr/bin/mv";
		}
		else if (hostString.equals( OperatingSystemIdentifiers.OS_LINUX ))
		{
			mvLocation = "/bin/mv";
		}
		else if(hostString.equals( OperatingSystemIdentifiers.OS_MACOSX ))
		{
			mvLocation = "/bin/mv";
		}
		else
		{
			throw new RuntimeException("Unknown host OS: " + hostString ); // NOPMD by dan on 08/07/15 13:35
		}
	}

	/**
	 * <P>
	 * Unix copy implemented by copy to a temporary file in the same directory
	 * then using mv to move to the new name since mv only changes the dir entry
	 * but leaves the inode of the file alone.
	 * </P>
	 * <P>
	 * <B>All paths should be ABSOLUTE and working that out is left to the
	 * calling code.</B>
	 * </P>
	 *
	 * @see uk.co.modularaudio.util.atomicio.AtomicFileUtilities#copyFile(String,
	 *      String)
	 */
	@Override
	public boolean copyFile( final String fromPath, final String toPath ) throws IOException
	{
		boolean retVal = false;
		final byte buffer[] = new byte[BUF_SIZE];
		// Unix copy implemented using copy to temp file in same directory
		// then move to the new name (using mv - since it _is_ atomic).

		// Set the temporary directory to be the same directory as the topath
		final File outFile = new File( toPath );
		final String outPath = outFile.getParent();
		final File outDir = new File( outPath );

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
	 * <P>
	 * Use mv to move the file to its new name. On Unix MV is guaranteed to be
	 * atomic (on the same partition).
	 * </P>
	 * <P>
	 * <B>All paths should be ABSOLUTE, and must be on the same partition - and
	 * working that out is left to the calling code.</B>
	 * </P>
	 *
	 * @see uk.co.modularaudio.util.atomicio.AtomicFileUtilities#moveFile(String,
	 *      String)
	 */
	@Override
	public boolean moveFile( final String fromPath, final String toPath ) throws IOException
	{
		boolean retVal = false;

		final File fromFile = new File( fromPath );
		final File toFile = new File( toPath );
		if (fromFile.getParentFile().getPath().equals( toFile.getParentFile().getPath() ))
		{
			// Do java rename which is atomic if parent dirs are the same
			retVal = fromFile.renameTo( toFile );
		}
		else
		{

			if (fromPath == null || fromPath.equals( "" ))
			{
				final String msg = "AtomicFileUtilities does not support a NULL fromPath for copy.";
				log.error( msg );
				throw new IOException( msg );
			}

			if (toPath == null || toPath.equals( "" ))
			{
				final String msg = "AtomicFileUtilities does not support a NULL toPath for copy.";
				log.error( msg );
				throw new IOException( msg );
			}

			final String cmdArray[] = new String[3];
			cmdArray[0] = mvLocation;
			cmdArray[1] = fromPath;
			cmdArray[2] = toPath;

			// Simple system call. Or at least it would be simple, if we knew
			// where
			// the binary lives _all_ the time.
			final Runtime runtime = Runtime.getRuntime();
			final Process executor = runtime.exec( cmdArray );
			try
			{
				executor.waitFor();
				final int exitValue = executor.exitValue();

				if (exitValue != 0)
				{
					throw new IOException( "Bad return value from UNIX " + mvLocation + ": " + exitValue );
				}
				else
				{
					retVal = true;
				}
			}
			catch (final InterruptedException ie)
			{
				final String errMsg = "Error waiting for mv child process to complete: " + ie.toString();
				log.error( errMsg );
				throw new IOException( errMsg );
			}
		}
		return retVal;
	}

}
