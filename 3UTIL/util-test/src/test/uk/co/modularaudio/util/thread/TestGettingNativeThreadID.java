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

package test.uk.co.modularaudio.util.thread;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.thread.GetThreadID;
import uk.co.modularaudio.util.thread.ThreadUtils;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public class TestGettingNativeThreadID extends TestCase
{
	private static Log log = LogFactory.getLog( TestGettingNativeThreadID.class.getName() );

	public void testGettingOne() throws Exception
	{
		log.debug("Current directory is " + System.getProperty( "user.dir" ) );
		try
		{
			long nativeId = GetThreadID.get_tid();
			log.debug("Got back the ID of " + nativeId );
		}
		catch( Throwable t )
		{
			String msg = "Throwable caught: " + t.toString();
			log.error( msg, t );
		}
		
		ThreadUtils.setCurrentThreadPriority( MAThreadPriority.REALTIME );
	}
}
