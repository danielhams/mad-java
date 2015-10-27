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

package uk.co.modularaudio.util.statetransition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StateTransitionEngine
{
	private static Log log = LogFactory.getLog( StateTransitionEngine.class.getName() );

	public interface StateEventHandler
	{
		int processEventCreateTransition( int currentState, int event );
	};

	public interface StateTransitionHandler
	{
		void executeTransition( int currentState, int newState );
	};

	public final static StateEventHandler[][] getEmptyEventHandlersArray(
			final int numStates,
			final int numEvents )
	{
		return new StateEventHandler[numStates][numEvents];
	}

	public final static StateTransitionHandler[] getEmptyTransitionHandlersArray(
			final int numStates )
	{
		return new StateTransitionHandler[numStates];
	}

	private final StateEventHandler[][] eventHandlers;

	private final StateTransitionHandler[] transitionHandlers;

	public StateTransitionEngine( final StateEventHandler[][] eventHandlers,
			final StateTransitionHandler[] transitionHandlers )
	{
		this.eventHandlers = eventHandlers;
		this.transitionHandlers = transitionHandlers;
	}

	public int processEvent( final int currentState, final int event )
	{
		final StateEventHandler th = eventHandlers[currentState][event];

		if( th == null )
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Handler missing for event " + event + " in state " + currentState );
			}
			return currentState;
		}
		else
		{
			final int newState = th.processEventCreateTransition( currentState, event );
			if( newState != currentState )
			{
				final StateTransitionHandler pth = transitionHandlers[ newState ];
				if( pth != null )
				{
					pth.executeTransition( currentState, newState );
				}
				return newState;
			}
			else
			{
				return currentState;
			}
		}
	}
}
