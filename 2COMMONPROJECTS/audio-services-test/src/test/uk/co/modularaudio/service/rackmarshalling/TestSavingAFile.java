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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.service.rackmarshalling.abstractunittest.AbstractGraphTest;
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

public class TestSavingAFile extends AbstractGraphTest
{
	private static Log log = LogFactory.getLog( TestSavingAFile.class.getName() );

	public void testSaveAFile()
		throws Exception
	{
		log.debug("Started.");
		final RackDataModel emptyRack = rackService.createNewRackDataModel( "Empty Rack", "", 20, 20, true );
		rackService.dumpRack( emptyRack );

		final DataRate dataRate = DataRate.CD_QUALITY;
		final MadGraphInstance<?,?> rackModelRootGraph = rackService.getRackGraphInstance( emptyRack );
		final MadGraphInstance<?,?> emptyRackRootGraph = rackModelRootGraph;
		final int channelBufferLength = 64;
		final HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		final long nanosLatency = 1000;
		final int sfLatency = 10;
		final HardwareIOChannelSettings dataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosLatency, sfLatency );
		final RenderingPlan emptyRenderingPlan = renderingPlanService.createRenderingPlan( emptyRackRootGraph, dataRateConfiguration, this );
		renderingPlanService.dumpRenderingPlan( emptyRenderingPlan );

		final String instanceName = "Test component one";
		final Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
		final MadDefinition<?,?> fadeInDefinition = componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );
		final RackComponent testComponent = rackService.createComponent( emptyRack, fadeInDefinition, parameterValues, instanceName );
		final MadInstance<?,?> testComponentOne = testComponent.getInstance();
		final MadChannelInstance ftciChannelInstance = testComponentOne.getChannelInstanceByName( "Input" );
		final MadChannelInstance ftcoChannelInstance = testComponentOne.getChannelInstanceByName( "Output" );
		rackService.dumpRack( emptyRack );

		final String secInstanceName = "The second test component";
		final RackComponent secondComponent = rackService.createComponent( emptyRack, fadeInDefinition, parameterValues,secInstanceName );
		final MadInstance<?,?> testComponentTwo = secondComponent.getInstance();
		final MadChannelInstance stciChannelInstance = testComponentTwo.getChannelInstanceByName( "Input" );
		final MadChannelInstance stcoChannelInstance = testComponentTwo.getChannelInstanceByName( "Output" );

		// Wire it to the input and output
		final MadChannelInstance rackInputChannelInstance = emptyRack.getRackIOChannelInstanceByName( "Input Channel 3" );
		final MadChannelInstance rackOutputChannelInstance = emptyRack.getRackIOChannelInstanceByName( "Output Channel 2" );
		rackService.addRackIOLink( emptyRack, rackInputChannelInstance, testComponent, ftciChannelInstance );

		// And wire the first to the second
		rackService.addRackLink( emptyRack, testComponent, ftcoChannelInstance, secondComponent, stciChannelInstance );

		// Finally link the output back to the rack IO
		rackService.addRackIOLink( emptyRack, rackOutputChannelInstance, secondComponent, stcoChannelInstance );

		rackService.dumpRack( emptyRack );

		rackMarshallingService.saveBaseRackToFile( emptyRack,  "test_save_file_output.xml" );

		// Create a rendering plan from it
		final RenderingPlan testRenderingPlan = renderingPlanService.createRenderingPlan( rackModelRootGraph, dataRateConfiguration, this );
		renderingPlanService.dumpRenderingPlan( testRenderingPlan );

		graphService.destroyGraph( rackModelRootGraph, true, true );
	}
}
