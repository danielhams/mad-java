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

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
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

public class ControllerToCvMadInstance extends MadInstance<ControllerToCvMadDefinition,ControllerToCvMadInstance>
{
//	private static Log log = LogFactory.getLog( ControllerToCvMadInstance.class.getName() );
	
	private int notePeriodLength = -1;
	
	private int sampleRate = -1;
	private static final int VALUE_CHASE_MILLIS = 1;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	
	private ControllerEventProcessor eventProcessor = null;
	
	protected ControllerEventMapping desiredMapping = ControllerEventMapping.LINEAR;
	protected int desiredChannel = 0;
	protected int desiredController = 0;
	
	public ControllerToCvMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			ControllerToCvMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
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
			final long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, final int numFrames )
	{
		boolean noteConnected = channelConnectedFlags.get( ControllerToCvMadDefinition.CONSUMER_NOTE );
		MadChannelBuffer noteCb = channelBuffers[ ControllerToCvMadDefinition.CONSUMER_NOTE ];
		boolean outCvConnected = channelConnectedFlags.get( ControllerToCvMadDefinition.PRODUCER_CV_OUT );
		MadChannelBuffer outCvCb = channelBuffers[ ControllerToCvMadDefinition.PRODUCER_CV_OUT ];
		
//		eventProcessor.setNewRatios( curValueRatio, newValueRatio );
		eventProcessor.setDesiredMapping( desiredMapping );
		
		if( noteConnected )
		{
			MadChannelNoteEvent[] noteEvents = noteCb.noteBuffer;
			int numNotes = noteCb.numElementsInBuffer;
			// Process the messages
			for( int n = 0 ; n < numNotes ; n++ )
			{
				MadChannelNoteEvent ne = noteEvents[ n ];
				switch( ne.getEventType() )
				{
					case CONTROLLER:
					{
						// Only process events on our channel
						if( ne.getChannel() == desiredChannel && ne.getParamOne() == desiredController )
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
			
			if( outCvConnected )
			{
				float[] outCvFloats = outCvCb.floatBuffer;
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
			float[] outCvFloats = outCvCb.floatBuffer;

			eventProcessor.emptyPeriod( numFrames );
			
			// Output nothing.
			eventProcessor.outputCv( numFrames, outCvFloats );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
