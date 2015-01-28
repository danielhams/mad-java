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

package uk.co.modularaudio.mads.base.mixer.mu;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class MixerMadDefinition extends MadDefinition<MixerMadDefinition, MixerMadInstance>
{
	public static final String DEFINITION_ID = "mixer";

	protected final static Set<MadParameterDefinition> PARAM_DEFS;

	protected final static MadParameterDefinition NUM_LANES_DEF = new MadParameterDefinition( "NumLanes", "Number Of Mixer Lanes" );

	static
	{
		PARAM_DEFS = new HashSet<MadParameterDefinition>();
		PARAM_DEFS.add( NUM_LANES_DEF );
	}

	private final static String USER_VISIBLE_NAME = "Mixer";
	private final static String CLASS_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;
	private final static String CLASS_DESC = "Mixer for up to 8 lanes of mono/stereo input into one stereo output";

	public MixerMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws DatastoreException, RecordNotFoundException
	{
		// Default super constructor is
		// super( name, isParametrable, parameterDefinitions );
		super( DEFINITION_ID, USER_VISIBLE_NAME, true,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						USER_VISIBLE_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				PARAM_DEFS,
				new MixerIOQueueBridge() );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters( final Map<MadParameterDefinition, String> parameterValues )
		throws MadProcessingException
	{
		final MixerMadInstanceConfiguration ic = new MixerMadInstanceConfiguration( parameterValues );
		return ic.getChannelConfiguration();
	}}
