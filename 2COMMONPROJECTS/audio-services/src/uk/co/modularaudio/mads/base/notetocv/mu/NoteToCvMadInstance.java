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

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class NoteToCvMadInstance extends MadInstance<NoteToCvMadDefinition,NoteToCvMadInstance>
{
//	private static Log log = LogFactory.getLog( NoteToCvMadInstance.class.getName() );
	
	private final static float FREQ_VALUE_CHASE_MILLIS = 10;
	
	private int sampleRate = -1;
	private int periodLength = -1;
	private int notePeriodLength = -1;
	
	private float freqGlideCurValueRatio = 0.0f;
	private float freqGlideNewValueRatio = 0.0f;
	private float ampGlideCurValueRatio = 0.0f;
	private float ampGlideNewValueRatio = 0.0f;
	private PeriodNoteState periodNoteState = new PeriodNoteState();

	public NoteOnType desiredNoteOnType = NoteOnType.FOLLOW_FIRST;

	// Default - all channels
	public int desiredChannelNum = -1;
	
	public NoteToCvMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			NoteToCvMadDefinition definition,
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
			periodLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			notePeriodLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();
			
			freqGlideNewValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, FREQ_VALUE_CHASE_MILLIS );
			freqGlideCurValueRatio = 1.0f - freqGlideNewValueRatio;
			
			ampGlideNewValueRatio = 0.05f;
			ampGlideCurValueRatio = 1.0f - ampGlideNewValueRatio;
			
			periodNoteState.resize( periodLength, notePeriodLength );
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
		boolean noteConnected = channelConnectedFlags.get( NoteToCvMadDefinition.CONSUMER_NOTE );
		boolean outGateConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_GATE_OUT );
		boolean outFreqConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_FREQ_OUT );
		boolean outVelocityConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_VELOCITY_OUT );
		boolean outTriggerConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_TRIGGER_OUT );
		boolean outVelAmpMultConnected = channelConnectedFlags.get( NoteToCvMadDefinition.PRODUCER_VEL_AMP_MULT_OUT);
		
		periodNoteState.setGlideRatios( freqGlideCurValueRatio, freqGlideNewValueRatio, ampGlideCurValueRatio, ampGlideNewValueRatio );
		
		periodNoteState.startNewPeriod( desiredNoteOnType );
		
		if( noteConnected )
		{
			MadChannelBuffer noteBuffer = channelBuffers[ NoteToCvMadDefinition.CONSUMER_NOTE ];
			MadChannelNoteEvent[] noteEvents = noteBuffer.noteBuffer;
			int numNotes = noteBuffer.numElementsInBuffer;

			for( int i = 0 ; i < numNotes ; i++ )
			{
				MadChannelNoteEvent noteEvent = noteEvents[i];
				MadChannelNoteEventType eventType = noteEvent.getEventType();
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
			MadChannelBuffer outGateBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_GATE_OUT ];
			float[] gb = outGateBuffer.floatBuffer;
			periodNoteState.fillGate( gb );
		}
		
		if( outFreqConnected )
		{
			MadChannelBuffer outFreqBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_FREQ_OUT ];
			float[] fb = outFreqBuffer.floatBuffer;
			periodNoteState.fillFrequency( fb );
		}

		if( outVelocityConnected )
		{
			MadChannelBuffer outVelocityBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_VELOCITY_OUT ];
			float[] vb = outVelocityBuffer.floatBuffer;
			periodNoteState.fillVelocity( vb );
		}

		if( outTriggerConnected )
		{
			MadChannelBuffer outTriggerBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_TRIGGER_OUT ];
			float[] tb = outTriggerBuffer.floatBuffer;
			periodNoteState.fillTrigger( tb );
		}

		if( outVelAmpMultConnected )
		{
			MadChannelBuffer outVelAmpMultBuffer = channelBuffers[ NoteToCvMadDefinition.PRODUCER_VEL_AMP_MULT_OUT ];
			float[] tb = outVelAmpMultBuffer.floatBuffer;
			periodNoteState.fillVelAmpMultiplier( tb );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
	
	protected void setFrequencyGlideMillis( float val )
	{
		val = ( val < 0.0f ? 0.0f : val );
		freqGlideNewValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, val );
		freqGlideCurValueRatio = 1.0f - freqGlideNewValueRatio;
//		log.debug("Setting frequency glide with cur=" + freqGlideCurValueRatio + " and new=" + freqGlideNewValueRatio );
	}
}
