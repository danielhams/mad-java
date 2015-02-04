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

package uk.co.modularaudio.service.apprendering.util.structure;

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadDefinition;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AudioSystemTestGraphCreator
{
	public static MadGraphInstance<?,?> createAudioSystemTestGraph( final MadGraphService graphService, final MadComponentService componentService )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		final MadGraphInstance<?, ?> graph = graphService.createNewParameterisedGraph( "AudioSystemTestGraph",
				GraphType.APP_GRAPH,
				16, 16,
				16, 16,
				16, 16 );

		final MadDefinition<?, ?> audioSystemTesterDefinition = componentService.findDefinitionById( AudioSystemTesterMadDefinition.DEFINITION_ID );
		final Map<MadParameterDefinition, String> testerParameterValues = new HashMap<MadParameterDefinition,String>();
		testerParameterValues.put( AudioSystemTesterMadDefinition.NUM_CHANNELS_PARAMETER, "16" );

		final MadInstance<?, ?> audioSystemTesterInstance = componentService.createInstanceFromDefinition( audioSystemTesterDefinition,
				testerParameterValues,
				"Audio System Tester" );

		// Now expose the instance channels as graph channels
		graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( graph, audioSystemTesterInstance,
				"Audio System Tester", false );

		return graph;
	}
}
