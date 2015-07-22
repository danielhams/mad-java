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

package test.uk.co.modularaudio.service.rackmarshalling.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.mads.internal.InternalComponentsFactory;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.renderingplan.RenderingPlanService;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;

public class RackMarshallingTestConfig
	implements MadFrameTimeFactory
{
	private static Log log = LogFactory.getLog( RackMarshallingTestConfig.class.getName());

	private List<SpringContextHelper> clientHelpers;
	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	public MadGraphService graphService;
	public MadComponentService componentService;
	public InternalComponentsFactory internalComponentsFactory;
	public MadClassificationService classificationService;
	public RenderingPlanService renderingPlanService;
	public RackService rackService;
	public RackMarshallingService rackMarshallingService;

	public void setUp() throws Exception
	{
		log.info( getClass().getSimpleName() + " unit test beginning");

		clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext();
		gac.start();
		classificationService = gac.getBean( MadClassificationService.class );
		graphService = gac.getBean( MadGraphService.class );
		componentService = gac.getBean( MadComponentService.class );
		internalComponentsFactory = gac.getBean( InternalComponentsFactory.class );
		renderingPlanService = gac.getBean( RenderingPlanService.class );
		rackService = gac.getBean( RackService.class );
		rackMarshallingService = gac.getBean( RackMarshallingService.class );
	}

	public void tearDown() throws Exception
	{
		log.info( getClass().getSimpleName() + " unit test done");
		gac.stop();
		gac.destroy();
	}

	@Override
	public long getCurrentUiFrameTime()
	{
		return 0;
	}

}
