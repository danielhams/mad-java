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

package uk.co.modularaudio.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class FileUtils
{

	private static final Log log = LogFactory.getLog(FileUtils.class);

	/**
	 * A function to recursively make a directory. Useful when you don't know if the path has elements that do not exist.
	 * @author dan
	 * @param path
	 * @throws IOException
	 */
	public static void recursiveMakeDir(final String path)
			throws IOException
	{
		// Basically, check if our parent dir exists, if it doesn't, then call recursiveMakeDir on it, then create
		// ourselves.]
		final char pathSeparator = File.separatorChar;

		final int lastPathSepInstance = path.lastIndexOf(pathSeparator);

		// We also need to know if its the first
		final int firstPathSepInstance = path.indexOf(pathSeparator);

		if (firstPathSepInstance == lastPathSepInstance )
		{
			if( path.charAt( 0 ) != File.separatorChar )
			{
				// We have a root directory that is relative - check it exists
				final File rootPathFile = new File( path );
				if( !rootPathFile.exists() )
				{
					rootPathFile.mkdir();
				}
			}
			else
			{
				// This is the root directory. We don't need to make this - just return
				return;
			}
		}
		else
		{
			final String parentPath = path.substring(0, lastPathSepInstance);
			final File parentDir = new File(parentPath);
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
				FileUtils.recursiveMakeDir(parentPath);
			}

			final File realDir = new File(path);
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
					final String message = "Unable to create directory: " + realDir;
					throw new IOException(message);
				}
			}
			return;
		}
	}

	public static void writeUTF8( final String filePath, final String content ) throws IOException
	{
		Files.write( FileSystems.getDefault().getPath( filePath ),
				content.getBytes( StandardCharsets.UTF_8 ) );
	}

	public static String basicReadInputStreamUTF8( final InputStream hbmInputStream ) throws IOException
	{
		BufferedReader br = null;
		final StringBuilder out = new StringBuilder();

		try
		{
			br = new BufferedReader( new InputStreamReader( hbmInputStream, "UTF-8" ) );
			String line;

			while( (line = br.readLine()) != null )
			{
				out.append( line );
				out.append( '\n' );
			}
		}
		finally
		{
			try
			{
				br.close();
			}
			catch( final Exception e )
			{
				log.error(e);
			}
		}

		return out.toString();
	}

	public static String basicReadFileUTF8( final String headerFilename ) throws IOException
	{
		return basicReadInputStreamUTF8( new FileInputStream( new File(headerFilename ) ) );
	}

	public static void copyFile( final File sourceFile, final File destFile ) throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try
		{
			source = new FileInputStream( sourceFile ).getChannel();
			destination = new FileOutputStream( destFile ).getChannel();
			destination.transferFrom( source, 0, source.size() );
		}
		finally
		{
			if (source != null)
			{
				source.close();
			}
			if (destination != null)
			{
				destination.close();
			}
		}
	}
}
