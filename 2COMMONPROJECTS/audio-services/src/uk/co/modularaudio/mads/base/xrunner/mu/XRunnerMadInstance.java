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

	private int millisBackEndPeriod = -1;
	private int nanosBackEndPeriod = -1;
	
	private boolean doXrun = false;
	
	public XRunnerMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			XRunnerMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			long realNanosBackEndPeriod = timingParameters.getNanosPerBackEndPeriod();
			long xrunMinimumTime = realNanosBackEndPeriod * 1;
			millisBackEndPeriod = (int)(xrunMinimumTime / 1000000);
			nanosBackEndPeriod = (int)(xrunMinimumTime % 1000000);
		}
		catch (Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, int numFrames )
	{
		boolean inWaveConnected = channelConnectedFlags.get( XRunnerMadDefinition.CONSUMER_IN_WAVE );
		
		boolean outWaveConnected = channelConnectedFlags.get( XRunnerMadDefinition.PRODUCER_OUT_WAVE );
		
		if( outWaveConnected && inWaveConnected )
		{
			if( doXrun )
			{
				log.debug("About to sleep for " + millisBackEndPeriod + ", " + nanosBackEndPeriod );
				long timeBefore, timeAfter;
				timeBefore = System.nanoTime();
				try
				{
					Thread.sleep( millisBackEndPeriod, nanosBackEndPeriod );
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				doXrun = false;
				timeAfter = System.nanoTime();
				long timeSlept = timeAfter - timeBefore;
				log.debug("Did sleep for " + timeSlept + " nanos");
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void receiveDoXrun()
	{
		doXrun = true;
	}
}
