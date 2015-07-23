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
import uk.co.modularaudio.util.timing.TestTimer;

public class MadGraphServiceAppGraphTest extends TestCase
{
	private static Log log = LogFactory.getLog( MadGraphServiceAppGraphTest.class.getName());

	private final GraphTestConfig gt = new GraphTestConfig();

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
		final MadGraphInstance<?,?> appGraph = gt.graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );
		log.debug("Got an app graph: " + appGraph.toString() );
		final MadChannelInstance[] channelIns = appGraph.getChannelInstances();
		assertTrue( channelIns.length == 8 );
		final Collection<MadInstance<?,?>> instanceIns = appGraph.getInstances();
		assertTrue( instanceIns.size() == 0 );
		final Collection<MadLink> links = appGraph.getLinks();
		assertTrue( links.size() == 0 );

		gt.graphService.destroyGraph( appGraph, true, true );
	}

	public void testAddComponentToAppGraph()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = gt.graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );
		final MadDefinitionListModel definitions = gt.componentService.listDefinitionsAvailable();
		assertTrue( definitions.getSize() > 0 );
		final MadDefinition<?,?> firstDefinition = definitions.getElementAt( 0 );
		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = gt.componentService.createInstanceFromDefinition(  firstDefinition, emptyParameterMap, "Test instance" );
		gt.graphService.addInstanceToGraphWithName(  appGraph, firstInstance, firstInstance.getInstanceName() );

		final Collection<MadInstance<?,?>> instanceIns = appGraph.getInstances();
		assertTrue( instanceIns.size() == 1 );

		gt.graphService.destroyGraph( appGraph, true, true );
	}

	public void testLinkComponentsInAppGraph()
			throws Exception
	{
		final MadGraphInstance<?,?> appGraph = gt.graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );

		final MadDefinition<?,?> definition = gt.componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = gt.componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Test instance" );
		gt.graphService.addInstanceToGraphWithName(  appGraph, firstInstance, firstInstance.getInstanceName() );

		final MadChannelInstance[] firstChannelInstances = firstInstance.getChannelInstances();
		final MadChannelInstance firstConsumerChannel = firstChannelInstances[ FadeInMadDefinition.CONSUMER ];

		final MadInstance<?,?> secondInstance = gt.componentService.createInstanceFromDefinition(  definition,
				emptyParameterMap, "Test instance two");

		gt.graphService.addInstanceToGraphWithName( appGraph, secondInstance, secondInstance.getInstanceName() );

		final MadChannelInstance[] secondChannelInstances = secondInstance.getChannelInstances();
		final MadChannelInstance secondProducerChannel = secondChannelInstances[ FadeInMadDefinition.PRODUCER ];

		final MadLink link = new MadLink( secondProducerChannel, firstConsumerChannel );

		gt.graphService.addLink( appGraph,  link );

		assertTrue( appGraph.getLinks().size() == 1 );

		gt.graphService.dumpGraph( appGraph );

		gt.graphService.destroyGraph( appGraph, true, true );
	}

	public void testExposedComponentChannelInAppGraph()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = gt.graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 0, 0, 0, 0 );

		final FakeGraphListener fgl = new FakeGraphListener();

		gt.graphService.addGraphListener( appGraph, fgl );

		final MadDefinition<?,?> definition = gt.componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = gt.componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Test instance" );
		gt.graphService.addInstanceToGraphWithName(  appGraph, firstInstance, firstInstance.getInstanceName() );

		final MadChannelInstance[] firstChannelInstances = firstInstance.getChannelInstances();
		final MadChannelInstance firstConsumerChannel = firstChannelInstances[ FadeInMadDefinition.CONSUMER ];

		final MadChannelInstance firstGraphConsumerChannel = appGraph.getChannelInstanceByName( "Input Channel 1" );

		// Now connect one of the component sinks to the graph sinks
		gt.graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph,
				firstGraphConsumerChannel,
				firstConsumerChannel );

		gt.graphService.dumpGraph( appGraph );

		gt.graphService.removeGraphListener( appGraph, fgl );

		gt.graphService.destroyGraph( appGraph, true, true );
	}

	public void testExposedSubGraphInAppGraph()
			throws Exception
	{
		final TestTimer tt = new TestTimer();

		tt.markBoundary( "Begin" );

		final MadGraphInstance<?,?> appGraph = gt.graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 4, 4, 4, 4, 4, 4 );

		tt.markBoundary( "Create app graph" );

		final FakeGraphListener fgl = new FakeGraphListener();

		gt.graphService.addGraphListener( appGraph, fgl );

		final MadGraphInstance<?,?> subGraph = gt.graphService.createNewParameterisedGraph( "Test Sub Graph",
				GraphType.SUB_GRAPH, 4, 4, 4, 4, 4, 4 );

		tt.markBoundary( "Create sub graph" );

		// Now expose all channels we can
		gt.graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( appGraph, subGraph, "SubGraphName", false );

		tt.markBoundary( "Add instance to graph map channels" );

		if( log.isTraceEnabled() )
		{
			gt.graphService.dumpGraph( appGraph );
			tt.markBoundary( "Dump graph" );
		}

		gt.graphService.removeInstanceFromGraph( appGraph, subGraph );

		tt.markBoundary( "Remove sub graph from app graph" );

		gt.graphService.removeGraphListener( appGraph, fgl );

		tt.markBoundary( "Remove graph listener" );

		gt.graphService.destroyGraph( subGraph, true, true );

		tt.markBoundary( "Destroy sub graph" );

		gt.graphService.destroyGraph( appGraph, true, true );

		tt.markBoundary( "Destroy app graph" );

		tt.logTimes( "EXPOSESUBGINAPPG", log );
	}

	public void testMakeDualLinksFromProducerToConsumersInGraph()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = gt.graphService.createNewParameterisedGraph( "Test App Graph",
				GraphType.APP_GRAPH, 0, 0, 0, 0, 0, 0 );

		final MadDefinition<?,?> definition = gt.componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> producerInstance = gt.componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Producer Instance" );
		gt.graphService.addInstanceToGraphWithName(  appGraph, producerInstance, producerInstance.getInstanceName() );

		final MadChannelInstance[] producerChannelInstances = producerInstance.getChannelInstances();
		final MadChannelInstance producerChannel = producerChannelInstances[ FadeInMadDefinition.PRODUCER ];

		final MadInstance<?,?> consumer1Instance = gt.componentService.createInstanceFromDefinition( definition,  emptyParameterMap,  "Consumer 1 Instance" );
		gt.graphService.addInstanceToGraphWithName(appGraph, consumer1Instance, consumer1Instance.getInstanceName() );
		final MadChannelInstance consumer1Channel = consumer1Instance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];

		final MadInstance<?,?> consumer2Instance = gt.componentService.createInstanceFromDefinition( definition,  emptyParameterMap,  "Consumer 2 Instance" );
		gt.graphService.addInstanceToGraphWithName(appGraph, consumer2Instance, consumer2Instance.getInstanceName() );
		final MadChannelInstance consumer2Channel = consumer2Instance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];

		final MadLink linkToOne = new MadLink(producerChannel, consumer1Channel);
		gt.graphService.addLink(appGraph, linkToOne);

		gt.graphService.dumpGraph(appGraph);

		final MadLink linkToTwo = new MadLink( producerChannel, consumer2Channel );
		gt.graphService.addLink( appGraph, linkToTwo );

		gt.graphService.dumpGraph(appGraph);

		gt.graphService.destroyGraph( appGraph, true, true );
	}

	public void testAddAndRemoveLinksInGraphCheckLinks()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = gt.graphService.createNewParameterisedGraph( "LinkAddRemoveGraph",
				GraphType.APP_GRAPH,
				0, 0,
				0, 0,
				0, 0 );
		final MadDefinition<?,?> def = gt.componentService.findDefinitionById( CrossFaderMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParams = new HashMap<MadParameterDefinition, String>();

		final MadInstance<?,?> pi = gt.componentService.createInstanceFromDefinition( def, emptyParams, "Producer" );
		gt.graphService.addInstanceToGraphWithName( appGraph, pi, "Producer" );

		final MadChannelInstance[] pcis = pi.getChannelInstances();
		final MadChannelInstance pci = pcis[ CrossFaderMadDefinition.PRODUCER_OUT_LEFT ];

		final MadInstance<?,?> ci = gt.componentService.createInstanceFromDefinition( def, emptyParams, "Consumer" );
		gt.graphService.addInstanceToGraphWithName( appGraph, ci, "Consumer" );

		final MadChannelInstance[] ccis = ci.getChannelInstances();
		final MadChannelInstance cci = ccis[ CrossFaderMadDefinition.CONSUMER_CHAN1_LEFT ];

		final MadLink link = new MadLink( pci, cci );

		appGraph.debugLinks();

		gt.graphService.addLink( appGraph, link );

		appGraph.debugLinks();

		assertTrue( appGraph.getLinks().size() == 1 );
		assertTrue( appGraph.getProducerInstanceLinks( pi ).size() == 1 );
		assertTrue( appGraph.getProducerInstanceLinks( ci ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( pi ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( ci ).size() == 1 );

		gt.graphService.dumpGraph( appGraph );

		gt.graphService.deleteLink( appGraph, link );

		gt.graphService.dumpGraph( appGraph );

		assertTrue( appGraph.getLinks().size() == 0 );
		assertTrue( appGraph.getProducerInstanceLinks( pi ).size() == 0 );
		assertTrue( appGraph.getProducerInstanceLinks( ci ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( pi ).size() == 0 );
		assertTrue( appGraph.getConsumerInstanceLinks( ci ).size() == 0 );

		gt.graphService.destroyGraph( appGraph, true, true );
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
