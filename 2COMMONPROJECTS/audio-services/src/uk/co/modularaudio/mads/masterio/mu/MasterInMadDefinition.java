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

package uk.co.modularaudio.mads.masterio.mu;

import uk.co.modularaudio.mads.masterio.MasterIOComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadNullLocklessQueueBridge;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class MasterInMadDefinition extends AbstractNonConfigurableMadDefinition<MasterInMadDefinition, MasterInMadInstance>
{
	public final static String DEFINITION_ID = "master_in";

	private final static String USER_VISIBLE_NAME = "Master In";

	private final static String CLASS_GROUP = MadClassificationService.INTERNAL_GROUP_ID;
	private final static String CLASS_NAME = "Master In";
	private final static String CLASS_DESC = "The input component";

	public final static int NUM_AUDIO_CHANNELS = 8;
	public final static int NUM_NOTE_CHANNELS = 8;

	public final static IOMadConfiguration CHAN_CONFIG = new IOMadConfiguration( NUM_AUDIO_CHANNELS,
			NUM_NOTE_CHANNELS, MadChannelDirection.PRODUCER );

	public MasterInMadDefinition( final MasterIOComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID,
				USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				new MadNullLocklessQueueBridge<MasterInMadInstance>(),
				CHAN_CONFIG.getNumTotalChannels(),
				CHAN_CONFIG.getChannelNames(),
				CHAN_CONFIG.getChannelTypes(),
				CHAN_CONFIG.getChannelDirections(),
				CHAN_CONFIG.getChannelPositions() );
	}
}
