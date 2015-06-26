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

package uk.co.modularaudio.mads.base.dctrap.mu;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
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

public class DCTrapMadDefinition extends AbstractNonConfigurableMadDefinition<DCTrapMadDefinition, DCTrapMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_IN_WAVE_LEFT = 0;
	public final static int CONSUMER_IN_WAVE_RIGHT = 1;
	public final static int PRODUCER_OUT_WAVE_LEFT = 2;
	public final static int PRODUCER_OUT_WAVE_RIGHT = 3;
	public final static int NUM_CHANNELS = 4;

	public final static String DEFINITION_ID = "dc_trap";

	private final static String USER_VISIBLE_NAME = "DC Trap";

	private final static String CLASSIFICATION_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;
	private final static String CLASSIFICATION_ID = "dc_trap";
	private final static String CLASSIFICATION_NAME = "DC Trap";
	private final static String CLASSIFICATION_DESC = "A stereo trap to eliminate DC offset";

	// These must match the channel indexes given above
	private final static String[] CHANNEL_NAMES = new String[] {
		"Input Wave Left",
		"Input Wave Right",
		"Output Wave Left",
		"Output Wave Right"
	};

	private final static MadChannelType[] CHANNEL_TYPES = new MadChannelType[] {
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO
	};

	private final static MadChannelDirection[] CHANNEL_DIRS = new MadChannelDirection[] {
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER
	};

	private final static MadChannelPosition[] CHANNEL_POSI = new MadChannelPosition[] {
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO
	};

	public DCTrapMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService mcs ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( mcs.findGroupById( CLASSIFICATION_GROUP ),
						CLASSIFICATION_ID,
						CLASSIFICATION_NAME,
						CLASSIFICATION_DESC,
						ReleaseState.RELEASED ),
				new MadNullLocklessQueueBridge<DCTrapMadInstance>(),
				NUM_CHANNELS,
				CHANNEL_NAMES,
				CHANNEL_TYPES,
				CHANNEL_DIRS,
				CHANNEL_POSI );

	}
}
