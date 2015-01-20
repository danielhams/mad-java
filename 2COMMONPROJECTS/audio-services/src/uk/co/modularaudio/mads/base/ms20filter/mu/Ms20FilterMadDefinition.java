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

package uk.co.modularaudio.mads.base.ms20filter.mu;

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

public class Ms20FilterMadDefinition extends AbstractNonConfigurableMadDefinition<Ms20FilterMadDefinition,Ms20FilterMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_IN_LEFT = 0;
	public final static int CONSUMER_IN_RIGHT = 1;
	public final static int CONSUMER_IN_CV_FREQUENCY = 2;
	public final static int CONSUMER_IN_CV_RESONANCE = 3;
	public final static int CONSUMER_IN_CV_THRESHOLD = 4;
	public final static int PRODUCER_OUT_LEFT = 5;
	public final static int PRODUCER_OUT_RIGHT = 6;
	public final static int NUM_CHANNELS = 7;

	private final static String definitionId = "ms20_filter";

	private final static String userVisibleName = "Ms20 Frequency Filter";

	private final static String classificationGroup = MadClassificationService.SOUND_PROCESSING_GROUP_ID;
	private final static String classificationId = "ms20_filter";
	private final static String classificationName = "Ms20 Frequency Filter";
	private final static String classificationDescription = "Simulation of the MS20 resonant filter to remove or allow frequencies";

	// These must match the channel indexes given above
	private final static String[] channelNames = new String[] { "Input Wave Left",
		"Input Wave Right",
		"Input CV Frequency",
		"Input CV Resonance",
		"Input CV Threshold",
		"Output Wave Left",
		"Output Wave Right" };

	private final static MadChannelType[] channelTypes = new MadChannelType[] { MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO };

	private final static MadChannelDirection[] channelDirections = new MadChannelDirection[] { MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER };

	private final static MadChannelPosition[] channelPositions = new MadChannelPosition[] { MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT };

	public Ms20FilterMadDefinition( BaseComponentsCreationContext creationContext,
			MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( definitionId, userVisibleName,
				new MadClassification( classificationService.findGroupById( classificationGroup ),
						classificationId,
						classificationName,
						classificationDescription,
						ReleaseState.ALPHA ),
				new Ms20FilterIOQueueBridge(),
				NUM_CHANNELS,
				channelNames,
				channelTypes,
				channelDirections,
				channelPositions );

	}
}
