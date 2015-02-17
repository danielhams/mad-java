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

package uk.co.modularaudio.mads.base.xrunner.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.xrunner.ui.XRunnerMadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class XRunnerMadInstance extends MadInstance<XRunnerMadDefinition,XRunnerMadInstance>
{
	private static Log log = LogFactory.getLog( XRunnerMadUiInstance.class.getName() );

	private int millisBackEndPeriod;
	private int nanosBackEndPeriod;

	private boolean doXrun;

	public XRunnerMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final XRunnerMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			final long realNanosBackEndPeriod = timingParameters.getNanosPerBackEndPeriod();
			final long xrunMinimumTime = realNanosBackEndPeriod * 1;
			millisBackEndPeriod = (int)(xrunMinimumTime / 1000000);
			nanosBackEndPeriod = (int)(xrunMinimumTime % 1000000);
		}
		catch (final Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , int frameOffset , final int numFrames  )
	{
		final boolean inWaveConnected = channelConnectedFlags.get( XRunnerMadDefinition.CONSUMER_IN_WAVE );

		final boolean outWaveConnected = channelConnectedFlags.get( XRunnerMadDefinition.PRODUCER_OUT_WAVE );

		if( outWaveConnected && inWaveConnected )
		{
			if( doXrun )
			{
				if( log.isDebugEnabled() )
				{
					log.debug("About to sleep for " + millisBackEndPeriod + ", " + nanosBackEndPeriod );
				}
				long timeBefore, timeAfter;
				timeBefore = System.nanoTime();
				try
				{
					Thread.sleep( millisBackEndPeriod, nanosBackEndPeriod );
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
				doXrun = false;
				timeAfter = System.nanoTime();
				final long timeSlept = timeAfter - timeBefore;
				if( log.isDebugEnabled() )
				{
					log.debug("Did sleep for " + timeSlept + " nanos");
				}
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void receiveDoXrun()
	{
		doXrun = true;
	}
}
