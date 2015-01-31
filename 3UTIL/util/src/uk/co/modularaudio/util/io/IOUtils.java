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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class IOUtils
{
	private static Log log = LogFactory.getLog(IOUtils.class);

	public static void writeUTF8( final String filePath, final String content ) throws IOException
	{
		Files.write( FileSystems.getDefault().getPath( filePath ),
				content.getBytes( StandardCharsets.UTF_8 ) );
	}

	public static String basicReadInputStreamUTF8( final InputStream inputStream ) throws IOException
	{
		return basicReadInputStreamUTF8( inputStream, true );
	}
	public static String basicReadInputStreamUTF8( final InputStream inputStream, final boolean trailingNewline ) throws IOException
	{
		BufferedReader br = null;
		final StringBuilder out = new StringBuilder();

		boolean doneFirst = false;

		try
		{
			br = new BufferedReader( new InputStreamReader( inputStream, "UTF-8" ) );
			String line;

			while( (line = br.readLine()) != null )
			{
				if( doneFirst )
				{
					out.append( '\n' );
				}
				out.append( line );
				doneFirst = true;
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
		if( trailingNewline && doneFirst )
		{
			out.append( '\n' );
		}

		return out.toString();
	}

	public static String basicReadFileUTF8( final String headerFilename ) throws IOException
	{
		return basicReadFileUTF8( headerFilename, true );
	}

	public static String basicReadFileUTF8( final String headerFilename, final boolean trailingNewline ) throws IOException
	{
		return basicReadInputStreamUTF8( new FileInputStream( new File(headerFilename ) ), trailingNewline );
	}
}
