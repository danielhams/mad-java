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

package test.uk.co.modularaudio.service.ioandrendering;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.TestConstants;
import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;


public class TestSubRackDestruction
{
//	private static Log log = LogFactory.getLog( TestSubRackDestruction.class.getName() );

	private final ComponentDesigner componentDesigner;
	private GenericApplicationContext applicationContext;

	private ComponentDesignerFrontController componentDesignerFrontController;
//	private RenderingController renderingController;
//	private UserPreferencesController userPreferencesController;
//	private RackController rackController;
	private RackService rackService;
//	private GraphService graphService;
	private MadComponentService componentService;

	public TestSubRackDestruction()
	{
		componentDesigner = new ComponentDesigner();
	}

	public void go() throws Exception
	{
		componentDesigner.setupApplicationContext( TestConstants.CDTEST_PROPERTIES,
				null, null,
				true, true );

		applicationContext = componentDesigner.getApplicationContext();

		// Grab the necessary controller references
		componentDesignerFrontController = applicationContext.getBean( ComponentDesignerFrontController.class );
//		renderingController = applicationContext.getBean( RenderingController.class );
//		userPreferencesController = applicationContext.getBean( UserPreferencesController.class );
//		rackController = applicationContext.getBean( RackController.class );
		rackService = applicationContext.getBean( RackService.class );
//		graphService = applicationContext.getBean( GraphService.class );
		componentService = applicationContext.getBean( MadComponentService.class );

		final RackDataModel rootRack = rackService.createNewRackDataModel( "Root Rack", "", 16, 16, true );

		final MadDefinition<?,?> subRackDef = componentService.findDefinitionById( SubRackMadDefinition.DEFINITION_ID );

		final Map<MadParameterDefinition,String> emptyParamValues = new HashMap<MadParameterDefinition,String>();

		rackService.createComponent( rootRack, subRackDef, emptyParamValues, "Sub Rack" );

		rackService.dumpRack( rootRack );

		rackService.destroyRackDataModel( rootRack );


		// Do stuff
		componentDesignerFrontController.ensureRenderingStoppedBeforeExit();

		componentDesigner.destroyApplicationContext();
	}

	/**
	 * @param args
	 */
	public static void main( final String[] args )
		throws Exception
	{
		final TestSubRackDestruction tester = new TestSubRackDestruction();
		tester.go();
	}

}
