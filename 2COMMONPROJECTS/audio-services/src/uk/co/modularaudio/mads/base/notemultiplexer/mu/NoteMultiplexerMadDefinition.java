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

package uk.co.modularaudio.mads.base.notemultiplexer.mu;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class NoteMultiplexerMadDefinition extends MadDefinition<NoteMultiplexerMadDefinition,NoteMultiplexerMadInstance>
{
	protected static Set<MadParameterDefinition> parameterDefinitions;
	protected static final MadParameterDefinition NUM_CHANNELS_PARAMETER = new MadParameterDefinition( "numchannels", "Num Channels" );

	static
	{
		parameterDefinitions = new HashSet<MadParameterDefinition>();
		parameterDefinitions.add(  NUM_CHANNELS_PARAMETER );
	}

	private static String id = "note_multiplexer";
	private static String name = "Note Multiplexer";
	private static String description = "Multiplex incoming notes to multiple polyphonic channels";
	private static String classificationGroup = MadClassificationService.SOUND_ROUTING_GROUP_ID;
	private static String classificationId = id;
	private static String classificationName = description;
	private static String classificationDescription = description;

	public NoteMultiplexerMadDefinition( BaseComponentsCreationContext creationContext,
			MadClassificationService classificationService ) throws DatastoreException, RecordNotFoundException
	{
		// Default super constructor is
		// super( name, isParametrable, parameterDefinitions );
		super( id, name, true,
				new MadClassification( classificationService.findGroupById( classificationGroup ),
						classificationId,
						classificationName,
						classificationDescription,
						ReleaseState.ALPHA ),
				parameterDefinitions,
				new NoteMultiplexerIOQueueBridge() );
	}

	@Override
	public MadChannelConfiguration getChannelConfigurationForParameters( Map<MadParameterDefinition, String> parameterValues )
		throws MadProcessingException
	{
		NoteMultiplexerMadInstanceConfiguration ic = new NoteMultiplexerMadInstanceConfiguration( parameterValues );
		return ic.getChannelConfiguration();
	}
}
