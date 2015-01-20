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

package test.uk.co.modularaudio.service.rendering;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.service.rendering.abstractunittest.AbstractGraphTest;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.service.rendering.vos.RenderingPlan;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.format.UnknownDataRateException;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class TestRootGraphAppGraphSubGraph extends AbstractGraphTest
{
	private static Log log = LogFactory.getLog( TestRootGraphAppGraphSubGraph.class.getName() );

	public void testCreatingGraphLevels()
		throws Exception
	{
		MadGraphInstance<?,?> subSubGraph = graphService.createNewParameterisedGraph( "Silly Sub Sub Graph",
				GraphType.SUB_GRAPH,
				4, 4,
				0, 0,
				0, 0 );

		setupSubSubGraph( subSubGraph );

		graphService.dumpGraph( subSubGraph );

		MadGraphInstance<?,?> subGraph = graphService.createNewParameterisedGraph( "Vol And Pan Subgraph",
				GraphType.SUB_GRAPH,
				2, 2,
				2, 2,
				0, 0 );

		graphService.addInstanceToGraphWithName( subGraph,  subSubGraph,  subSubGraph.getInstanceName());

		setupSubGraph( subGraph );

		graphService.dumpGraph( subGraph );

		MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Component Designer Application Graph",
				GraphType.APP_GRAPH,
				// Audio Ins/Outs
				4, 4,
				// CV Ins/Outs
				0, 0,
				// Midi Ins/Outs
				4, 4 );

		graphService.addInstanceToGraphWithName( appGraph,  subGraph,  subGraph.getInstanceName());

		setupAppGraph( appGraph, subGraph );

		graphService.dumpGraph( appGraph );

		MadGraphInstance<?,?> rootGraph = graphService.createNewRootGraph(  "Root graph" );
		graphService.addInstanceToGraphWithName( rootGraph,  appGraph,  appGraph.getInstanceName());
		setupRootGraph( rootGraph, appGraph );

		graphService.dumpGraph( rootGraph );

		RenderingPlan magic = null;


		DataRate dataRate = DataRate.getCDQuality();
		int channelBufferLength = 64;
		HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		long nanosLatency = 1000;
		int sfLatency = 10;
		HardwareIOChannelSettings planDataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosLatency, sfLatency );
		for( int i =0 ; i < 1 ; i++ )
		{
			if( i == 0 || i == 999 )
			{
				log.debug("Beginning render plan creation");
			}
			magic = renderingService.createRenderingPlan( rootGraph, planDataRateConfiguration, this );
			if( i == 0 || i == 999 )
			{
				log.debug("Finished render plan creation");
			}
		}
		renderingService.dumpRenderingPlan( magic );

		renderingService.destroyRenderingPlan( magic );

		graphService.destroyGraph( rootGraph, true, true );
	}

	private void setupSubSubGraph( MadGraphInstance<?,?> subSubGraph ) throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		MadInstance<?,?> fakeOscillatorInstance = createInstanceNamed( "Fake sub sub Oscillator" );
		graphService.addInstanceToGraphWithName(subSubGraph, fakeOscillatorInstance, fakeOscillatorInstance.getInstanceName());
	}

	private void setupSubGraph( MadGraphInstance<?,?> subGraph ) throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		MadInstance<?,?> fakeGainInstance = createInstanceNamed( "Fake sub graph Gain" );
		MadInstance<?,?> fakePanInstance = createInstanceNamed( "Fake sub graph Pan" );
		graphService.addInstanceToGraphWithName(subGraph, fakeGainInstance, fakeGainInstance.getInstanceName());
		graphService.addInstanceToGraphWithName(subGraph, fakePanInstance, fakePanInstance.getInstanceName());

		// Connecting gain in as the subgraph in 0
		MadChannelInstance graphInChannel = subGraph.getChannelInstanceByName( "Input Channel 1" );

		MadChannelInstance gainInChannel = fakeGainInstance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];
		graphService.exposeAudioInstanceChannelAsGraphChannel( subGraph, graphInChannel, gainInChannel );

		// Connecting gain output to the pan input 0
		MadChannelInstance gainOutChannel = fakeGainInstance.getChannelInstances()[ FadeInMadDefinition.PRODUCER ];
		MadChannelInstance panInChannel = fakePanInstance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];
		MadLink gainToPanLink = new MadLink( gainOutChannel, panInChannel );
		graphService.addLink( subGraph, gainToPanLink );

		// Connecting pan output to graph out 0
		MadChannelInstance panOutChannel = fakePanInstance.getChannelInstances()[ FadeInMadDefinition.PRODUCER ];
		MadChannelInstance graphOutChannel = subGraph.getChannelInstanceByName(  "Output Channel 1" );
		graphService.exposeAudioInstanceChannelAsGraphChannel(subGraph, graphOutChannel, panOutChannel);
	}

	private void setupAppGraph( MadGraphInstance<?,?> appGraph, MadInstance<?,?> volAndPanInstance ) throws RecordNotFoundException, DatastoreException, MAConstraintViolationException, UnknownDataRateException, MadProcessingException
	{
		MadInstance<?,?> fakeOscillatorInstance = createInstanceNamed( "Fake app graph Oscillator" );

		graphService.addInstanceToGraphWithName(appGraph, fakeOscillatorInstance, fakeOscillatorInstance.getInstanceName() );

		// Connect oscillator out to the first input channel of the vol and pan subgraph
		MadChannelInstance volAndPanInChannelInstance = volAndPanInstance.getChannelInstanceByName( "Input Channel 1" );

		MadChannelInstance oscOutChannel = fakeOscillatorInstance.getChannelInstances()[ FadeInMadDefinition.PRODUCER ];

		MadLink oscToVAPLink = new MadLink( oscOutChannel, volAndPanInChannelInstance );
		graphService.addLink( appGraph, oscToVAPLink );

		// Now expose the vol and pan output 1 as our graph output 1
		MadChannelInstance volAndPanOutChannelInstance = volAndPanInstance.getChannelInstanceByName( "Output Channel 1" );

		MadChannelInstance graphOutChannelInstance = appGraph.getChannelInstanceByName( "Output Channel 1" );

		graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph, graphOutChannelInstance, volAndPanOutChannelInstance );

		// And expose a fake component as input too
		MadInstance<?,?> fakeInputInstance = createInstanceNamed("AppGraphInputProcessor");
		graphService.addInstanceToGraphWithName( appGraph, fakeInputInstance, fakeInputInstance.getInstanceName() );

		MadChannelInstance ipInChannelInstance = fakeInputInstance.getChannelInstanceByName( "Input");

		MadChannelInstance graphInChannelInstance = appGraph.getChannelInstanceByName( "Input Channel 1");

		graphService.exposeAudioInstanceChannelAsGraphChannel(appGraph, graphInChannelInstance, ipInChannelInstance);
	}

	private void setupRootGraph( MadGraphInstance<?,?> rootGraph, MadGraphInstance<?,?> appGraphInstance ) throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException, UnknownDataRateException
	{
		// Add a fake master in and master out
		// and then connect them to the relevant channels in the app graph
		MadInstance<?,?> fakeMasterIn = createInstanceNamed( "Fake Master In" );
		MadInstance<?,?> fakeMasterOut = createInstanceNamed( "Fake Master Out" );
		rootGraph.addInstanceWithName(  fakeMasterIn, fakeMasterIn.getInstanceName() );
		rootGraph.addInstanceWithName(  fakeMasterOut, fakeMasterOut.getInstanceName() );

		MadChannelInstance masterInChannel = fakeMasterIn.getChannelInstances()[ FadeInMadDefinition.PRODUCER ];
		MadChannelInstance appInChannel = appGraphInstance.getChannelInstanceByName( "Input Channel 1" );

		MadLink masterInToAppLink = new MadLink( masterInChannel, appInChannel );
		graphService.addLink( rootGraph,  masterInToAppLink );

		// Connect master out to app out
		MadChannelInstance masterOutChannel = fakeMasterOut.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];
		MadChannelInstance appOutChannel = appGraphInstance.getChannelInstanceByName(  "Output Channel 1" );

		MadLink masterOutToAppLink = new MadLink( appOutChannel, masterOutChannel );
		graphService.addLink( rootGraph, masterOutToAppLink );
	}


	private MadInstance<?,?> createInstanceNamed( String name ) throws DatastoreException, RecordNotFoundException, MadProcessingException
	{
		MadDefinition<?,?> def = componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );
		Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
		MadInstance<?,?> retVal = componentService.createInstanceFromDefinition(  def, parameterValues, name );
		return retVal;
	}
}
