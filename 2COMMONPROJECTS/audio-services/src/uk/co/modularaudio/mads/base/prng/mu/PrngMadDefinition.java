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

package uk.co.modularaudio.mads.base.prng.mu;

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

public class PrngMadDefinition extends AbstractNonConfigurableMadDefinition<PrngMadDefinition, PrngMadInstance>
{
	// Indexes into the channels
	public final static int PRODUCER_CV_OUT = 0;
	public final static int NUM_CHANNELS = 1;

	public final static String DEFINITION_ID = "prng";

	private final static String USER_VISIBLE_NAME = "Pseudo Random Number Generator";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;
	private final static String CLASS_NAME = "Pseudeo Random Number Generator";
	private final static String CLASS_DESC = "Generate random values between -1.0 and 1.0";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] { "Output CV" };

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] { MadChannelType.CV };

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] { MadChannelDirection.PRODUCER };

	private final static MadChannelPosition[] CHAN_POSIS = new MadChannelPosition[] { MadChannelPosition.MONO};

	public PrngMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.ALPHA ),
				new MadNullLocklessQueueBridge<PrngMadInstance>(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSIS );

	}
}
