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
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphListener;

public class MadGraphServiceAppGraphTest extends AbstractGraphTest
{
	private static Log log = LogFactory.getLog( MadGraphServiceAppGraphTest.class.getName());

	private class FakeGraphListener implements MadGraphListener
	{

		@Override
		public void receiveGraphChangeSignal()
		{
			log.debug("Received graph change signal");
		}

		@Override
		public String getName()
		{
			return this.getClass().getSimpleName();
		}
	};

	public void testCreateNewAppGraph()
		throws Exception
	{
		log.debug("Starting create new app graph test");
		// 4 audio ins and outs
		final MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );
		log.debug("Got an app graph: " + appGraph.toString() );
		final MadChannelInstance[] channelIns = appGraph.getChannelInstances();
		assertTrue( channelIns.length == 8 );
		final Collection<MadInstance<?,?>> instanceIns = appGraph.getInstances();
		assertTrue( instanceIns.size() == 0 );
		final Collection<MadLink> links = appGraph.getLinks();
		assertTrue( links.size() == 0 );

		graphService.destroyGraph( appGraph, true, true );
	}

	public void testAddComponentToAppGraph()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );
		final MadDefinitionListModel definitions = componentService.listDefinitionsAvailable();
		assertTrue( definitions.getSize() > 0 );
		final MadDefinition<?,?> firstDefinition = definitions.getElementAt( 0 );
		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = componentService.createInstanceFromDefinition(  firstDefinition, emptyParameterMap, "Test instance" );
		graphService.addInstanceToGraphWithName(  appGraph, firstInstance, firstInstance.getInstanceName() );

		final Collection<MadInstance<?,?>> instanceIns = appGraph.getInstances();
		assertTrue( instanceIns.size() == 1 );

		graphService.destroyGraph( appGraph, true, true );
	}

	public void testLinkComponentsInAppGraph()
			throws Exception
	{
		final MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );

		final MadDefinition<?,?> definition = componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Test instance" );
		graphService.addInstanceToGraphWithName(  appGraph, firstInstance, firstInstance.getInstanceName() );

		final MadChannelInstance[] firstChannelInstances = firstInstance.getChannelInstances();
		final MadChannelInstance firstConsumerChannel = firstChannelInstances[ FadeInMadDefinition.CONSUMER ];

		final MadInstance<?,?> secondInstance = componentService.createInstanceFromDefinition(  definition,
				emptyParameterMap, "Test instance two");

		graphService.addInstanceToGraphWithName( appGraph, secondInstance, secondInstance.getInstanceName() );

		final MadChannelInstance[] secondChannelInstances = secondInstance.getChannelInstances();
		final MadChannelInstance secondProducerChannel = secondChannelInstances[ FadeInMadDefinition.PRODUCER ];

		final MadLink link = new MadLink( secondProducerChannel, firstConsumerChannel );

		graphService.addLink( appGraph,  link );

		assertTrue( appGraph.getLinks().size() == 1 );

		graphService.dumpGraph( appGraph );

		graphService.destroyGraph( appGraph, true, true );
	}

	public void testExposedComponentChannelInAppGraph()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );

		final FakeGraphListener fgl = new FakeGraphListener();

		graphService.addGraphListener( appGraph, fgl );

		final MadDefinition<?,?> definition = componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Test instance" );
		graphService.addInstanceToGraphWithName(  appGraph, firstInstance, firstInstance.getInstanceName() );

		final MadChannelInstance[] firstChannelInstances = firstInstance.getChannelInstances();
		final MadChannelInstance firstConsumerChannel = firstChannelInstances[ FadeInMadDefinition.CONSUMER ];

		final MadChannelInstance firstGraphConsumerChannel = appGraph.getChannelInstanceByName( "Input Channel 1" );

		// Now connect one of the component sinks to the graph sinks
		graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph,
				firstGraphConsumerChannel,
				firstConsumerChannel );

		graphService.dumpGraph( appGraph );

		graphService.removeGraphListener( appGraph, fgl );

		graphService.destroyGraph( appGraph, true, true );
	}

	public void testExposedSubGraphInAppGraph()
			throws Exception
	{
		final MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 4, 4, 4, 4 );

		final FakeGraphListener fgl = new FakeGraphListener();

		graphService.addGraphListener( appGraph, fgl );

		final MadGraphInstance<?,?> subGraph = graphService.createNewParameterisedGraph( "Test Sub Graph",
				GraphType.SUB_GRAPH, 4, 4, 4, 4, 4, 4 );

		// Now expose all channels we can
		graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( appGraph, subGraph, "SubGraphName", false );

		graphService.dumpGraph( appGraph );

		graphService.removeInstanceFromGraph( appGraph, subGraph );

		graphService.removeGraphListener( appGraph, fgl );

		graphService.destroyGraph( appGraph, true, true );
	}

	public void testMakeDualLinksFromProducerToConsumersInGraph()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 0, 0, 0, 0, 0, 0 );

		final MadDefinition<?,?> definition = componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> producerInstance = componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Producer Instance" );
		graphService.addInstanceToGraphWithName(  appGraph, producerInstance, producerInstance.getInstanceName() );

		final MadChannelInstance[] producerChannelInstances = producerInstance.getChannelInstances();
		final MadChannelInstance producerChannel = producerChannelInstances[ FadeInMadDefinition.PRODUCER ];

		final MadInstance<?,?> consumer1Instance = componentService.createInstanceFromDefinition( definition,  emptyParameterMap,  "Consumer 1 Instance" );
		graphService.addInstanceToGraphWithName(appGraph, consumer1Instance, consumer1Instance.getInstanceName() );
		final MadChannelInstance consumer1Channel = consumer1Instance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];

		final MadInstance<?,?> consumer2Instance = componentService.createInstanceFromDefinition( definition,  emptyParameterMap,  "Consumer 2 Instance" );
		graphService.addInstanceToGraphWithName(appGraph, consumer2Instance, consumer2Instance.getInstanceName() );
		final MadChannelInstance consumer2Channel = consumer2Instance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];

		final MadLink linkToOne = new MadLink(producerChannel, consumer1Channel);
		graphService.addLink(appGraph, linkToOne);

		graphService.dumpGraph(appGraph);

		final MadLink linkToTwo = new MadLink( producerChannel, consumer2Channel );
		graphService.addLink( appGraph, linkToTwo );

		graphService.dumpGraph(appGraph);

		graphService.destroyGraph( appGraph, true, true );
	}

	public void testAddAndRemoveLinksInGraphCheckLinks()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "LinkAddRemoveGraph",
				GraphType.APP_GRAPH,
				0, 0,
				0, 0,
				0, 0 );
		final MadDefinition<?,?> def = componentService.findDefinitionById( CrossFaderMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParams = new HashMap<MadParameterDefinition, String>();

		final MadInstance<?,?> pi = componentService.createInstanceFromDefinition( def, emptyParams, "Producer" );
		graphService.addInstanceToGraphWithName( appGraph, pi, "Producer" );

		final MadChannelInstance[] pcis = pi.getChannelInstances();
		final MadChannelInstance pci = pcis[ CrossFaderMadDefinition.PRODUCER_OUT_LEFT ];

		final MadInstance<?,?> ci = componentService.createInstanceFromDefinition( def, emptyParams, "Consumer" );
		graphService.addInstanceToGraphWithName( appGraph, ci, "Consumer" );

		final MadChannelInstance[] ccis = ci.getChannelInstances();
		final MadChannelInstance cci = ccis[ CrossFaderMadDefinition.CONSUMER_CHAN1_LEFT ];

		final MadLink link = new MadLink( pci, cci );

		appGraph.debugLinks();

		graphService.addLink( appGraph, link );

		appGraph.debugLinks();

		assertTrue( appGraph.getLinks().size() == 1 );
		assertTrue( appGraph.getProducerInstanceLinks( pi ).size() == 1 );
		assertTrue( appGraph.getProducerInstanceLinks( ci ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( pi ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( ci ).size() == 1 );

		graphService.dumpGraph( appGraph );

		graphService.deleteLink( appGraph, link );

		graphService.dumpGraph( appGraph );

		assertTrue( appGraph.getLinks().size() == 0 );
		assertTrue( appGraph.getProducerInstanceLinks( pi ).size() == 0 );
		assertTrue( appGraph.getProducerInstanceLinks( ci ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( pi ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( ci ).size() == 0 );

		graphService.destroyGraph( appGraph, true, true );
	}
}
