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
		final MadGraphInstance<?,?> graphToRender = graphService.createNewRootGraph(  "Test Simple Graph" );

		final MadDefinition<?,?> definition = componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition, String> emptyParameterMap = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> firstInstance = componentService.createInstanceFromDefinition(  definition, emptyParameterMap, "Test instance" );
		graphService.addInstanceToGraphWithName(  graphToRender, firstInstance, firstInstance.getInstanceName() );

		final MadChannelInstance[] firstChannelInstances = firstInstance.getChannelInstances();
		final MadChannelInstance firstConsumerChannel = firstChannelInstances[ FadeInMadDefinition.CONSUMER ];

		final MadInstance<?,?> secondInstance = componentService.createInstanceFromDefinition(  definition,
				emptyParameterMap, "Test instance two");

		graphService.addInstanceToGraphWithName( graphToRender, secondInstance, secondInstance.getInstanceName() );

		final MadChannelInstance[] secondChannelInstances = secondInstance.getChannelInstances();
		final MadChannelInstance secondProducerChannel = secondChannelInstances[ FadeInMadDefinition.PRODUCER ];
		final MadChannelInstance secondConsumerChannel = secondChannelInstances[ FadeInMadDefinition.CONSUMER ];

		final MadLink link = new MadLink( secondProducerChannel, firstConsumerChannel );

		graphService.addLink( graphToRender,  link );

		final MadInstance<?,?> thirdInstance = componentService.createInstanceFromDefinition(  definition,
				emptyParameterMap, "Third one");

		graphService.addInstanceToGraphWithName( graphToRender, thirdInstance, thirdInstance.getInstanceName() );

		final MadChannelInstance[] thirdChannelInstances = thirdInstance.getChannelInstances();
		final MadChannelInstance thirdProducerChannel = thirdChannelInstances[ FadeInMadDefinition.PRODUCER ];

		final MadLink link2 = new MadLink( thirdProducerChannel, secondConsumerChannel );

		graphService.addLink( graphToRender, link2 );

		final DataRate dataRate = DataRate.CD_QUALITY;
		final int channelBufferLength = 64;
		final HardwareIOOneChannelSetting coreEngineLatencyConfiguration = new HardwareIOOneChannelSetting( dataRate, channelBufferLength );

		final long nanosLatency = 1000;
		final int sfLatency = 10;
		final HardwareIOChannelSettings planDataRateConfiguration = new HardwareIOChannelSettings( coreEngineLatencyConfiguration, nanosLatency, sfLatency );
		RenderingPlan renderingPlan = null;
//		int numRenderingPlanCreations = 10000;
		final int numRenderingPlanCreations = 1;
		for( int i = 0 ; i < numRenderingPlanCreations ; i ++)
		{
			log.debug("Before creating the rendering plan");
			final long bt = System.nanoTime();
			renderingPlan = renderingService.createRenderingPlan( graphToRender, planDataRateConfiguration, this );
			final long at = System.nanoTime();
			final long diff = at - bt;
			final long inMicros = diff / 1000;
			log.debug("After creating the rendering plan diff in micros is " + inMicros );
		}
		graphService.dumpGraph(  graphToRender  );
		renderingService.dumpRenderingPlan( renderingPlan );

		graphService.destroyGraph( graphToRender, true, true );
	}
}
