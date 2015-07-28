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

package uk.co.modularaudio.mads.base.specampgen.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.buffer.BackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SpectralAmpGenMadInstance<D extends SpectralAmpGenMadDefinition<D, I>,
	I extends SpectralAmpGenMadInstance<D,I>>
	extends MadInstance<D,I>
{
	private static Log log = LogFactory.getLog( SpectralAmpGenMadInstance.class.getName() );

	protected boolean active;

	protected int maxRingBufferingInSamples;

	private BackendToFrontendDataRingBuffer dataRingBuffer;

	private int numSamplePerFrontEndPeriod;

	public SpectralAmpGenMadInstance( final String instanceName,
			final D definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		dataRingBuffer = new BackendToFrontendDataRingBuffer( 1 );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			final int sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			final long nanosFeBuffering = timingParameters.getNanosPerFrontEndPeriod() * 2;
			final long nanosBeBuffering = timingParameters.getNanosPerBackEndPeriod() * 2;
			final long nanosForBuffering = nanosFeBuffering + nanosBeBuffering;

			maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 16 ) +
				AudioTimingUtils.getNumSamplesForNanosAtSampleRate( sampleRate, nanosForBuffering );

			dataRingBuffer = new BackendToFrontendDataRingBuffer( maxRingBufferingInSamples );
			dataRingBuffer.backEndClearNumSamplesQueued();

			numSamplePerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
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
			final MadTimingParameters timingParameters,
			final long periodStartTimestamp,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final boolean inConnected = channelConnectedFlags.get( SpectralAmpGenMadDefinition.CONSUMER_IN);

		if( active )
		{
			final MadChannelBuffer inCb = channelBuffers[ SpectralAmpGenMadDefinition.CONSUMER_IN ];
			final float[] inFloats = inCb.floatBuffer;

			try
			{
				if( inConnected )
				{
					int curSampleIndex = 0;
					while( curSampleIndex < numFrames )
					{
						final long timestampForIndexUpdate = periodStartTimestamp + curSampleIndex;

						if( dataRingBuffer.backEndGetNumSamplesQueued() >= numSamplePerFrontEndPeriod )
						{
							queueWriteIndexUpdate( tempQueueEntryStorage,
								0,
								dataRingBuffer.getWritePosition(),
								timestampForIndexUpdate );
							dataRingBuffer.backEndClearNumSamplesQueued();
						}

						final int numLeft = numSamplePerFrontEndPeriod - dataRingBuffer.backEndGetNumSamplesQueued();

						final int numAvailable = numFrames - curSampleIndex;
						final int numThisRound = ( numLeft > numAvailable ? numAvailable : numLeft );

						final int spaceAvailable = dataRingBuffer.getNumWriteable();

						final int numToWrite = ( spaceAvailable > numThisRound ? numThisRound : spaceAvailable );

						if( numToWrite > 0 )
						{
							dataRingBuffer.backEndWrite( inFloats, frameOffset + curSampleIndex, numToWrite );
						}
						else
						{
							break;
						}
						curSampleIndex += numThisRound;
					}
				}

			}
			catch( final Exception e )
			{
				final String msg = "Exception caught processing into ring buffer: " + e.toString();
				log.error( msg, e );
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected void queueWriteIndexUpdate( final ThreadSpecificTemporaryEventStorage tses,
			final int dataChannelNum,
			final int writePosition,
			final long frameTime )
	{
		final long joinedParts = ((long)writePosition << 32 ) | (dataChannelNum );

		localBridge.queueTemporalEventToUi( tses,
			frameTime,
			SpectralAmpGenIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX,
			joinedParts,
			null );
	}

	public BackendToFrontendDataRingBuffer getDataRingBuffer()
	{
		return dataRingBuffer;
	}
}
