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

package test.uk.co.modularaudio.service.rendering.abstractunittest;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.stubs.FakeAdvancedComponentsFrontController;
import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.internal.InternalComponentsFactory;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;

public class AbstractGraphTest extends TestCase
	implements MadFrameTimeFactory
{
	private static Log log = LogFactory.getLog( AbstractGraphTest.class.getName());
	
	private List<SpringContextHelper> clientHelpers = null;
	private SpringComponentHelper sch = null;
	private GenericApplicationContext gac = null;
	
	protected MadGraphService graphService = null;
	protected MadComponentService componentService = null;
	protected InternalComponentsFactory internalComponentsFactory = null;
	protected MadClassificationService classificationService = null;
	protected RenderingService renderingService = null;
	protected AdvancedComponentsFrontController advancedComponentsFrontController = null;
	
	protected void setUp() throws Exception
	{
		super.setUp();
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
		renderingService = gac.getBean( RenderingService.class );
		advancedComponentsFrontController = new FakeAdvancedComponentsFrontController();
	}

	protected void tearDown() throws Exception
	{
		log.info( getClass().getSimpleName() + " unit test done");
		gac.stop();
		gac.destroy();
		super.tearDown();
	}

	@Override
	public long getCurrentUiFrameTime()
	{
		return 0;
	}

}
