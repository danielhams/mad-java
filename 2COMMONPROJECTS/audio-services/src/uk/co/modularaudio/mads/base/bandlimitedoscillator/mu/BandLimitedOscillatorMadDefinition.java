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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.mu;

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

public class BandLimitedOscillatorMadDefinition extends AbstractNonConfigurableMadDefinition<BandLimitedOscillatorMadDefinition, BandLimitedOscillatorMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_CV_FREQ= 0;
	public final static int CONSUMER_CV_TRIGGER = 1;
	public final static int CONSUMER_CV_PHASE = 2;
	public final static int CONSUMER_CV_PULSEWIDTH = 3;
	public final static int PRODUCER_AUDIO_OUT = 4;
	public final static int PRODUCER_CV_OUT = 5;
	public final static int PRODUCER_PHASE_OUT = 6;
	public final static int NUM_CHANNELS = 7;

	public final static String DEFINITION_ID = "band_limited_oscillator";

	private final static String USER_VISIBLE_NAME = "Band Limited Oscillator";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_SOURCE_GROUP_ID;
	private final static String CLASS_NAME = "Sound Source Band Limited Oscillator";
	private final static String CLASS_DESC = "A sound producing band limited oscillator";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] { "Input Frequency CV",
		"Input Trigger CV",
		"Input Phase CV",
		"Input Pulse Width CV",
		"Output Wave",
		"Output CV",
		"Output Phase CV" };

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] { MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.CV,
		MadChannelType.CV };

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] { MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER };

	private final static MadChannelPosition[] CHAN_POSI = new MadChannelPosition[] { MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO };

	public BandLimitedOscillatorMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService )
		throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.ALPHA ),
				new BandLimitedOscillatorIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );

	}
}
