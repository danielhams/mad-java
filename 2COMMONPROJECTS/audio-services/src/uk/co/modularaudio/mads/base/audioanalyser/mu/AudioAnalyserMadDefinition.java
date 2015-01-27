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

package uk.co.modularaudio.mads.base.audioanalyser.mu;

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

public class AudioAnalyserMadDefinition extends AbstractNonConfigurableMadDefinition<AudioAnalyserMadDefinition,AudioAnalyserMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_AUDIO_SIGNAL0 = 0;
	public final static int NUM_CHANNELS = 1;

	public static final String DEFINITION_ID = "audio_analyser";

	private final static String USER_VISIBLE_NAME = "Audio Analyser";

	private final static String CLASSIFICATION_GROUP = MadClassificationService.SOUND_ANALYSIS_GROUP_ID;
	private final static String CLASSIFICATION_NAME = "Audio Analyser";
	private final static String CLASSIFICATION_DESC = "The Audio Analyser";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] {
		"Input Wave 0"
	};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] {
		MadChannelType.AUDIO
	};

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] {
		MadChannelDirection.CONSUMER,
	};

	private final static MadChannelPosition[] CHAN_POSI = new MadChannelPosition[] {
		MadChannelPosition.MONO
	};

	public AudioAnalyserMadDefinition( BaseComponentsCreationContext creationContext,
			MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASSIFICATION_GROUP ),
						DEFINITION_ID,
						CLASSIFICATION_NAME,
						CLASSIFICATION_DESC,
						ReleaseState.ALPHA ),
				new AudioAnalyserIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );

	}
}
