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

package uk.co.modularaudio.mads.base.djeq2.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.BaseMadDefinition;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class DJEQ2MadDefinition
	extends AbstractNonConfigurableMadDefinition<DJEQ2MadDefinition, DJEQ2MadInstance>
	implements BaseMadDefinition
{
	// Indexes into the channels
	public final static int CONSUMER_WAVE_LEFT = 0;
	public final static int CONSUMER_WAVE_RIGHT = 1;
	public final static int PRODUCER_WAVE_LEFT = 2;
	public final static int PRODUCER_WAVE_RIGHT = 3;
	public final static int PRODUCER_LOW_LEFT = 4;
	public final static int PRODUCER_LOW_RIGHT = 5;
	public final static int PRODUCER_HIGH_LEFT = 6;
	public final static int PRODUCER_HIGH_RIGHT = 7;

	public final static int NUM_CHANNELS = PRODUCER_HIGH_RIGHT+1;

	public static final String DEFINITION_ID = "djeq2";

	private final static String USER_VISIBLE_NAME = "DJ EQ 2 Way";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;
	private final static String CLASS_NAME = "DJ EQ 2 Way";
	private final static String CLASS_DESC = "A DJ equaliser unit with 2 bands and volume fader";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] {
		"Input Wave Left",
		"Input Wave Right",
		"Output Wave Left",
		"Output Wave Right",
		"Output Low Left",
		"Output Low Rigth",
		"Output High Left",
		"Output High Rigth",
		};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] {
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO
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
	};

	private final static MadChannelPosition[] CHAN_POSI = new MadChannelPosition[] {
		MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT,
		MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT,
		MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT,
		MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT
	};

	private final BaseComponentsCreationContext creationContext;

	public DJEQ2MadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				new DJEQ2IOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );
		this.creationContext = creationContext;
	}

	@Override
	public MadInstance<?, ?> createInstance( final Map<MadParameterDefinition, String> parameterValues, final String instanceName )
			throws MadProcessingException
	{
		return new DJEQ2MadInstance(
				creationContext,
				instanceName,
				this,
				parameterValues,
				getChannelConfigurationForParameters( parameterValues ) );
	}
}
