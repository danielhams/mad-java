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

package uk.co.modularaudio.mads.base.dctrap.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.DcTrapFilter;
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

public class DCTrapMadInstance extends MadInstance<DCTrapMadDefinition,DCTrapMadInstance>
{
//	private static Log log = LogFactory.getLog( DCTrapMadInstance.class.getName() );

	private int sampleRate = -1;

	private DcTrapFilter dcFilter;

	public DCTrapMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			DCTrapMadDefinition definition,
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
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			dcFilter = new DcTrapFilter( sampleRate );
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
		boolean inWaveConnected = channelConnectedFlags.get( DCTrapMadDefinition.CONSUMER_IN_WAVE );
		MadChannelBuffer inWaveCb = channelBuffers[ DCTrapMadDefinition.CONSUMER_IN_WAVE ];
		float[] inWaveFloats = (inWaveConnected ? inWaveCb.floatBuffer : null );

		boolean outWaveConnected = channelConnectedFlags.get( DCTrapMadDefinition.PRODUCER_OUT_WAVE );
		MadChannelBuffer outWaveCb = channelBuffers[ DCTrapMadDefinition.PRODUCER_OUT_WAVE ];
		float[] outWaveFloats = (outWaveConnected ? outWaveCb.floatBuffer : null );

		// Now mix them together with the precomputed amps
		if( outWaveConnected )
		{
			if( inWaveConnected )
			{
				System.arraycopy(inWaveFloats, 0, outWaveFloats, 0, numFrames );
				dcFilter.filter( outWaveFloats, 0,  numFrames );
			}
			else
			{
				Arrays.fill( outWaveFloats, 0.0f );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
