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

package uk.co.modularaudio.mads.internal.blockingwritering.mu;

import uk.co.modularaudio.mads.internal.InternalComponentsCreationContext;
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

public class BlockingWriteRingMadDefinition extends AbstractNonConfigurableMadDefinition<BlockingWriteRingMadDefinition,BlockingWriteRingMadInstance>
{
	// Indexes into the channels
	public final static int PRODUCER_LEFT = 0;
	public final static int PRODUCER_RIGHT = 1;
	public final static int NUM_CHANNELS = 2;

	public final static String DEFINITION_ID = "blocking_write_ring";

	private final static String USER_VISIBLE_NAME = "Blocking Write Ring";

	private final static String CLASS_GROUP = MadClassificationService.INTERNAL_GROUP_ID;
	private final static String CLASS_NAME = "Blocking Write Ring";
	private final static String CLASS_DESC = "Testing component allowing blocking writes from a thread outside the DSP thread.";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] { "Output Left", "Output Right" };

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] { MadChannelType.AUDIO,
		MadChannelType.AUDIO };

	private final static MadChannelDirection[] CHAN_DIRECTIONS = new MadChannelDirection[] { MadChannelDirection.PRODUCER,
		 MadChannelDirection.PRODUCER };

	private final static MadChannelPosition[] CHAN_POSI = new MadChannelPosition[] { MadChannelPosition.MONO,
		MadChannelPosition.MONO };

	public BlockingWriteRingMadDefinition( final InternalComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.ALPHA ),
				new MadNullLocklessQueueBridge<BlockingWriteRingMadInstance>(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRECTIONS,
				CHAN_POSI );

	}
}
