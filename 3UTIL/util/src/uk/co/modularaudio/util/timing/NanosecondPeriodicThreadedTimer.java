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

package uk.co.modularaudio.util.timing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.thread.AbstractInterruptableThread;
import uk.co.modularaudio.util.thread.InterruptableJob;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public class NanosecondPeriodicThreadedTimer
{
	private static Log log = LogFactory.getLog( NanosecondPeriodicThreadedTimer.class.getName() );

	private final static int NUM_VALUES_IN_AVERAGE = 1000;
	private final static int RATIO_FOR_EXISTING = NUM_VALUES_IN_AVERAGE - 1;

	private class TimerThread extends AbstractInterruptableThread
	{
		private long expectedWakeupNanos = 0;
		private long adjustmentNanos = 0;

		private long averageAdjustedOvershoot = 0;
		private long averageRegularOvershoot = 0;

		private long numCalls = 0;
		private long numUndershoots = 0;
		private long numAdjustedOvershoots = 0;
		private long numRegularOvershoots = 0;

		public TimerThread( final MAThreadPriority threadPriority )
		{
			super(threadPriority);
		}

		@Override
		protected void doJob() throws Exception
		{
			expectedWakeupNanos = System.nanoTime();
			if( log.isDebugEnabled() )
			{
				log.debug("Set initial wakeupnanos to " + expectedWakeupNanos );
			}
			boolean localShouldHalt = shouldHalt;

			while( !localShouldHalt )
			{
				long wakeupTime = System.nanoTime();
				while( wakeupTime < (expectedWakeupNanos - 2000) )
				{
					wakeupTime = System.nanoTime();
				}

				timerJob.run();
				numCalls++;
				// Positive -> overshoot, negative, undershoot
				final long wakeupError = wakeupTime - expectedWakeupNanos;
				// Create an average error
//				log.debug( (wakeupError >= 0 ? "Overshoot" : "Undershoot" ) + "Wakeup error of " +
//						MathFormatter.fastFloatPrint((wakeupError / 1000000.0f), 8, true ) +
//						" ms");
				if( wakeupError > 0 )
				{
				}
				else
				{
				}
				if( wakeupError > 0 && wakeupError > adjustmentNanos )
				{
					adjustmentNanos = wakeupError;
//					log.debug("Harsh adjustment");
					averageAdjustedOvershoot = (averageAdjustedOvershoot + wakeupError) / 2;
					numAdjustedOvershoots++;
				}
				else
				{
					if( wakeupError >= 0 )
					{
						averageRegularOvershoot = (averageRegularOvershoot + wakeupError) / 2;
						numRegularOvershoots++;
					}
					else
					{
						numUndershoots++;
					}
					adjustmentNanos = ((adjustmentNanos * RATIO_FOR_EXISTING) + wakeupError) / NUM_VALUES_IN_AVERAGE;
				}
//				log.debug("Average adjustement of " + adjustmentNanos);
				expectedWakeupNanos += nanosBetweenCalls;
				final long adjustedWakeupNanos = expectedWakeupNanos - adjustmentNanos;
				final long fullNanosToSleep = adjustedWakeupNanos - System.nanoTime();
				final int nanosToSleep = (int)(fullNanosToSleep % 1000000);
				final long millisToSleep = fullNanosToSleep / 1000000;
				if( nanosToSleep >= 0 && millisToSleep >= 0 )
				{
					Thread.sleep( millisToSleep, nanosToSleep );
//					log.debug("Slept for " + millisToSleep  +" ms and " + nanosToSleep + " ns");
				}
				else
				{
					log.debug("Skipped sleep, was negative");
				}
				localShouldHalt = shouldHalt;
			}
			if( log.isDebugEnabled() )
			{
				log.debug("Final adjustment nanos is " + adjustmentNanos + " which is " +
						MathFormatter.fastFloatPrint( (adjustmentNanos / 1000000.0f), 8, true) +
						" ms");
				log.debug("And average adjusted overshoot is " + averageAdjustedOvershoot + " which is " +
						MathFormatter.fastFloatPrint( (averageAdjustedOvershoot / 1000000.0f), 8, true) +
						" ms");
				log.debug("And average regular overshoot is " + averageRegularOvershoot + " which is " +
						MathFormatter.fastFloatPrint( (averageRegularOvershoot / 1000000.0f), 8, true) +
						" ms");
				log.debug("Total calls: " + numCalls );
				log.debug("Num undershoots: " + numUndershoots );
				log.debug("Num adjusted overshoots: " + numAdjustedOvershoots );
				log.debug("Num regular overshoots: " + numRegularOvershoots );
			}
		}
	};

	private final long nanosBetweenCalls;
	private final MAThreadPriority threadPriority;
	private final InterruptableJob timerJob;

	private TimerThread timerThread = null;

	public NanosecondPeriodicThreadedTimer( final long nanosBetweenCalls,
			final MAThreadPriority threadPriority,
			final InterruptableJob timerJob )
	{
		this.nanosBetweenCalls = nanosBetweenCalls;
		this.threadPriority = threadPriority;
		this.timerJob = timerJob;
		if( log.isDebugEnabled() )
		{
			log.debug("Nanos between calls is " + nanosBetweenCalls + " which is " +
					MathFormatter.fastFloatPrint( (nanosBetweenCalls / 1000000.0f), 8, true ) +
					" ms");
		}
	}

	public void start()
	{
		if( timerThread != null )
		{
			log.error("Attempted to start timer thread but one is already running!");
		}
		else
		{
			timerThread = new TimerThread(threadPriority);
			timerThread.start();
		}
	}

	public void stop()
	{
		if( timerThread == null )
		{
			log.error("Attempted to stop timer thread without one running!");
		}
		else
		{
			timerThread.halt();
			try
			{
				timerThread.join();
			}
			catch (final InterruptedException e)
			{
				if( log.isErrorEnabled() )
				{
					log.error("Failed during timer thread join: " + e.toString(), e );
				}
			}
			timerThread = null;
		}
	}
}
