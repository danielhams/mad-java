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

package test.uk.co.modularaudio.service.ioandrendering.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;


public class TestUsingIOAndRendering
{
	private static Log log = LogFactory.getLog( TestUsingIOAndRendering.class.getName() );

	private ComponentDesigner componentDesigner = null;
	private GenericApplicationContext applicationContext = null;

	private ComponentDesignerFrontController componentDesignerFrontController = null;
//	private RenderingController renderingController = null;
//	private UserPreferencesController userPreferencesController = null;
//	private RackController rackController = null;

	public TestUsingIOAndRendering()
	{
		componentDesigner = new ComponentDesigner();
	}

	public void go() throws Exception
	{
		componentDesigner.setupApplicationContext( true, true, null, null );

		applicationContext = componentDesigner.getApplicationContext();

		// Grab the necessary controller references
		componentDesignerFrontController = applicationContext.getBean( ComponentDesignerFrontController.class );
//		renderingController = applicationContext.getBean( RenderingController.class );
//		userPreferencesController = applicationContext.getBean( UserPreferencesController.class );
//		rackController = applicationContext.getBean( RackController.class );

		boolean testSuccess = componentDesignerFrontController.testUserPreferencesChanges();
//		boolean testSuccess = true;

		if( !testSuccess )
		{
			log.debug("Failed the test..");
		}
		else
		{
			componentDesignerFrontController.toggleRendering();
			boolean audioEngineStarted = componentDesignerFrontController.startAudioEngine();
			if( audioEngineStarted )
			{

				try
				{
					Thread.sleep( 10000 );
				}
				catch( InterruptedException ie )
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
	public static void main( String[] args )
		throws Exception
	{
		TestUsingIOAndRendering tester = new TestUsingIOAndRendering();
		tester.go();
	}

}
