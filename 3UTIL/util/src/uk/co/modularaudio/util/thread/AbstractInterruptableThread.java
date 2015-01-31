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

import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public abstract class AbstractInterruptableThread extends Thread implements InterruptableJob
{
	private static Log log = LogFactory.getLog( AbstractInterruptableThread.class.getName() );

	protected volatile boolean shouldHalt = false; // NOPMD by dan on 31/01/15 16:34

	protected MAThreadPriority threadPriority = null;

	public AbstractInterruptableThread( MAThreadPriority threadPriority )
	{
		this.threadPriority = threadPriority;
	}

	@Override
	public void run()
	{
		try
		{
			ThreadUtils.setCurrentThreadPriority( threadPriority );
		}
		catch( Exception e )
		{
			String msg = "Thread " + getName() + " unable to set priority: " + threadPriority + ": " + e.toString();
			log.error( msg, e );
		}

		boolean localShouldHalt = shouldHalt;;
		while( !localShouldHalt )
		{
			// Do job
			try
			{
				doJob();
			}
			catch( InterruptedException ie )
			{
				// We don't care.
			}
			catch( Throwable t )
			{
				String msg = "Throwable caught during job run: " + t.toString();
				log.error( msg, t );
				shouldHalt = true;
			}
			localShouldHalt = shouldHalt;
		}
	}

	protected abstract void doJob() throws Exception;

	@Override
	public void halt()
	{
		shouldHalt = true;
	}

	@Override
	public void forceHalt()
	{
		this.halt();
		this.interrupt();
	}
}
