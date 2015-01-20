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

package uk.co.modularaudio.util.pooling.forkexec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dan
 *
 */
public class MultishotChildProcess
{
	private static Log log = LogFactory.getLog( MultishotChildProcess.class.getName() );

	String lineSeparator;
	ChildProcessExecutor cpe;
	OutputStream toProcess;
	InputStream fromProcess;
	InputStream errorProcess;
	String endOfDataMarker;

	public MultishotChildProcess( String[] cmd, String endOfDataMarker ) throws IOException
	{
		log.debug( "Creating cpe" );
		cpe = new ChildProcessExecutor( cmd );
		toProcess = cpe.getOutputStream();
		fromProcess = cpe.getInputStream();
		errorProcess = cpe.getErrorStream();
		log.debug( "Done obtaining streams." );
		this.endOfDataMarker = endOfDataMarker;
		lineSeparator = System.getProperty( "line.separator" );
		log.debug( "Line separator set to '" + lineSeparator + "'" );
	}

	public StringBuilder passAndReturn( StringBuilder dataToProcess ) throws IOException
	{
		log.debug( "About to write: " + dataToProcess + " to the subprocess." );
		StringBuilder retVal = new StringBuilder();
		// Write the line and flush
		toProcess.write( dataToProcess.toString().getBytes() );
		toProcess.write( lineSeparator.getBytes() );
		toProcess.flush();
		log.debug( "Wrote input." );
		boolean hadError = false, done = false;
		BufferedReader reader = new BufferedReader( new InputStreamReader( fromProcess ) );
		while (!hadError && !done)
		{
			try
			{
				log.debug( "Attempting to read a line" );
				String line = reader.readLine();
				if (line == null)
				{
					hadError = true;
				}
				else
				{
					if (line.equals( endOfDataMarker ))
					{
						done = true;
					}
					else
					{
						retVal.append( line );
						retVal.append( lineSeparator );
						log.debug( "Read a line: " + line );
					}
				}
			}
			catch (Exception e)
			{
				hadError = true;
				throw new IOException( e.toString() );
			}
		}

		return (retVal);
	}

	public void close() throws IOException
	{
		cpe.close();
	}
}
