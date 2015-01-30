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
import uk.co.modularaudio.service.rendering.RenderingPlan;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;

public class TestFlatteningGraph extends AbstractGraphTest
{
	private static Log log = LogFactory.getLog( TestFlatteningGraph.class.getName() );

	public void testSimpleGraphFlatten()
		throws Exception
	{
		log.debug("SimpleGraphFlattenTest beginning.");
		MadGraphInstance<?,?> graphToRender = graphService.createNewRootGraph(  "Test Simple Graph" );

		MadDefinition<?,?> definition = componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		MadInstance<?,?> firstInstance = componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Test instance" );
		graphService.addInstanceToGraphWithName(  graphToRender, firstInstance, firstInstance.getInstanceName() );

		MadChannelInstance[] firstChannelInstances = firstInstance.getChannelInstances();
		MadChannelInstance firstConsumerChannel = firstChannelInstances[ FadeInMadDefinition.CONSUMER ];

		MadInstance<?,?> secondInstance = componentService.createInstanceFromDefinition(  definition,
				emptyParameterMap, "Test instance two");

		graphService.addInstanceToGraphWithName( graphToRender, secondInstance, secondInstance.getInstanceName() );

		MadChannelInstance[] secondChannelInstances = secondInstance.getChannelInstances();
		MadChannelInstance secondProducerChannel = secondChannelInstances[ FadeInMadDefinition.PRODUCER ];
		MadChannelInstance secondConsumerChannel = secondChannelInstances[ FadeInMadDefinition.CONSUMER ];

		MadLink link = new MadLink( secondProducerChannel, firstConsumerChannel );

		graphService.addLink( graphToRender,  link );

		MadInstance<?,?> thirdInstance = componentService.createInstanceFromDefinition(  definition,
				emptyParameterMap, "Third one");

		graphService.addInstanceToGraphWithName( graphToRender, thirdInstance, thirdInstance.getInstanceName() );

		MadChannelInstance[] thirdChannelInstances = thirdInstance.getChannelInstances();
		MadChannelInstance thirdProducerChannel = thirdChannelInstances[ FadeInMadDefinition.PRODUCER ];

		MadLink link2 = new MadLink( thirdProducerChannel, secondConsumerChannel );

		graphService.addLink( graphToRender, link2 );

		DataRate dataRate = DataRate.getCDQuality();
		int channelBufferLength = 64;
		HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		long nanosLatency = 1000;
		int sfLatency = 10;
		HardwareIOChannelSettings planDataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosLatency, sfLatency );
		RenderingPlan renderingPlan = null;
//		int numRenderingPlanCreations = 10000;
		int numRenderingPlanCreations = 1;
		for( int i = 0 ; i < numRenderingPlanCreations ; i ++)
		{
			log.debug("Before creating the rendering plan");
			long bt = System.nanoTime();
			renderingPlan = renderingService.createRenderingPlan( graphToRender, planDataRateConfiguration, this );
			long at = System.nanoTime();
			long diff = at - bt;
			long inMicros = diff / 1000;
			log.debug("After creating the rendering plan diff in micros is " + inMicros );
		}
		graphService.dumpGraph(  graphToRender  );
		renderingService.dumpRenderingPlan( renderingPlan );

		graphService.destroyGraph( graphToRender, true, true );
	}
}
