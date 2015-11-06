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

package uk.co.modularaudio.mads.base.controllerhistogram.mu;

import java.util.Map;

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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class ControllerHistogramMadInstance extends MadInstance<ControllerHistogramMadDefinition,ControllerHistogramMadInstance>
{
//	private static Log log = LogFactory.getLog( NoteHistogramMadInstance.class.getName() );

	private long lastNoteFrameTime = -1;

	public ControllerHistogramMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final ControllerHistogramMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
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
		final MadChannelBuffer noteBuffer = channelBuffers[ControllerHistogramMadDefinition.CONSUMER_NOTE];
		final MadChannelNoteEvent[] noteEvents = noteBuffer.noteBuffer;

		final int numNotes = noteBuffer.numElementsInBuffer;
		if( numNotes > 0 )
		{
			if( lastNoteFrameTime == -1 )
			{
				lastNoteFrameTime = periodStartFrameTime + noteEvents[0].getEventSampleIndex();
			}
			else
			{
				int noteIndex = 0;
				do
				{
					final int noteEventSampleIndex = noteEvents[noteIndex].getEventSampleIndex();
					final long noteEventFrameTime = periodStartFrameTime + noteEventSampleIndex;

					final int diff = (int)(noteEventFrameTime - lastNoteFrameTime);

					if( diff > 0 )
					{
						sendDiscoveredController( tempQueueEntryStorage,
								noteEventFrameTime,
								diff );
					}
					lastNoteFrameTime = noteEventFrameTime;
					noteIndex++;
				}
				while( noteIndex < numNotes );

			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private void sendDiscoveredController( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int noteDiff )
	{
		localBridge.queueTemporalEventToUi( tses,
				frameTime,
				ControllerHistogramIOQueueBridge.COMMAND_OUT_NOTE_DIFF,
				noteDiff,
				null );

	}
}
