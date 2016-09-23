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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.lookuptable.fade.FadeConstants;
import uk.co.modularaudio.util.audio.lookuptable.fade.FadeInWaveTable;
import uk.co.modularaudio.util.audio.lookuptable.fade.FadeOutWaveTable;
import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;
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

	private boolean active;
	private boolean desiredActive;

	private int maxRingBufferingInSamples;

	private BackendToFrontendDataRingBuffer dataRingBuffer;

	private int numSamplePerFrontEndPeriod;

	private FadeInWaveTable fadeInTable;
	private FadeOutWaveTable fadeOutTable;
	private int fadePosition;

	public SpectralAmpGenMadInstance( final String instanceName,
			final D definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		dataRingBuffer = new BackendToFrontendDataRingBuffer( 1 );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings,
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

			maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
					FadeConstants.SLOW_FADE_MILLIS ) +
				AudioTimingUtils.getNumSamplesForNanosAtSampleRate( sampleRate, nanosForBuffering );

			dataRingBuffer = new BackendToFrontendDataRingBuffer( maxRingBufferingInSamples );
			dataRingBuffer.backEndClearNumSamplesQueued();

			numSamplePerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();

			fadeInTable = timingParameters.getSlowFadeInWaveTable();
			fadeOutTable = timingParameters.getSlowFadeOutWaveTable();
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
			final int U_periodStartTimestamp,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final boolean inConnected = channelConnectedFlags.get( SpectralAmpGenMadDefinition.CONSUMER_IN);
		final MadChannelBuffer inCb = channelBuffers[ SpectralAmpGenMadDefinition.CONSUMER_IN ];
		final float[] inFloats = inCb.floatBuffer;

		if( desiredActive != active )
		{
			// Need to swap over until we run out of fade.
			final RawLookupTable fadeTable = ( desiredActive ? fadeInTable : fadeOutTable );
			final int numLeftInFade = fadeTable.capacity - fadePosition;
//			log.debug("Have " + numLeftInFade + " left to fade and need " + numFrames + " frames in process");

			final int numThisRound = (numLeftInFade < numFrames ? numLeftInFade : numFrames );
//			log.debug("Can fade " + numThisRound + " this round");
			final int numWithoutFade = numFrames - numThisRound;
//			log.debug("Filling remaining " + numWithoutFade + " with zeros");

			final int tmpPosition = 0;
			final float[] tmpFloats = tempQueueEntryStorage.temporaryFloatArray;

			System.arraycopy( inFloats, frameOffset, tmpFloats, tmpPosition, numFrames );

			for( int s = 0 ; s < numThisRound ; ++s )
			{
				final float fadeVal = fadeTable.getValueAt( fadePosition );
				tmpFloats[s] *= fadeVal;
				fadePosition++;
			}

			if( !desiredActive && numWithoutFade > 0 )
			{
				Arrays.fill( tmpFloats, numThisRound, numFrames, 0.0f );
			}

			// And push to the front end
			final int U_timestampForIndexUpdate = U_periodStartTimestamp + numFrames;
			final int numWritten = dataRingBuffer.backEndWrite( tmpFloats, tmpPosition, numFrames );
			if( numWritten != numFrames )
			{
				log.warn("Missed out on some data in back end write");
			}
			queueWriteIndexUpdate( tempQueueEntryStorage,
					0,
					dataRingBuffer.getWritePosition(),
					U_timestampForIndexUpdate );
			dataRingBuffer.backEndClearNumSamplesQueued();

			if( fadePosition >= fadeTable.capacity )
			{
//				log.debug("Completed fade, switching over active flag");
				active = desiredActive;
			}
		}
		else if( active )
		{
			try
			{
				if( inConnected )
				{
					int curSampleIndex = 0;
					while( curSampleIndex < numFrames )
					{
						final int U_timestampForIndexUpdate = U_periodStartTimestamp + curSampleIndex;

						if( dataRingBuffer.backEndGetNumSamplesQueued() >= numSamplePerFrontEndPeriod )
						{
							queueWriteIndexUpdate( tempQueueEntryStorage,
								0,
								dataRingBuffer.getWritePosition(),
								U_timestampForIndexUpdate );
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
			final int U_frameTime )
	{
		final long joinedParts = ((long)writePosition << 32 ) | (dataChannelNum );

		localBridge.queueTemporalEventToUi( tses,
			U_frameTime,
			SpectralAmpGenIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX,
			joinedParts,
			null );
	}

	public BackendToFrontendDataRingBuffer getDataRingBuffer()
	{
		return dataRingBuffer;
	}

	public void setActive( final boolean desiredActive )
	{
		this.desiredActive = desiredActive;
		fadePosition = 0;
	}
}
