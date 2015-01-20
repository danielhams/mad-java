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


package test.uk.co.modularaudio.service.madgraph;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.service.madgraph.abstractunittest.AbstractGraphTest;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;

public class MadGraphServiceRootGraphTest extends AbstractGraphTest
{
	private static Log log = LogFactory.getLog( MadGraphServiceRootGraphTest.class.getName());
	
	public void testCreateNewRootGraph()
		throws Exception
	{
		log.debug("Starting create new root graph test");
		MadGraphInstance<?,?> rootGraph = graphService.createNewRootGraph(  "Test Root Graph" );
		log.debug("Got a root graph: " + rootGraph.toString() );
		MadChannelInstance[] channelIns = rootGraph.getChannelInstances();
		assertTrue( channelIns.length == 0 );
		Collection<MadInstance<?,?> > instanceIns = rootGraph.getInstances();
		assertTrue( instanceIns.size() == 0 );
		Collection<MadLink> links = rootGraph.getLinks();
		assertTrue( links.size() == 0 );
		
		graphService.destroyGraph( rootGraph, true, true );
	}
	
	public void testAddComponentToRootGraph()
		throws Exception
	{
		MadGraphInstance<?,?> rootGraph = graphService.createNewRootGraph(  "Test root graph" );
		MadDefinitionListModel definitions = componentService.listDefinitionsAvailable();
		assertTrue( definitions.getSize() > 0 );
		MadDefinition<?,?> firstDefinition = definitions.getElementAt( 0 );
		Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		MadInstance<?,?> firstInstance = componentService.createInstanceFromDefinition(  firstDefinition, emptyParameterMap, "Test instance" );
		graphService.addInstanceToGraphWithName(  rootGraph, firstInstance, firstInstance.getInstanceName() );
		
		Collection<MadInstance<?,?>> instanceIns = rootGraph.getInstances();
		assertTrue( instanceIns.size() == 1 );

		graphService.destroyGraph( rootGraph, true, true );
	}
}
