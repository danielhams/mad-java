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

package test.uk.co.modularaudio.service.madgraph.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.service.stubs.FakeAdvancedComponentsFrontController;
import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.BaseComponentsFactory;
import uk.co.modularaudio.mads.internal.InternalComponentsFactory;
import uk.co.modularaudio.service.madclassification.impl.MadClassificationServiceImpl;
import uk.co.modularaudio.service.madcomponent.impl.MadComponentServiceImpl;
import uk.co.modularaudio.service.madgraph.impl.MadGraphServiceImpl;

public class GraphTestConfig
{
	private static Log log = LogFactory.getLog( GraphTestConfig.class.getName());

	public MadGraphServiceImpl graphService;
	public MadComponentServiceImpl componentService;
	public InternalComponentsFactory internalComponentsFactory;
	public BaseComponentsFactory baseComponentsFactory;
	public MadClassificationServiceImpl classificationService;
	public AdvancedComponentsFrontController advancedComponentsFrontController;

	public void setUp() throws Exception
	{
		log.info( getClass().getSimpleName() + " unit test beginning");
		classificationService = new MadClassificationServiceImpl();
		classificationService.init();
		componentService = new MadComponentServiceImpl();
		componentService.init();
		graphService = new MadGraphServiceImpl();
		graphService.setComponentService( componentService );
		graphService.init();
		advancedComponentsFrontController = new FakeAdvancedComponentsFrontController();

		internalComponentsFactory = new InternalComponentsFactory();
		internalComponentsFactory.setComponentService( componentService );
		internalComponentsFactory.setClassificationService(  classificationService  );
		internalComponentsFactory.setAdvancedComponentsFrontController(advancedComponentsFrontController);
		internalComponentsFactory.init();

		baseComponentsFactory = new BaseComponentsFactory();
		baseComponentsFactory.setComponentService( componentService );
		baseComponentsFactory.setClassificationService(classificationService);
		baseComponentsFactory.setAdvancedComponentsFrontController(advancedComponentsFrontController);
		baseComponentsFactory.init();

		graphService.postInit();
	}

	public void tearDown() throws Exception
	{
		log.info( getClass().getSimpleName() + " unit test done");
		graphService.preShutdown();

		internalComponentsFactory.destroy();
		componentService.destroy();
		graphService.destroy();
		classificationService.destroy();
	}
}
