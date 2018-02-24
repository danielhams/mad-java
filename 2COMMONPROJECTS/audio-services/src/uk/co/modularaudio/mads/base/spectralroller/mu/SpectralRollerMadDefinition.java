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

package uk.co.modularaudio.mads.base.spectralroller.mu;

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

public class SpectralRollerMadDefinition
	extends AbstractNonConfigurableMadDefinition<SpectralRollerMadDefinition,SpectralRollerMadInstance>
	implements BaseMadDefinition
{
	// Indexes into the channels
	public final static int CONSUMER_AUDIO_SIGNAL0 = 0;
	public final static int NUM_CHANNELS = 1;

	public static final String DEFINITION_ID = "spectral_roller";

	private final static String USER_VISIBLE_NAME = "Spectral Roller";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ANALYSIS_GROUP_ID;
	private final static String CLASS_NAME = "Spectral Roller";
	private final static String CLASS_DESC = "Show the frequency of a signal on a rolling screen";

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

	private final static MadChannelPosition[] CHAN_POSIS = new MadChannelPosition[] {
		MadChannelPosition.MONO
	};

	private final BaseComponentsCreationContext creationContext;

	public SpectralRollerMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				new SpectralRollerIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSIS );
		this.creationContext = creationContext;
	}

	@Override
	public MadInstance<?, ?> createInstance( final Map<MadParameterDefinition, String> parameterValues, final String instanceName )
			throws MadProcessingException
	{
		return new SpectralRollerMadInstance(
				creationContext,
				instanceName,
				this,
				parameterValues,
				getChannelConfigurationForParameters( parameterValues ) );
	}
}