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

package uk.co.modularaudio.mads.base.oscilloscope.mu;

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

public class OscilloscopeMadDefinition extends AbstractNonConfigurableMadDefinition<OscilloscopeMadDefinition,OscilloscopeMadInstance>
{
	// Indexes into the channels
	public final static int CONSUMER_CV_TRIGGER = 0;
	public final static int CONSUMER_AUDIO_SIGNAL0 = 1;
	public final static int CONSUMER_CV_SIGNAL0 = 2;
	public final static int CONSUMER_AUDIO_SIGNAL1 = 3;
	public final static int CONSUMER_CV_SIGNAL1 = 4;
	public final static int NUM_CHANNELS = 5;

	private final static String definitionId = "oscilloscope";

	private final static String userVisibleName = "Oscilloscope";

	private final static String classificationGroup = MadClassificationService.SOUND_ANALYSIS_GROUP_ID;
	private final static String classificationId = "oscilloscope";
	private final static String classificationName = "Oscilloscope";
	private final static String classificationDescription = "An oscilloscope for analysing sound";

	// These must match the channel indexes given above
	private final static String[] channelNames = new String[] {
		"Input Trigger",
		"Input Wave 0",
		"Input CV 0",
		"Input Wave 1",
		"Input CV 1"};

	private final static MadChannelType[] channelTypes = new MadChannelType[] {
		MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.CV };

	private final static MadChannelDirection[] channelDirections = new MadChannelDirection[] { MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER };

	private final static MadChannelPosition[] channelPositions = new MadChannelPosition[] { MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO };

	public OscilloscopeMadDefinition( BaseComponentsCreationContext creationContext,
			MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( definitionId, userVisibleName,
				new MadClassification( classificationService.findGroupById( classificationGroup ),
						classificationId,
						classificationName,
						classificationDescription,
						ReleaseState.ALPHA ),
				new OscilloscopeIOQueueBridge(),
				NUM_CHANNELS,
				channelNames,
				channelTypes,
				channelDirections,
				channelPositions );

	}
}
