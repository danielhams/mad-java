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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dan
 *
 */
public class ChildProcessExecutor
{
	private static final long WAIT_FOR_DEATH_SLEEP_MILLIS = 500;

	private static Log log = LogFactory.getLog( ChildProcessExecutor.class.getName() );

	private Process process;
	private InputStream inputStream;
	private OutputStream outputStream;
	private InputStream errorStream;

	private int exitValue;

	public ChildProcessExecutor( final String[] cmdArray ) throws IOException
	{
		// log.debug("CPE Constructor called.");
		final Runtime runtime = Runtime.getRuntime();
		boolean wasError = false;
		try
		{
			this.process = runtime.exec( cmdArray );
		}
		catch (final SecurityException se)
		{
			log.warn( "Caught se" );
		}
		catch (final NullPointerException npe)
		{
			log.warn( "Caught npe" );
		}
		catch (final IOException ioe)
		{
			log.warn( "Caught ioe" );
		}
		catch (final Exception e)
		{
			log.warn( "Caught e" );
			wasError = true;
		}

		if (this.process == null)
		{
			wasError = true;
		}

		if (wasError)
		{
			throw new IOException( "Unable to create child process " + cmdArray[0] );

		}
		inputStream = process.getInputStream();
		outputStream = process.getOutputStream();
		errorStream = process.getErrorStream();
	}

	/**
	 * Returns the errorStream.
	 *
	 * @return InputStream
	 */
	public InputStream getErrorStream()
	{
		return errorStream;
	}

	/**
	 * Returns the inputStream.
	 *
	 * @return InputStream
	 */
	public InputStream getInputStream()
	{
		return inputStream;
	}

	/**
	 * Returns the outputStream.
	 *
	 * @return OutputStream
	 */
	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	public void close() throws IOException
	{
		boolean finished = false;
		boolean wasError = false;

		while (!finished)
		{
			try
			{
				// Check for the exit value
				exitValue = this.process.exitValue();
				if (exitValue != 0)
				{
					wasError = true;
				}
				finished = true;
			}
			catch (final IllegalThreadStateException itse)
			{
				try
				{
					Thread.sleep( WAIT_FOR_DEATH_SLEEP_MILLIS );
				}
				catch (final InterruptedException ie)
				{
					final String msg = "Caught interruption in waiting for sub-process death: " + ie.toString();
					log.warn( msg, ie );
				}
			}
		}
		try
		{
			inputStream.close();
		}
		catch (final Exception e)
		{
		}
		;
		try
		{
			outputStream.close();
		}
		catch (final Exception e)
		{
		}
		;
		try
		{
			errorStream.close();
		}
		catch (final Exception e)
		{
		}
		;
		try
		{
			process.destroy();
		}
		catch (final Exception e)
		{
			throw new IOException( "Error destroying child process: " + e.toString() );
		}
		inputStream = null;
		outputStream = null;
		errorStream = null;
		if (wasError)
		{
			throw new IOException( "Bad return code from exit() of the sub-process: " + exitValue );
		}
	}

	public void destroyProcess()
	{
		try
		{
			if (process != null)
			{
				process.destroy();
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught destroying process: " + e.toString();
			log.error( msg, e );
		}
	}

	public int getExitValue()
	{
		return exitValue;
	}

}
