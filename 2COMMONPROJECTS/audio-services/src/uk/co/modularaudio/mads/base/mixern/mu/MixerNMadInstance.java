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

package uk.co.modularaudio.mads.base.mixern.mu;

import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.util.audio.dsp.Limiter;
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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MixerNMadInstance<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D,I>> extends MadInstance<D,I>
{
//	private static Log log = LogFactory.getLog( MixerNMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 2;

	private final MixerNInstanceConfiguration instanceConfiguration;

	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	private long sampleRate;

	private int sampleFramesPerFrontEndPeriod = 0;
	private int numSamplesProcessed = 0;

	private final int numInputLanes;
	private final LaneProcessor<D,I>[] channelLaneProcessors;
	private final MasterProcessor<D,I> masterProcessor;
	private final MixerMuteAndSoloMachine<D,I> muteAndSoloMachine;

	private int leftOutputChannelIndex = -1;
	private int rightOutputChannelIndex = -1;

	private final Limiter limiterRt = new Limiter( 0.99, 5 );

	public boolean active;

	@SuppressWarnings("unchecked")
	public MixerNMadInstance( final String instanceName,
			final D definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		instanceConfiguration = definition.getMixerInstanceConfiguration();

		numInputLanes = instanceConfiguration.getNumInputLanes();
		channelLaneProcessors = new LaneProcessor[ numInputLanes ];
		for( int i = 0 ; i < numInputLanes ; i++ )
		{
			channelLaneProcessors[ i ] = new LaneProcessor<D,I>( (I)this, instanceConfiguration, i, curValueRatio, newValueRatio );
		}
		masterProcessor = new MasterProcessor<D,I>( (I)this, instanceConfiguration, curValueRatio, newValueRatio );

		muteAndSoloMachine = new MixerMuteAndSoloMachine<D,I>( (I)this, channelLaneProcessors );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;

			for( int i = 0 ; i < numInputLanes ; i++ )
			{
				channelLaneProcessors[ i ].resetCurNewValues( curValueRatio, newValueRatio );
			}
			masterProcessor.resetCurNewValues( curValueRatio, newValueRatio );

			leftOutputChannelIndex = instanceConfiguration.getIndexForOutputChannel( 0 );
			rightOutputChannelIndex = instanceConfiguration.getIndexForOutputChannel( 1 );

			sampleFramesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
			numSamplesProcessed = 0;
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught starting up mixer: " + e.toString();
			throw new MadProcessingException( msg, e );
		}
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int numFrames )
	{
		// Zero output so lane processors can just add to existing output floats
		final float[] leftOutputFloats = channelBuffers[ leftOutputChannelIndex ].floatBuffer;
		final float[] rightOutputFloats = channelBuffers[ rightOutputChannelIndex ].floatBuffer;
		Arrays.fill(  leftOutputFloats, 0.0f );
		Arrays.fill(  rightOutputFloats, 0.0f );

		int currentSampleIndex = 0;

		while( currentSampleIndex < numFrames )
		{
			if( active && numSamplesProcessed >= sampleFramesPerFrontEndPeriod )
			{
				// Emit stuff
//				log.debug("Emitting meter readings at " + emitTimestamp );
				final long emitFrameTime = periodStartFrameTime + currentSampleIndex;

				for( int il = 0 ; il < numInputLanes ; il++ )
				{
					channelLaneProcessors[ il ].emitLaneMeterReadings( tempQueueEntryStorage, emitFrameTime );
				}
				masterProcessor.emitMasterMeterReadings( tempQueueEntryStorage, emitFrameTime );

				// Make sure they get pushed
//				if( tempQueueEntryStorage.numTemporalEventsToUi > 0 )
//				{
//					log.debug("Emitting " + tempQueueEntryStorage.numTemporalEventsToUi + " events to UI");
//				}
				postProcess( tempQueueEntryStorage, timingParameters, emitFrameTime );

				// And process new events
//				debugTimestamp( "SubPe", emitTimestamp );
				preProcess( tempQueueEntryStorage, timingParameters, emitFrameTime );
//				if( tempQueueEntryStorage.numTemporalEventsToInstance > 0 )
//				{
//					log.debug("Consuming " + tempQueueEntryStorage.numTemporalEventsToInstance + " events to instance");
//				}
				numSamplesProcessed = 0;
			}

			final int numFramesAvail = numFrames - currentSampleIndex;

			final int numLeftForPeriod = ( active ? sampleFramesPerFrontEndPeriod - numSamplesProcessed : numFramesAvail );
			final int numThisRound = (numLeftForPeriod < numFramesAvail ? numLeftForPeriod : numFramesAvail );

			// Get each channel to add it's output in
			for( int il = 0 ; il < numInputLanes ; il++ )
			{
				channelLaneProcessors[ il ].processLaneMixToOutput( tempQueueEntryStorage, channelConnectedFlags, channelBuffers, currentSampleIndex, numThisRound );
			}

			// Now apply master mix multiplier
			masterProcessor.processMasterOutput( tempQueueEntryStorage, channelConnectedFlags, channelBuffers, currentSampleIndex, numThisRound );

			currentSampleIndex += numThisRound;
			numSamplesProcessed += numThisRound;
		}

		// Finally, run a limiter over the output to curb any clipping.
		limiterRt.filter( leftOutputFloats, 0, numFrames );
		limiterRt.filter( rightOutputFloats, 0, numFrames );

//		debugTimestamp( "Done ", emitTimestamp );
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public void stop() throws MadProcessingException
	{
		// Nothing for now.
	}

	public void emitLaneMeterReading( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int laneNumber,
			final float leftMeterLevel,
			final float rightMeterLevel )
		throws BufferOverflowException
	{
		if( active )
		{
			long floatIntBits = Float.floatToIntBits( leftMeterLevel );
			long joinedParts = (floatIntBits << 32) | (laneNumber * 2);
			localBridge.queueTemporalEventToUi( tses, frameTime, MixerNIOQueueBridge.COMMAND_OUT_LANE_METER, joinedParts, null );

			floatIntBits = Float.floatToIntBits( rightMeterLevel );
			joinedParts = (floatIntBits << 32) | (laneNumber * 2) + 1;
			localBridge.queueTemporalEventToUi( tses, frameTime, MixerNIOQueueBridge.COMMAND_OUT_LANE_METER, joinedParts, null );
		}
	}

	public void emitMasterMeterReading( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final float leftMeterLevel,
			final float rightMeterLevel )
		throws BufferOverflowException
	{
		if( active )
		{
			long floatIntBits = Float.floatToIntBits( leftMeterLevel );
			long joinedParts = (floatIntBits << 32 ) | (0);

			localBridge.queueTemporalEventToUi( tses, frameTime, MixerNIOQueueBridge.COMMAND_OUT_MASTER_METER, joinedParts, null );

			floatIntBits = Float.floatToIntBits( rightMeterLevel );
			joinedParts = (floatIntBits << 32 ) | (1);
			localBridge.queueTemporalEventToUi( tses, frameTime, MixerNIOQueueBridge.COMMAND_OUT_MASTER_METER, joinedParts, null );
		}
	}

	protected void emitLaneMuteStatus( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int laneNumber,
			final boolean muted )
			throws BufferOverflowException
	{
		final long muteBits = (muted ? 1 : 0 );
		final long joinedParts = (muteBits << 32 ) | laneNumber;
		localBridge.queueTemporalEventToUi( tses, frameTime, MixerNIOQueueBridge.COMMAND_OUT_LANE_MUTE_SET, joinedParts, null );
	}

	protected void emitLaneSoloStatus( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int laneNumber,
			final boolean solod )
			throws BufferOverflowException
	{
		final long soloBits = (solod ? 1 : 0 );
		final long joinedParts = (soloBits << 32 ) | laneNumber;
		localBridge.queueTemporalEventToUi( tses, frameTime, MixerNIOQueueBridge.COMMAND_OUT_LANE_SOLO_SET, joinedParts, null );
	}

	protected void setLaneAmp( final int laneNum, final float ampValue )
	{
		channelLaneProcessors[ laneNum ].setLaneAmp( ampValue );
	}

	protected void setMasterAmp( final float masterAmp )
	{
		masterProcessor.setMasterAmp( masterAmp );
	}

	protected void setLanePan( final int laneNum, final float panValue )
	{
		channelLaneProcessors[ laneNum ].setLanePan( panValue );
	}

	protected void setMasterPan( final float panValue )
	{
		masterProcessor.setMasterPan( panValue );
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
