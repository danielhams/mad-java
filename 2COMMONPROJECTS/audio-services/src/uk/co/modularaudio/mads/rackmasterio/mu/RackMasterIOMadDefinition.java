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

package uk.co.modularaudio.mads.rackmasterio.mu;

import uk.co.modularaudio.mads.rackmasterio.RackMasterIOCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadNullLocklessQueueBridge;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class RackMasterIOMadDefinition  extends AbstractNonConfigurableMadDefinition<RackMasterIOMadDefinition,RackMasterIOMadInstance>
{
//	private static Log log = LogFactory.getLog( RackMasterIOMadDefinition.class.getName());

	// Indexes into the channels
    public enum ChanIndexes
    {
        CONSUMER_AUDIO_IN_1,
        CONSUMER_AUDIO_IN_2,
        CONSUMER_AUDIO_IN_3,
        CONSUMER_AUDIO_IN_4,
        CONSUMER_AUDIO_IN_5,
        CONSUMER_AUDIO_IN_6,
        CONSUMER_AUDIO_IN_7,
        CONSUMER_AUDIO_IN_8,
        PRODUCER_AUDIO_OUT_1,
        PRODUCER_AUDIO_OUT_2,
        PRODUCER_AUDIO_OUT_3,
        PRODUCER_AUDIO_OUT_4,
        PRODUCER_AUDIO_OUT_5,
        PRODUCER_AUDIO_OUT_6,
        PRODUCER_AUDIO_OUT_7,
        PRODUCER_AUDIO_OUT_8,
        CONSUMER_CV_IN_1,
        CONSUMER_CV_IN_2,
        CONSUMER_CV_IN_3,
        CONSUMER_CV_IN_4,
        CONSUMER_CV_IN_5,
        CONSUMER_CV_IN_6,
        CONSUMER_CV_IN_7,
        CONSUMER_CV_IN_8,
        PRODUCER_CV_OUT_1,
        PRODUCER_CV_OUT_2,
        PRODUCER_CV_OUT_3,
        PRODUCER_CV_OUT_4,
        PRODUCER_CV_OUT_5,
        PRODUCER_CV_OUT_6,
        PRODUCER_CV_OUT_7,
        PRODUCER_CV_OUT_8,
        CONSUMER_NOTE_IN_1,
        CONSUMER_NOTE_IN_2,
        CONSUMER_NOTE_IN_3,
        CONSUMER_NOTE_IN_4,
        CONSUMER_NOTE_IN_5,
        CONSUMER_NOTE_IN_6,
        CONSUMER_NOTE_IN_7,
        CONSUMER_NOTE_IN_8,
        PRODUCER_NOTE_OUT_1,
        PRODUCER_NOTE_OUT_2,
        PRODUCER_NOTE_OUT_3,
        PRODUCER_NOTE_OUT_4,
        PRODUCER_NOTE_OUT_5,
        PRODUCER_NOTE_OUT_6,
        PRODUCER_NOTE_OUT_7,
        PRODUCER_NOTE_OUT_8,
        NUM_CHANNELS
    };

	private final static String[] CHAN_NAMES;
	private final static MadChannelType[] CHAN_TYPES;
	private final static MadChannelDirection[] CHAN_DIRS;
	private final static MadChannelPosition[] CHAN_POSI;

	static
	{
		final int numChannels = ChanIndexes.NUM_CHANNELS.ordinal();
		CHAN_NAMES = new String[numChannels];
		CHAN_TYPES = new MadChannelType[numChannels];
		CHAN_DIRS = new MadChannelDirection[numChannels];
		CHAN_POSI = new MadChannelPosition[numChannels];

		int curChan = 0;

		for( int i = 1 ; i <= 8 ; ++i )
		{
			CHAN_NAMES[curChan] = "Input Channel " + i;
			CHAN_TYPES[curChan] = MadChannelType.AUDIO;
			CHAN_DIRS[curChan] = MadChannelDirection.CONSUMER;
			CHAN_POSI[curChan] = MadChannelPosition.MONO;
			curChan++;
		}
		for( int o = 1 ; o <= 8 ; ++o )
		{
			CHAN_NAMES[curChan] = "Output Channel " + o;
			CHAN_TYPES[curChan] = MadChannelType.AUDIO;
			CHAN_DIRS[curChan] = MadChannelDirection.PRODUCER;
			CHAN_POSI[curChan] = MadChannelPosition.MONO;
			curChan++;
		}
		for( int i = 1 ; i <= 8 ; ++i )
		{
			CHAN_NAMES[curChan] = "Input CV Channel " + i;
			CHAN_TYPES[curChan] = MadChannelType.CV;
			CHAN_DIRS[curChan] = MadChannelDirection.CONSUMER;
			CHAN_POSI[curChan] = MadChannelPosition.MONO;
			curChan++;
		}
		for( int o = 1 ; o <= 8 ; ++o )
		{
			CHAN_NAMES[curChan] = "Output CV Channel " + o;
			CHAN_TYPES[curChan] = MadChannelType.CV;
			CHAN_DIRS[curChan] = MadChannelDirection.PRODUCER;
			CHAN_POSI[curChan] = MadChannelPosition.MONO;
			curChan++;
		}
		for( int i = 1 ; i <= 8 ; ++i )
		{
			CHAN_NAMES[curChan] = "Input Note Channel " + i;
			CHAN_TYPES[curChan] = MadChannelType.NOTE;
			CHAN_DIRS[curChan] = MadChannelDirection.CONSUMER;
			CHAN_POSI[curChan] = MadChannelPosition.MONO;
			curChan++;
		}
		for( int o = 1 ; o <= 8 ; ++o )
		{
			CHAN_NAMES[curChan] = "Output Note Channel " + o;
			CHAN_TYPES[curChan] = MadChannelType.NOTE;
			CHAN_DIRS[curChan] = MadChannelDirection.PRODUCER;
			CHAN_POSI[curChan] = MadChannelPosition.MONO;
			curChan++;
		}
	}

	public final static String DEFINITION_ID = "rack_master_io";

	private final static String USER_VISIBLE_NAME = "Rack Master IO";

	private final static String CLASS_GROUP = MadClassificationService.INTERNAL_GROUP_ID;
	private final static String CLASS_NAME = "Rack MasterIO";
	private final static String CLASS_DESC = "Internal component used to represent the IO channels available inside a rack";

	public RackMasterIOMadDefinition( final RackMasterIOCreationContext creationContext,
			final MadClassificationService classificationService )
		throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				new MadNullLocklessQueueBridge<RackMasterIOMadInstance>(),
				ChanIndexes.NUM_CHANNELS.ordinal(),
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );
	}
}
