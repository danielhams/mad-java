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

package uk.co.modularaudio.mads.base.controllertocv.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class ControllerToCvMadInstance extends MadInstance<ControllerToCvMadDefinition,ControllerToCvMadInstance>
{
	private static Log log = LogFactory.getLog( ControllerToCvMadInstance.class.getName() );

	private int notePeriodLength;

	private int sampleRate;
	private static final int VALUE_CHASE_MILLIS = 1;
	private float curValueRatio = 0.0f;
	private float newValueRatio = 1.0f;

	private ControllerEventProcessor eventProcessor;

	private ControllerEventMapping desiredMapping = ControllerEventMapping.LINEAR;
	private int desiredChannel = 0;
	private int desiredController = 0;

	private boolean isLearning;

	public ControllerToCvMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final ControllerToCvMadDefinition definition,
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
			notePeriodLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;

			eventProcessor = new ControllerEventProcessor( notePeriodLength );
			eventProcessor.setNewRatios( curValueRatio, newValueRatio );
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
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{
		final boolean noteConnected = channelConnectedFlags.get( ControllerToCvMadDefinition.CONSUMER_NOTE );
		final MadChannelBuffer noteCb = channelBuffers[ ControllerToCvMadDefinition.CONSUMER_NOTE ];
		final boolean outCvConnected = channelConnectedFlags.get( ControllerToCvMadDefinition.PRODUCER_CV_OUT );
		final MadChannelBuffer outCvCb = channelBuffers[ ControllerToCvMadDefinition.PRODUCER_CV_OUT ];

//		eventProcessor.setNewRatios( curValueRatio, newValueRatio );
		eventProcessor.setDesiredMapping( desiredMapping );

		if( noteConnected )
		{
			final MadChannelNoteEvent[] noteEvents = noteCb.noteBuffer;
			final int numNotes = noteCb.numElementsInBuffer;

			if( isLearning )
			{
				int lastController = -1;
				int lastChannel = -1;
				boolean wasController = false;
				for( int n = 0 ; n < numNotes ; n++ )
				{
					final MadChannelNoteEvent ne = noteEvents[ n ];
					switch( ne.getEventType() )
					{
						case CONTROLLER:
						{
							lastChannel = ne.getChannel();
							lastController = ne.getParamOne();
							wasController = true;
							break;
						}
						default:
						{
							break;
						}
					}
				}
				if( wasController )
				{
					// Encode channel and controller in a message back
					// to the UI
					sendDiscoveredController( tempQueueEntryStorage, periodStartFrameTime, lastChannel, lastController );
					isLearning = false;
				}

				eventProcessor.emptyPeriod( numFrames );
			}
			else
			{
				// Process the messages
				for( int n = 0 ; n < numNotes ; n++ )
				{
					final MadChannelNoteEvent ne = noteEvents[ n ];
					switch( ne.getEventType() )
					{
						case CONTROLLER:
						{
							// Only process events on our channel
							if( (desiredChannel == -1 || desiredChannel == ne.getChannel() )
								&&
								(desiredController == -1 || desiredController == ne.getParamOne() )
								)
							{
	//							log.debug("Processing event " + ne.toString() );
								eventProcessor.processEvent( ne );
							}
							break;
						}
						default:
						{
							break;
						}
					}
				}

				if( numNotes == 0 )
				{
					eventProcessor.emptyPeriod( numFrames );
				}
			}

			if( outCvConnected )
			{
				final float[] outCvFloats = outCvCb.floatBuffer;
				// Spit out values.
				eventProcessor.outputCv( numFrames, outCvFloats );
				eventProcessor.done();
			}
			else
			{
				eventProcessor.done();
			}
		}
		else if( outCvConnected )
		{
			if( isLearning )
			{

			}
			else
			{
				final float[] outCvFloats = outCvCb.floatBuffer;

				eventProcessor.emptyPeriod( numFrames );

				// Output nothing.
				eventProcessor.outputCv( numFrames, outCvFloats );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void beginLearn()
	{
		isLearning = true;
		log.trace("Beginning note learn");
	}

	public void setDesiredMapping( final ControllerEventMapping mapping )
	{
		this.desiredMapping = mapping;
	}

	public void setDesiredChannel( final int channelNumber )
	{
		this.desiredChannel = channelNumber;
	}

	public void setDesiredController( final int controllerNumber )
	{
		this.desiredController = controllerNumber;
	}

	private void sendDiscoveredController( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int lastChannel,
			final int lastController )
	{
		log.trace("Sending discovered channel " + lastChannel + " and controller " + lastController );
		final long value = (lastChannel << 32) | lastController;
		localBridge.queueTemporalEventToUi( tses,
				frameTime,
				ControllerToCvIOQueueBridge.COMMAND_OUT_LEARNT_CONTROLLER,
				value,
				null );

	}
}
