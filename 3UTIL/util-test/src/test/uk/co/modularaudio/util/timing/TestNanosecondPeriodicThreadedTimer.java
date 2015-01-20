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

package test.uk.co.modularaudio.util.timing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.thread.InterruptableJob;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;
import uk.co.modularaudio.util.timing.NanosecondPeriodicThreadedTimer;

public class TestNanosecondPeriodicThreadedTimer
{
	private static Log log = LogFactory.getLog( TestNanosecondPeriodicThreadedTimer.class.getName() );
	
	class TestJob implements InterruptableJob
	{
		long counter = 0;
		@Override
		public void run()
		{
			long jobRunTime = System.nanoTime();
			log.debug( "TestJob run at " + jobRunTime );
			for( int i =0 ; i < 1000 ; ++i )
			{
				counter += counter + (counter / (i+1));
			}
		}

		@Override
		public void halt()
		{
			// Do nothing
		}

		@Override
		public void forceHalt()
		{
			// Do nothing
		}
	};
	
	private TestJob job;
	private NanosecondPeriodicThreadedTimer timer;
	
	public TestNanosecondPeriodicThreadedTimer()
	{
		job = new TestJob();
		// Every second
//		long nanosPerJob = 1000 * 1000 * 1000;
		// Every 16 ms
		long nanosPerJob = (long)(1000 * 1000 * 16.66666666667);
		timer = new NanosecondPeriodicThreadedTimer( nanosPerJob, MAThreadPriority.APPLICATION,  job );
	}
	
	public void go() throws Exception
	{
		int testSeconds = 5;
		timer.start();
		Thread.sleep( testSeconds * 1000 );
		timer.stop();
		
		Thread.sleep( 1000 );
		
		// Now do it again, since there is a chance hotspot is good now
		timer.start();
		Thread.sleep( testSeconds * 1000 );
		timer.stop();
		
	}

	public static void main(String[] args) throws Exception
	{
		TestNanosecondPeriodicThreadedTimer tester = new TestNanosecondPeriodicThreadedTimer();
		tester.go();
	}

}
