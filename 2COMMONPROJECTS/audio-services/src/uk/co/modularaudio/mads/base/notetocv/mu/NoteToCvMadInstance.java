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

package uk.co.modularaudio.mads.base.notetocv.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.notetocv.ui.NoteChannelChoiceUiJComponent;
import uk.co.modularaudio.mads.base.notetocv.ui.NoteOnTypeChoiceUiJComponent;
import uk.co.modularaudio.mads.base.notetocv.ui.NoteOnTypeChoiceUiJComponent.NoteOnType;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class NoteToCvMadInstance extends MadInstance<NoteToCvMadDefinition,NoteToCvMadInstance>
{
	private static Log log = LogFactory.getLog( NoteToCvMadInstance.class.getName() );

	private final static float FREQ_VALUE_CHASE_MILLIS = 10;

	private int sampleRate;
	private int periodLength;
	private int notePeriodLength;

	private float freqGlideCurValueRatio;
	private float freqGlideNewValueRatio;
	private float ampGlideCurValueRatio;
	private float ampGlideNewValueRatio;
	private final PeriodNoteState periodNoteState = new PeriodNoteState();

	public NoteOnType desiredNoteOnType = NoteOnTypeChoiceUiJComponent.DEFAULT_NOTE_ON_TYPE;

	public int desiredChannelNum = NoteChannelChoiceUiJComponent.DEFAULT_CHANNEL.getChannelNum();

	public NoteToCvMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final NoteToCvMadDefinition definition,
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
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
			periodLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			notePeriodLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();

			freqGlideNewValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, FREQ_VALUE_CHASE_MILLIS );
			freqGlideCurValueRatio = 1.0f - freqGlideNewValueRatio;

			ampGlideNewValueRatio = 0.05f;
			ampGlideCurValueRatio = 1.0f - ampGlideNewValueRatio;

			periodNoteState.resize( periodLength, notePeriodLength );
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
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final boolean noteConnected = channelConnectedFlags.get( NoteToCvMadDefinition.CONSUMER_NOTE );
		final boolean outGateConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_GATE_OUT );
		final boolean outFreqConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_FREQ_OUT );
		final boolean outVelocityConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_VELOCITY_OUT );
		final boolean outTriggerConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_TRIGGER_OUT );
		final boolean outVelAmpMultConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_VEL_AMP_MULT_OUT);

		periodNoteState.setGlideRatios( freqGlideCurValueRatio, freqGlideNewValueRatio, ampGlideCurValueRatio, ampGlideNewValueRatio );

		periodNoteState.startNewPeriod( desiredNoteOnType );

		if( noteConnected )
		{
			final MadChannelBuffer noteBuffer = channelBuffers[ NoteToCvMadDefinition.CONSUMER_NOTE ];
			final MadChannelNoteEvent[] noteEvents = noteBuffer.noteBuffer;
			final int numNotes = noteBuffer.numElementsInBuffer;

			for( int i = 0 ; i < numNotes ; i++ )
			{
				final MadChannelNoteEvent noteEvent = noteEvents[i];
				if( desiredChannelNum != -1 && noteEvent.getChannel() != desiredChannelNum )
				{
					continue;
				}
				final MadChannelNoteEventType eventType = noteEvent.getEventType();
				if( log.isTraceEnabled() )
				{
					log.trace("Received note event in process(" + frameOffset + ", " + numFrames +
							" " + eventType.toString() + " " + noteEvent.toString() );
				}
				switch( eventType )
				{
					case NOTE_ON:
					{
//						log.debug("Note is " + noteEvent.paramOne );
					}
					case NOTE_OFF:
					case NOTE_CONTINUATION:
					case NOTE_AFTERTOUCH:
					{
//						if( log.isDebugEnabled() )
//						{
//							log.debug("Passing event to period note state processing: " + noteEvent.toString() );
//						}
						periodNoteState.addNewEvent( noteEvent );
						break;
					}
					default:
					{
					}
				}
			}
		}
		else
		{
//			log.debug("No events as note channel not connected");
			periodNoteState.turnOffNotes( numFrames );
		}

		periodNoteState.endPeriod( numFrames );

		if( outGateConnected )
		{
			final MadChannelBuffer outGateBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_GATE_OUT ];
			final float[] gb = outGateBuffer.floatBuffer;
			periodNoteState.fillGate( gb, frameOffset, numFrames );
		}

		if( outTriggerConnected )
		{
			final MadChannelBuffer outTriggerBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_TRIGGER_OUT ];
			final float[] tb = outTriggerBuffer.floatBuffer;
			periodNoteState.fillTrigger( tb, frameOffset, numFrames );
		}

		if( outFreqConnected )
		{
			final MadChannelBuffer outFreqBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_FREQ_OUT ];
			final float[] fb = outFreqBuffer.floatBuffer;
			periodNoteState.fillFrequency( fb, frameOffset, numFrames );
		}

		if( outVelocityConnected )
		{
			final MadChannelBuffer outVelocityBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_VELOCITY_OUT ];
			final float[] vb = outVelocityBuffer.floatBuffer;
			periodNoteState.fillVelocity( vb, frameOffset, numFrames );
		}

		if( outVelAmpMultConnected )
		{
			final MadChannelBuffer outVelAmpMultBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_VEL_AMP_MULT_OUT ];
			final float[] tb = outVelAmpMultBuffer.floatBuffer;
			periodNoteState.fillVelAmpMultiplier( tb, frameOffset, numFrames );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected void setFrequencyGlideMillis( final float iValue )
	{
		final float val = ( iValue < 0.0f ? 0.0f : iValue );
		freqGlideNewValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, val );
		freqGlideCurValueRatio = 1.0f - freqGlideNewValueRatio;
//		log.debug("Setting frequency glide with cur=" + freqGlideCurValueRatio + " and new=" + freqGlideNewValueRatio );
	}

	public void setDesiredNoteOnType( final NoteOnType not )
	{
		desiredNoteOnType = not;
	}

	public void setDesiredChannelNum( final int channelNum )
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Setting desired channel num to " + channelNum );
		}
		desiredChannelNum = channelNum;
	}
}
