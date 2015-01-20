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

package uk.co.modularaudio.mads.subrack.mu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.co.modularaudio.mads.subrack.SubRackCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.service.madgraph.MadSubGraphDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphQueueBridge;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class SubRackMadDefinition extends MadSubGraphDefinition<SubRackMadDefinition, SubRackMadInstance>
{

	public final static int CONSUMER_AUDIO_IN_1= 0;
	public final static int CONSUMER_AUDIO_IN_2= 1;
	public final static int CONSUMER_AUDIO_IN_3= 2;
	public final static int CONSUMER_AUDIO_IN_4= 3;
	public final static int CONSUMER_AUDIO_IN_5= 4;
	public final static int CONSUMER_AUDIO_IN_6= 5;
	public final static int CONSUMER_AUDIO_IN_7= 6;
	public final static int CONSUMER_AUDIO_IN_8= 7;
	public final static int PRODUCER_AUDIO_OUT_1= 8;
	public final static int PRODUCER_AUDIO_OUT_2= 9;
	public final static int PRODUCER_AUDIO_OUT_3= 10;
	public final static int PRODUCER_AUDIO_OUT_4= 11;
	public final static int PRODUCER_AUDIO_OUT_5= 12;
	public final static int PRODUCER_AUDIO_OUT_6= 13;
	public final static int PRODUCER_AUDIO_OUT_7= 14;
	public final static int PRODUCER_AUDIO_OUT_8= 15;
	public final static int CONSUMER_CV_IN_1= 16;
	public final static int CONSUMER_CV_IN_2= 17;
	public final static int CONSUMER_CV_IN_3= 18;
	public final static int CONSUMER_CV_IN_4= 19;
	public final static int CONSUMER_CV_IN_5 = 20;
	public final static int CONSUMER_CV_IN_6= 21;
	public final static int CONSUMER_CV_IN_7= 22;
	public final static int CONSUMER_CV_IN_8= 23;
	public final static int PRODUCER_CV_OUT_1= 24;
	public final static int PRODUCER_CV_OUT_2= 25;
	public final static int PRODUCER_CV_OUT_3= 26;
	public final static int PRODUCER_CV_OUT_4= 27;
	public final static int PRODUCER_CV_OUT_5= 28;
	public final static int PRODUCER_CV_OUT_6= 29;
	public final static int PRODUCER_CV_OUT_7= 30;
	public final static int PRODUCER_CV_OUT_8= 31;
	public final static int CONSUMER_NOTE_IN_1= 32;
	public final static int CONSUMER_NOTE_IN_2= 33;
	public final static int CONSUMER_NOTE_IN_3= 34;
	public final static int CONSUMER_NOTE_IN_4= 35;
	public final static int CONSUMER_NOTE_IN_5= 36;
	public final static int CONSUMER_NOTE_IN_6= 37;
	public final static int CONSUMER_NOTE_IN_7= 38;
	public final static int CONSUMER_NOTE_IN_8= 39;
	public final static int PRODUCER_NOTE_OUT_1= 40;
	public final static int PRODUCER_NOTE_OUT_2= 41;
	public final static int PRODUCER_NOTE_OUT_3= 42;
	public final static int PRODUCER_NOTE_OUT_4= 43;
	public final static int PRODUCER_NOTE_OUT_5= 44;
	public final static int PRODUCER_NOTE_OUT_6= 45;
	public final static int PRODUCER_NOTE_OUT_7= 46;
	public final static int PRODUCER_NOTE_OUT_8= 47;
	public final static int NUM_CHANNELS = 48;

	// These must match the channel indexes given above
	private final static String[] CHANNEL_NAMES = new String[] {
		"Input Channel 1",
		"Input Channel 2",
		"Input Channel 3",
		"Input Channel 4",
		"Input Channel 5",
		"Input Channel 6",
		"Input Channel 7",
		 "Input Channel 8",
		"Output Channel 1",
		"Output Channel 2",
		"Output Channel 3",
		"Output Channel 4",
		"Output Channel 5",
		"Output Channel 6",
		"Output Channel 7",
		 "Output Channel 8",
		 "Input CV Channel 1",
		 "Input CV Channel 2",
		 "Input CV Channel 3",
		 "Input CV Channel 4",
		 "Input CV Channel 5",
		 "Input CV Channel 6",
		 "Input CV Channel 7",
		 "Input CV Channel 8",
		 "Output CV Channel 1",
		 "Output CV Channel 2",
		 "Output CV Channel 3",
		 "Output CV Channel 4",
		 "Output CV Channel 5",
		 "Output CV Channel 6",
		 "Output CV Channel 7",
		 "Output CV Channel 8",
		 "Input Note Channel 1",
		 "Input Note Channel 2",
		 "Input Note Channel 3",
		 "Input Note Channel 4",
		 "Input Note Channel 5",
		 "Input Note Channel 6",
		 "Input Note Channel 7",
		 "Input Note Channel 8",
		 "Output Note Channel 1",
		 "Output Note Channel 2",
		 "Output Note Channel 3",
		 "Output Note Channel 4",
		 "Output Note Channel 5",
		 "Output Note Channel 6",
		 "Output Note Channel 7",
		 "Output Note Channel 8" };

	private final static MadChannelType[] CHANNEL_TYPES = new MadChannelType[] { MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE,
		MadChannelType.NOTE };

	private final static MadChannelDirection[] CHANNEL_DIRECTIONS = new MadChannelDirection[] { MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.CONSUMER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER };

	private final static MadChannelPosition[] CHANNEL_POSITIONS = new MadChannelPosition[] { MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO };

	public static final String DEFINITION_ID = "sub_rack";

	private final static String USER_VISIBLE_NAME = "Sub Rack";

	private final static String CLASS_GROUP = MadClassificationService.USER_RACK_GROUP_ID;
	private final static String CLASS_NAME = "Sub Rack";
	private final static String CLASS_DESC = "Internal component used to represent a sub rack placed as a component";

	private MadChannelConfiguration channelConfiguration;

	public SubRackMadDefinition( final SubRackCreationContext creationContext,
			final MadClassificationService classificationService )
		throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID,
				USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				new MadGraphQueueBridge<SubRackMadInstance>() );

		final MadChannelDefinition[] channelDefinitions = buildChannelDefinitions();
		channelConfiguration = new MadChannelConfiguration( channelDefinitions );
	}

	private MadChannelDefinition[] buildChannelDefinitions()
	{
		final List<MadChannelDefinition> retVal = new ArrayList<MadChannelDefinition>();

		for( int c = 0 ; c < NUM_CHANNELS ; c++ )
		{
			retVal.add( new MadChannelDefinition( CHANNEL_NAMES[ c ],
					CHANNEL_TYPES[ c ],
					CHANNEL_DIRECTIONS[ c ],
					CHANNEL_POSITIONS[ c ] ) );
		}

		return retVal.toArray( new MadChannelDefinition[ retVal.size() ] );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters( final Map<MadParameterDefinition, String> parameterValues )
	{
		return channelConfiguration;
	}
}
