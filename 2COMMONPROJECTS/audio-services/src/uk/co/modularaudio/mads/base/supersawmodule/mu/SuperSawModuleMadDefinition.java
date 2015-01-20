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

	private final static String definitionId = "supersaw_module";

	private final static String userVisibleName = "Supersaw Module";

	private final static String classificationGroup = MadClassificationService.CONTROL_PROCESSING_GROUP_ID;
	private final static String classificationId = "supersaw_module";
	private final static String classificationName = "Supersaw Module";
	private final static String classificationDescription = "A supersaw like mapping module";

	// These must match the channel indexes given above
	private final static String[] channelNames = new String[] {
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

	private final static MadChannelType[] channelTypes = new MadChannelType[] {
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

	private final static MadChannelDirection[] channelDirections = new MadChannelDirection[] {
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

	private final static MadChannelPosition[] channelPositions = new MadChannelPosition[] {
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

	public SuperSawModuleMadDefinition( BaseComponentsCreationContext creationContext,
			MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( definitionId, userVisibleName,
				new MadClassification( classificationService.findGroupById( classificationGroup ),
						classificationId,
						classificationName,
						classificationDescription,
						ReleaseState.ALPHA ),
				new SuperSawModuleIOQueueBridge(),
				NUM_CHANNELS,
				channelNames,
				channelTypes,
				channelDirections,
				channelPositions );

	}
}
