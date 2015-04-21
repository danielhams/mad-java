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

package uk.co.modularaudio.util.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.os.OperatingSystemIdentifiers;
import uk.co.modularaudio.util.pooling.forkexec.ChildProcessExecutor;

public class ThreadUtils
{
	private static Log log = LogFactory.getLog( ThreadUtils.class.getName() );
	
	public enum MAThreadPriority
	{
		REALTIME,
		REALTIME_SUPPORT,
		APPLICATION,
		BACKGROUND,
		IDLE
	};

	private static boolean platformSupportsRenice = false;

	static
	{
		// Switch based on host type
		String hostString = System.getProperty("os.name");
		
		if(hostString.equals(OperatingSystemIdentifiers.OS_SOLARIS) ||
				hostString.equals(OperatingSystemIdentifiers.OS_LINUX))
		{
			platformSupportsRenice = true;
		}
		else
		{
			platformSupportsRenice = false;
		}
	};

	public static void setCurrentThreadPriority( MAThreadPriority priority )
		throws DatastoreException
	{
		if( !platformSupportsRenice )
		{
			log.warn("Platform doesn't support renice.");
			return;
		}
		int niceValue;
		switch( priority )
		{
			case REALTIME:
			{
				niceValue = -10;
				break;
			}
			case REALTIME_SUPPORT:
			{
				niceValue = -5;
				break;
			}
			case APPLICATION:
			{
				niceValue = -1;
				break;
			}
			case BACKGROUND:
			{
				niceValue = +1;
				break;
			}
			case IDLE:
			{
				niceValue = +10;
				break;
			}
			default:
			{
				throw new DatastoreException("Unknown priority attempting to set current thread priority: " + priority );
			}
		}

		try
		{
			int nativeThreadID = GetThreadID.get_tid();
			String[] cmdArray = new String[] { "/usr/bin/renice", "-n", "" + niceValue, "-p", "" + nativeThreadID };
			ChildProcessExecutor cpe = new ChildProcessExecutor( cmdArray );
			cpe.close();
			int retVal = cpe.getExitValue();
			if( retVal != 0 )
			{
				throw new DatastoreException("Failed return code from renice child process: " + retVal );
			}
		}
		catch ( Exception e )
		{
			String msg = "Exception caught calling renice: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}
}
