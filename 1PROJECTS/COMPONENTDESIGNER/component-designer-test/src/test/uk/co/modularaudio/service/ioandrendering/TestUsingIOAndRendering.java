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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.TestConstants;
import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;


public class TestUsingIOAndRendering
{
	private static Log log = LogFactory.getLog( TestUsingIOAndRendering.class.getName() );

	private final ComponentDesigner componentDesigner;
	private GenericApplicationContext applicationContext;

	private ComponentDesignerFrontController componentDesignerFrontController;
//	private RenderingController renderingController;
//	private UserPreferencesController userPreferencesController;
//	private RackController rackController;

	public TestUsingIOAndRendering()
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

		final boolean testSuccess = componentDesignerFrontController.testUserPreferencesChanges();
//		boolean testSuccess = true;

		if( !testSuccess )
		{
			log.debug("Failed the test..");
		}
		else
		{
			componentDesignerFrontController.toggleRendering();
			final boolean audioEngineStarted = componentDesignerFrontController.startAudioEngine();
			if( audioEngineStarted )
			{

				try
				{
					Thread.sleep( 10000 );
				}
				catch( final InterruptedException ie )
				{
				}

				componentDesignerFrontController.stopAudioEngine();
			}
			componentDesignerFrontController.toggleRendering();
		}

		// Do stuff
		componentDesigner.destroyApplicationContext();
	}

	/**
	 * @param args
	 */
	public static void main( final String[] args )
		throws Exception
	{
		final TestUsingIOAndRendering tester = new TestUsingIOAndRendering();
		tester.go();
	}

}
