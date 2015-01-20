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

package test.uk.co.modularaudio.util.audio.mad.audioenginestate;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestUsingStateMachine extends TestCase
{
	private static Log log = LogFactory.getLog( TestUsingStateMachine.class.getName() );
	
	public void testUsingIt() throws Exception
	{
		AudioIOEngineStateMachine stateMachine = new AudioIOEngineStateMachine();
		
		stateMachine.receiveEvent( AudioIOEngineStateMachine.AEE_SETATOMICPLRENDERINGQUEUE );
		
		try
		{
			stateMachine.receiveEvent( AudioIOEngineStateMachine.AEE_SETATOMICPLRENDERINGQUEUE );
			log.debug("Ooops. Didn't get a problem");
		}
		catch(BadStateTransitionException bste)
		{
			log.debug(  "Caught expect state transition exception" );
		}
		
		try
		{
			stateMachine.receiveEvent( AudioIOEngineStateMachine.AEE_DESTROY );
		}
		catch(BadStateTransitionException bste )
		{
			log.debug(  "Caught expect state transition exception" );
		}
		
		stateMachine.receiveEvent( AudioIOEngineStateMachine.AEE_STARTWITHHARDWAREIO );
	}
}
