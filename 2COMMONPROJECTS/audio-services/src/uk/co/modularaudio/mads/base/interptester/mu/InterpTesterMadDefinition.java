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

package uk.co.modularaudio.mads.base.interptester.mu;

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

public class InterpTesterMadDefinition extends AbstractNonConfigurableMadDefinition<InterpTesterMadDefinition, InterpTesterMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_AUDIO = 0;
	public final static int CONSUMER_CV = 1;

	public final static int PRODUCER_CV_RAW_NOTS = 2;
	public final static int PRODUCER_CV_HALFHANN_NOTS = 3;

	public final static int PRODUCER_CV_RAW = 4;
	public final static int PRODUCER_CV_LINEAR = 5;
	public final static int PRODUCER_CV_HALFHANN = 6;
	public final static int PRODUCER_CV_SPRINGDAMPER = 7;
	public final static int PRODUCER_CV_LOWPASS = 8;
	public final static int PRODUCER_CV_SPRINGDAMPER_DOUBLE = 9;

	public final static int PRODUCER_AUDIO = 10;
	public final static int PRODUCER_CV = 11;

	public final static int NUM_CHANNELS = 12;

	public static final String DEFINITION_ID = "interptester";

	private final static String USER_VISIBLE_NAME = "Control Interpolation Tester";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ROUTING_GROUP_ID;
	private final static String CLASS_NAME = "Control Interpolation Tester";
	private final static String CLASS_DESC = "A tester of control interpolation";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] {
		"Audio In",
		"CV In",
		"Raw Control NoTS CV Out",
		"Half Hann NoTS CV Out",
		"Raw Control CV Out",
		"Linear Interpolation CV Out",
		"Half Hann CV Out",
		"Spring Damper CV Out",
		"Low Pass CV Out",
		"Spring Damper Double CV Out",
		"Audio Out",
		"CV Out"
	};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] {
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
		MadChannelType.AUDIO,
		MadChannelType.CV
	};

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] {
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
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER
	};

	private final static MadChannelPosition[] CHAN_POSI = new MadChannelPosition[] {
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
		MadChannelPosition.MONO
	};

	public InterpTesterMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.BETA ),
				new InterpTesterIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );

	}
}
