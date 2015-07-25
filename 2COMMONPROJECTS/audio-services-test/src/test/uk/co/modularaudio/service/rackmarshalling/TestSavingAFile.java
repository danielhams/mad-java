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

package test.uk.co.modularaudio.service.rackmarshalling;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.service.rackmarshalling.config.RackMarshallingTestConfig;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;

public class TestSavingAFile extends TestCase
{
	private static Log log = LogFactory.getLog( TestSavingAFile.class.getName() );

	private final RackMarshallingTestConfig tc = new RackMarshallingTestConfig();

	public void testSaveAFile()
		throws Exception
	{
		log.debug("Started.");
		final RackDataModel emptyRack = tc.rackService.createNewRackDataModel( "Empty Rack", "", 20, 20, true );
		tc.rackService.dumpRack( emptyRack );

		final DataRate dataRate = DataRate.CD_QUALITY;
		final MadGraphInstance<?,?> rackModelRootGraph = tc.rackService.getRackGraphInstance( emptyRack );
		final MadGraphInstance<?,?> emptyRackRootGraph = rackModelRootGraph;
		final int channelBufferLength = 64;
		final HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		final long nanosLatency = 1000;
		final int sfLatency = 10;
		final HardwareIOChannelSettings dataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosLatency, sfLatency );
		final RenderingPlan emptyRenderingPlan = tc.renderingPlanService.createRenderingPlan( emptyRackRootGraph, dataRateConfiguration, tc );
		tc.renderingPlanService.dumpRenderingPlan( emptyRenderingPlan );

		final String instanceName = "Test component one";
		final Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
		final MadDefinition<?,?> fadeInDefinition = tc.componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );
		final RackComponent testComponent = tc.rackService.createComponent( emptyRack, fadeInDefinition, parameterValues, instanceName );
		final MadInstance<?,?> testComponentOne = testComponent.getInstance();
		final MadChannelInstance ftciChannelInstance = testComponentOne.getChannelInstanceByName( "Input" );
		final MadChannelInstance ftcoChannelInstance = testComponentOne.getChannelInstanceByName( "Output" );
		tc.rackService.dumpRack( emptyRack );

		final String secInstanceName = "The second test component";
		final RackComponent secondComponent = tc.rackService.createComponent( emptyRack, fadeInDefinition, parameterValues,secInstanceName );
		final MadInstance<?,?> testComponentTwo = secondComponent.getInstance();
		final MadChannelInstance stciChannelInstance = testComponentTwo.getChannelInstanceByName( "Input" );
		final MadChannelInstance stcoChannelInstance = testComponentTwo.getChannelInstanceByName( "Output" );

		// Wire it to the input and output
		final MadChannelInstance rackInputChannelInstance = emptyRack.getRackIOChannelInstanceByName( "Input Channel 3" );
		final MadChannelInstance rackOutputChannelInstance = emptyRack.getRackIOChannelInstanceByName( "Output Channel 2" );
		tc.rackService.addRackIOLink( emptyRack, rackInputChannelInstance, testComponent, ftciChannelInstance );

		// And wire the first to the second
		tc.rackService.addRackLink( emptyRack, testComponent, ftcoChannelInstance, secondComponent, stciChannelInstance );

		// Finally link the output back to the rack IO
		tc.rackService.addRackIOLink( emptyRack, rackOutputChannelInstance, secondComponent, stcoChannelInstance );

		tc.rackService.dumpRack( emptyRack );

		final String outputFileName = "tmpoutput/test_save_file_output.xml";
		final File outputFile = new File( outputFileName );
		tc.rackMarshallingService.saveBaseRackToFile( emptyRack,  outputFile.getAbsolutePath() );

		// Create a rendering plan from it
		final RenderingPlan testRenderingPlan = tc.renderingPlanService.createRenderingPlan( rackModelRootGraph, dataRateConfiguration, tc );
		tc.renderingPlanService.dumpRenderingPlan( testRenderingPlan );

		tc.graphService.destroyGraph( rackModelRootGraph, true, true );
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tc.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		tc.tearDown();
		super.tearDown();
	}
}
