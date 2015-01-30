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
import uk.co.modularaudio.service.rendering.RenderingPlan;
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

public class TestGraphSubGraph extends AbstractGraphTest
{
	private static Log log = LogFactory.getLog( TestGraphSubGraph.class.getName() );

	public void testCreatingGraphLevels()
		throws Exception
	{
		MadGraphInstance<?,?> appGraph = graphService.createNewParameterisedGraph( "Component Designer Application Graph",
				GraphType.APP_GRAPH,
				// Audio Ins/Outs
				1, 1,
				// CV Ins/Outs
				0, 0,
				// Midi Ins/Outs
				0, 0 );

		setupAppGraph( appGraph );

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

	private void setupAppGraph( MadGraphInstance<?,?> appGraph ) throws RecordNotFoundException, DatastoreException, MAConstraintViolationException, UnknownDataRateException, MadProcessingException
	{
		MadChannelInstance graphInChannelInstance = appGraph.getChannelInstanceByName( "Input Channel 1");

		MadInstance<?,?> fakeIOInstance = createInstanceNamed( "AppGraphInputOutputProcessor" );
		graphService.addInstanceToGraphWithName(appGraph, fakeIOInstance, fakeIOInstance.getInstanceName() );

		MadChannelInstance ioOutChannelInstance = fakeIOInstance.getChannelInstanceByName("Output");
		MadChannelInstance graphOutChannelInstance = appGraph.getChannelInstanceByName("Output Channel 1");

		graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph, graphOutChannelInstance, ioOutChannelInstance );

		MadChannelInstance ioInChannelInstance = fakeIOInstance.getChannelInstanceByName("Input");

		graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph, graphInChannelInstance, ioInChannelInstance );

		// And expose a fake component as input too
		MadInstance<?,?> fakeInputInstance = createInstanceNamed("AppGraphInputOnlyProcessor");
		graphService.addInstanceToGraphWithName( appGraph, fakeInputInstance, fakeInputInstance.getInstanceName() );

		MadChannelInstance ipInChannelInstance = fakeInputInstance.getChannelInstanceByName( "Input");

		graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph, graphInChannelInstance, ipInChannelInstance );

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
