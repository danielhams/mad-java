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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.service.rendering.config.RenderingTestConfig;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
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

public class TestGraphSubGraph extends TestCase
{
	private static Log log = LogFactory.getLog( TestGraphSubGraph.class.getName() );

	private final RenderingTestConfig rt = new RenderingTestConfig();

	public void testCreatingGraphLevels()
		throws Exception
	{
		final MadGraphInstance<?,?> appGraph = rt.graphService.createNewParameterisedGraph( "Component Designer Application Graph",
				GraphType.APP_GRAPH,
				// Audio Ins/Outs
				1, 1,
				// CV Ins/Outs
				0, 0,
				// Midi Ins/Outs
				0, 0 );

		setupAppGraph( appGraph );

		rt.graphService.dumpGraph( appGraph );

		final MadGraphInstance<?,?> rootGraph = rt.graphService.createNewRootGraph(  "Root graph" );
		rt.graphService.addInstanceToGraphWithName( rootGraph,  appGraph,  appGraph.getInstanceName());
		setupRootGraph( rootGraph, appGraph );

		rt.graphService.dumpGraph( rootGraph );

		RenderingPlan magic = null;


		final DataRate dataRate = DataRate.CD_QUALITY;
		final int channelBufferLength = 64;
		final HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		final long nanosLatency = 1000;
		final int sfLatency = 10;
		final HardwareIOChannelSettings planDataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosLatency, sfLatency );
		for( int i =0 ; i < 1 ; i++ )
		{
			if( i == 0 || i == 999 )
			{
				log.debug("Beginning render plan creation");
			}
			magic = rt.renderingPlanService.createRenderingPlan( rootGraph, planDataRateConfiguration, rt );
			if( i == 0 || i == 999 )
			{
				log.debug("Finished render plan creation");
			}
		}
		rt.renderingPlanService.dumpRenderingPlan( magic );

		rt.renderingPlanService.destroyRenderingPlan( magic );

		rt.graphService.destroyGraph( rootGraph, true, true );
	}

	private void setupAppGraph( final MadGraphInstance<?,?> appGraph ) throws RecordNotFoundException, DatastoreException, MAConstraintViolationException, UnknownDataRateException, MadProcessingException
	{
		final MadChannelInstance graphInChannelInstance = appGraph.getChannelInstanceByName( "Input Channel 1");

		final MadInstance<?,?> fakeIOInstance = createInstanceNamed( "AppGraphInputOutputProcessor" );
		rt.graphService.addInstanceToGraphWithName(appGraph, fakeIOInstance, fakeIOInstance.getInstanceName() );

		final MadChannelInstance ioOutChannelInstance = fakeIOInstance.getChannelInstanceByName("Output");
		final MadChannelInstance graphOutChannelInstance = appGraph.getChannelInstanceByName("Output Channel 1");

		rt.graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph, graphOutChannelInstance, ioOutChannelInstance );

		final MadChannelInstance ioInChannelInstance = fakeIOInstance.getChannelInstanceByName("Input");

		rt.graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph, graphInChannelInstance, ioInChannelInstance );

		// And expose a fake component as input too
		final MadInstance<?,?> fakeInputInstance = createInstanceNamed("AppGraphInputOnlyProcessor");
		rt.graphService.addInstanceToGraphWithName( appGraph, fakeInputInstance, fakeInputInstance.getInstanceName() );

		final MadChannelInstance ipInChannelInstance = fakeInputInstance.getChannelInstanceByName( "Input");

		rt.graphService.exposeAudioInstanceChannelAsGraphChannel( appGraph, graphInChannelInstance, ipInChannelInstance );

	}

	private void setupRootGraph( final MadGraphInstance<?,?> rootGraph, final MadGraphInstance<?,?> appGraphInstance ) throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException, UnknownDataRateException
	{
		// Add a fake master in and master out
		// and then connect them to the relevant channels in the app graph
		final MadInstance<?,?> fakeMasterIn = createInstanceNamed( "Fake Master In" );
		final MadInstance<?,?> fakeMasterOut = createInstanceNamed( "Fake Master Out" );
		rootGraph.addInstanceWithName(  fakeMasterIn, fakeMasterIn.getInstanceName() );
		rootGraph.addInstanceWithName(  fakeMasterOut, fakeMasterOut.getInstanceName() );

		final MadChannelInstance masterInChannel = fakeMasterIn.getChannelInstances()[ FadeInMadDefinition.PRODUCER ];
		final MadChannelInstance appInChannel = appGraphInstance.getChannelInstanceByName( "Input Channel 1" );

		final MadLink masterInToAppLink = new MadLink( masterInChannel, appInChannel );
		rt.graphService.addLink( rootGraph,  masterInToAppLink );

		// Connect master out to app out
		final MadChannelInstance masterOutChannel = fakeMasterOut.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];
		final MadChannelInstance appOutChannel = appGraphInstance.getChannelInstanceByName(  "Output Channel 1" );

		final MadLink masterOutToAppLink = new MadLink( appOutChannel, masterOutChannel );
		rt.graphService.addLink( rootGraph, masterOutToAppLink );
	}


	private MadInstance<?,?> createInstanceNamed( final String name ) throws DatastoreException, RecordNotFoundException, MadProcessingException
	{
		final MadDefinition<?,?> def = rt.componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );
		final Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> retVal = rt.componentService.createInstanceFromDefinition(  def, parameterValues, name );
		return retVal;
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		rt.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		rt.tearDown();
		super.tearDown();
	}
}
