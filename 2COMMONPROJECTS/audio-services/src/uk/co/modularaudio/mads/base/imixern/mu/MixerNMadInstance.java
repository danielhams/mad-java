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

package uk.co.modularaudio.mads.base.imixern.mu;

import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.util.audio.dsp.LimiterCrude;
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
import uk.co.modularaudio.util.math.Float16;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MixerNMadInstance<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D,I>> extends MadInstance<D,I>
{
//	private static Log log = LogFactory.getLog( MixerNMadInstance.class.getName() );

	private int sampleFramesPerFrontEndPeriod = 0;
	private int numSamplesProcessed = 0;

	private final int numInputLanes;
	private final int numTotalLanes;

	private final LaneProcessor[] laneProcessors;

	private final MixerMuteAndSoloMachine<D,I> muteAndSoloMachine;

	private final int leftOutputChannelIndex = 0;
	private final int rightOutputChannelIndex = 1;

	private final LimiterCrude limiterRt = new LimiterCrude( 0.99, 5 );

	private boolean active;

	@SuppressWarnings("unchecked")
	public MixerNMadInstance( final String instanceName,
			final D definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		final MixerNInstanceConfiguration instanceConfiguration = definition.getMixerInstanceConfiguration();

		numInputLanes = instanceConfiguration.getNumInputLanes();
		numTotalLanes = instanceConfiguration.getNumTotalLanes();

		laneProcessors = new LaneProcessor[ numTotalLanes ];
		laneProcessors[0] = new MasterOutProcessor<D,I>( (I)this, instanceConfiguration );
		for( int il = 0 ; il < numInputLanes ; ++il )
		{
			laneProcessors[il+1] = new InputLaneProcessor<D,I>( (I)this, instanceConfiguration, il+1 );
		}

		muteAndSoloMachine = new MixerMuteAndSoloMachine<D,I>( laneProcessors );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		sampleFramesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
		numSamplesProcessed = 0;

		final int sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

		for( final LaneProcessor lp : laneProcessors )
		{
			lp.setSampleRate( sampleRate );
		}
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final int U_periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers ,
			final int frameOffset,
			final int numFrames  )
	{
		// Zero output so lane processors can just add to existing output floats
		final float[] leftOutputFloats = channelBuffers[ leftOutputChannelIndex ].floatBuffer;
		final float[] rightOutputFloats = channelBuffers[ rightOutputChannelIndex ].floatBuffer;
		final int lastFrameIndex = frameOffset + numFrames;
		Arrays.fill( leftOutputFloats, frameOffset, lastFrameIndex, 0.0f );
		Arrays.fill( rightOutputFloats, frameOffset, lastFrameIndex, 0.0f );

		int currentSampleIndex = 0;

		while( currentSampleIndex < numFrames )
		{
			if( active && numSamplesProcessed >= sampleFramesPerFrontEndPeriod )
			{
				// Emit stuff
//				log.debug("Emitting meter readings at " + emitTimestamp );
				final int U_emitFrameTime = U_periodStartFrameTime + currentSampleIndex;

				for( final LaneProcessor lp : laneProcessors )
				{
					lp.emitLaneMeterReadings( tempQueueEntryStorage, U_emitFrameTime );
				}

//				postProcess( tempQueueEntryStorage, timingParameters, emitFrameTime );

				numSamplesProcessed = 0;
			}

			final int numFramesAvail = numFrames - currentSampleIndex;

			final int numLeftForPeriod = ( active ? sampleFramesPerFrontEndPeriod - numSamplesProcessed : numFramesAvail );
			final int numThisRound = (numLeftForPeriod < numFramesAvail ? numLeftForPeriod : numFramesAvail );

			final int frameProcessingOffset = frameOffset + currentSampleIndex;

			// Do the input lanes first, then the master
			for( int il = 0 ; il < numInputLanes ; ++il )
			{
				laneProcessors[il+1].processLane( tempQueueEntryStorage,
						channelConnectedFlags,
						channelBuffers,
						frameProcessingOffset,
						numThisRound );
			}
			laneProcessors[0].processLane( tempQueueEntryStorage,
					channelConnectedFlags,
					channelBuffers,
					frameProcessingOffset,
					numThisRound );

			currentSampleIndex += numThisRound;
			numSamplesProcessed += numThisRound;
		}

		// Finally, run a limiter over the output to curb any clipping.
		limiterRt.filter( leftOutputFloats, frameOffset, numFrames );
		limiterRt.filter( rightOutputFloats, frameOffset, numFrames );

//		debugTimestamp( "Done ", emitTimestamp );
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public void stop() throws MadProcessingException
	{
		// Nothing for now.
	}

	public void emitMeterReading( final ThreadSpecificTemporaryEventStorage tses,
			final int U_frameTime,
			final int laneNumber,
			final float leftMeterLevel,
			final float rightMeterLevel )
		throws BufferOverflowException
	{
		if( active )
		{
//			log.debug("Emitting meter reading for lane " + laneNumber );
			final long leftFloatIntBits = Float16.fromFloat( leftMeterLevel );
			final long rightFloatIntBits = Float16.fromFloat( rightMeterLevel );
			final long joinedParts = (leftFloatIntBits << 48) |
					(rightFloatIntBits << 32) |
					laneNumber;
			localBridge.queueTemporalEventToUi( tses, U_frameTime, MixerNIOQueueBridge.COMMAND_OUT_METER, joinedParts, null );
		}
	}

	protected void setLaneAmp( final int laneNum, final float ampValue )
	{
		laneProcessors[ laneNum ].setLaneAmp( ampValue );
	}

	protected void setLanePan( final int laneNum, final float panValue )
	{
		laneProcessors[ laneNum ].setLanePan( panValue );
	}

	protected void setLaneMute( final ThreadSpecificTemporaryEventStorage tses,
			final long currentTimestamp,
			final int laneNumber,
			final boolean muteValue )
	{
		muteAndSoloMachine.setLaneMute( tses, currentTimestamp, laneNumber, muteValue );
	}

	protected void setLaneSolo( final ThreadSpecificTemporaryEventStorage tses,
			final long currentTimestamp,
			final int laneNumber,
			final boolean soloValue )
	{
		muteAndSoloMachine.setLaneSolo( tses, currentTimestamp, laneNumber, soloValue );
	}

	protected void setActive( final boolean active )
	{
		this.active = active;
		numSamplesProcessed = 0;
	}

}
