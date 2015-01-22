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

package uk.co.modularaudio.mads.internal.audiosystemtester.mu;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.modularaudio.mads.internal.InternalComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadNullLocklessQueueBridge;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AudioSystemTesterMadDefinition extends MadDefinition<AudioSystemTesterMadDefinition,AudioSystemTesterMadInstance>
{
	public static final Set<MadParameterDefinition> PARAM_DEFS;
	public static final MadParameterDefinition NUM_CHANNELS_PARAMETER = new MadParameterDefinition( "numchannels",
			"Num Output Channels" );

	static
	{
		PARAM_DEFS = new HashSet<MadParameterDefinition>();
		PARAM_DEFS.add(  NUM_CHANNELS_PARAMETER );
	}

	public final static String DEFINITION_ID = "audio_system_tester";
	private final static String USER_VISIBLE_NAME = "AudioSystemTester";
	private final static String CLASS_GROUP = MadClassificationService.INTERNAL_GROUP_ID;
	private final static String CLASS_DESC = "Produce a nicely attenuated sine wave output for audio io testing";
	private final static String CLASS_NAME = CLASS_DESC;

	public AudioSystemTesterMadDefinition( InternalComponentsCreationContext creationContext,
			MadClassificationService classificationService )
		throws DatastoreException, RecordNotFoundException
	{
		// Default super constructor is
		// super( name, isParametrable, parameterDefinitions );
		super( DEFINITION_ID, USER_VISIBLE_NAME, true,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				PARAM_DEFS,
				new MadNullLocklessQueueBridge<AudioSystemTesterMadInstance>() );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters( final Map<MadParameterDefinition, String> parameterValues )
		throws MadProcessingException
	{
		final AudioSystemTesterMadInstanceConfiguration ic = new AudioSystemTesterMadInstanceConfiguration( parameterValues );
		return ic.getChannelConfiguration();
	}
}
