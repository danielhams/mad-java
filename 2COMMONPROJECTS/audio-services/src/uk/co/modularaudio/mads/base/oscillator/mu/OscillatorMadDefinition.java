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

package uk.co.modularaudio.mads.base.oscillator.mu;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class OscillatorMadDefinition extends AbstractNonConfigurableMadDefinition<OscillatorMadDefinition, OscillatorMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_CV_FREQ= 0;
	public final static int PRODUCER_AUDIO_OUT = 1;
	public final static int PRODUCER_CV_OUT = 2;
	public final static int NUM_CHANNELS = 3;

	public final static String DEFINITION_ID = "oscillator";

	private final static String USER_VISIBLE_NAME = "Oscillator";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_SOURCE_GROUP_ID;
	private final static String CLASS_NAME = "Sound Source Oscillator";
	private final static String CLASS_DESC = "A sound producing oscillator";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] { "Input Frequency CV",
		"Output Wave",
		"Output CV" };

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] { MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.CV };

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] { MadChannelDirection.CONSUMER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER };

	private final static MadChannelPosition[] CHAN_POSIS = new MadChannelPosition[] { MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO };

	public OscillatorMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService )
		throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.ALPHA ),
				new OscillatorIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSIS );

	}
}
