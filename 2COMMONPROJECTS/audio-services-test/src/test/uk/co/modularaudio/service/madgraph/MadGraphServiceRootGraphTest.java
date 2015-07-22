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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.service.madgraph.config.GraphTestConfig;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;

public class MadGraphServiceRootGraphTest extends TestCase
{
	private static Log log = LogFactory.getLog( MadGraphServiceRootGraphTest.class.getName());

	private final GraphTestConfig gt = new GraphTestConfig();

	public void testCreateNewRootGraph()
		throws Exception
	{
		log.debug("Starting create new root graph test");
		final MadGraphInstance<?,?> rootGraph = gt.graphService.createNewRootGraph(  "Test Root Graph" );
		log.debug("Got a root graph: " + rootGraph.toString() );
		final MadChannelInstance[] channelIns = rootGraph.getChannelInstances();
		assertTrue( channelIns.length == 0 );
		final Collection<MadInstance<?,?> > instanceIns = rootGraph.getInstances();
		assertTrue( instanceIns.size() == 0 );
		final Collection<MadLink> links = rootGraph.getLinks();
		assertTrue( links.size() == 0 );

		gt.graphService.destroyGraph( rootGraph, true, true );
	}

	public void testAddComponentToRootGraph()
		throws Exception
	{
		final MadGraphInstance<?,?> rootGraph = gt.graphService.createNewRootGraph(  "Test root graph" );
		final MadDefinitionListModel definitions = gt.componentService.listDefinitionsAvailable();
		assertTrue( definitions.getSize() > 0 );
		final MadDefinition<?,?> firstDefinition = definitions.getElementAt( 0 );
		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = gt.componentService.createInstanceFromDefinition(  firstDefinition, emptyParameterMap, "Test instance" );
		gt.graphService.addInstanceToGraphWithName(  rootGraph, firstInstance, firstInstance.getInstanceName() );

		final Collection<MadInstance<?,?>> instanceIns = rootGraph.getInstances();
		assertTrue( instanceIns.size() == 1 );

		gt.graphService.destroyGraph( rootGraph, true, true );
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		gt.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		gt.tearDown();
		super.tearDown();
	}
}
