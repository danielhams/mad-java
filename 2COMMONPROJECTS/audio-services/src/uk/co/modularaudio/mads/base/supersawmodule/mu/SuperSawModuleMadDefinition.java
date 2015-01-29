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

package uk.co.modularaudio.mads.base.supersawmodule.mu;

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

public class SuperSawModuleMadDefinition extends AbstractNonConfigurableMadDefinition<SuperSawModuleMadDefinition, SuperSawModuleMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_CV_IN = 0;
	public final static int CONSUMER_CV_FREQ_IN = 1;
	public final static int CONSUMER_CV_MIX_IN = 2;

	public final static int PRODUCER_CV_OUT = 3;

	public final static int PRODUCER_CV_OSC1_FREQ = 4;
	public final static int PRODUCER_CV_OSC2_FREQ = 5;
	public final static int PRODUCER_CV_OSC3_FREQ = 6;
	public final static int PRODUCER_CV_OSC4_FREQ = 7;
	public final static int PRODUCER_CV_OSC5_FREQ = 8;
	public final static int PRODUCER_CV_OSC6_FREQ = 9;
	public final static int PRODUCER_CV_OSC7_FREQ = 10;

	public final static int PRODUCER_CV_OSC1_AMP = 11;
	public final static int PRODUCER_CV_OSC2_AMP = 12;
	public final static int PRODUCER_CV_OSC3_AMP = 13;
	public final static int PRODUCER_CV_OSC4_AMP = 14;
	public final static int PRODUCER_CV_OSC5_AMP = 15;
	public final static int PRODUCER_CV_OSC6_AMP = 16;
	public final static int PRODUCER_CV_OSC7_AMP = 17;

	public final static int NUM_CHANNELS = 18;

	public final static String DEFINITION_ID = "supersaw_module";

	private final static String USER_VISIBLE_NAME = "Supersaw Module";

	private final static String CLASS_GROUP = MadClassificationService.CONTROL_PROCESSING_GROUP_ID;
	private final static String CLASS_NAME = "Supersaw Module";
	private final static String CLASS_DESC = "A supersaw like mapping module";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] {
		"Input CV",
		"Input CV Frequency",
		"Input CV Mix",
		"Output CV",
		"Output CV Oscillator 1 Frequency",
		"Output CV Oscillator 2 Frequency",
		"Output CV Oscillator 3 Frequency",
		"Output CV Oscillator 4 Frequency",
		"Output CV Oscillator 5 Frequency",
		"Output CV Oscillator 6 Frequency",
		"Output CV Oscillator 7 Frequency",
		"Output CV Oscillator 1 Amp",
		"Output CV Oscillator 2 Amp",
		"Output CV Oscillator 3 Amp",
		"Output CV Oscillator 4 Amp",
		"Output CV Oscillator 5 Amp",
		"Output CV Oscillator 6 Amp",
		"Output CV Oscillator 7 Amp"
		};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] {
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
		MadChannelType.CV,
		MadChannelType.CV
		};

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] {
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
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER
		};

	private final static MadChannelPosition[] CHAN_POSIS = new MadChannelPosition[] {
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
		MadChannelPosition.MONO
		};

	public SuperSawModuleMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.ALPHA ),
				new MadNullLocklessQueueBridge<SuperSawModuleMadInstance>(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSIS );

	}
}
