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

package uk.co.modularaudio.util.swing.dndtable;

import java.util.ArrayList;

public class GuiDndTableState
{
//	private static Log log = LogFactory.getLog( GuiDndTableState.class.getName() );

	public enum State
	{
		BROWSING,
		MOUSE_OVER_DRAGGABLE_AREA,
		DURING_DRAG,
		POPUP
	}

	private State currentState;
	private final ArrayList<GuiDndTableStateTransitionListener> transitionListeners = new ArrayList<GuiDndTableStateTransitionListener>();

	public GuiDndTableState( final State initialState )
	{
		this.currentState = initialState;
	}

	public void addTransitionListener( final GuiDndTableStateTransitionListener l )
	{
		transitionListeners.add( l );
	}

	public void removeTransitionListener( final GuiDndTableStateTransitionListener l )
	{
		transitionListeners.remove( l );
	}

	public void changeTo( final State nextState ) throws BadStateTransitionException
	{
		boolean error = false;
		switch( currentState )
		{
		case BROWSING:
			switch( nextState )
			{
			case BROWSING:
			case MOUSE_OVER_DRAGGABLE_AREA:
			case POPUP:
				break;
			default:
				error = true;
				break;
			}
			break;
		case POPUP:
			switch( nextState )
			{
				case BROWSING:
				case POPUP:
					break;
				default:
					error =true;
					break;
			}
			break;
		case MOUSE_OVER_DRAGGABLE_AREA:
			break;
		case DURING_DRAG:
			break;
		default:
			error = true;
			break;
		}
		if( error )
		{
			throw new BadStateTransitionException();
		}
		else
		{
			if( currentState != nextState )
			{
				final State stateBefore = currentState;
				final State stateAfter = nextState;
				currentState = nextState;
				for( final GuiDndTableStateTransitionListener l : transitionListeners)
				{
					l.receiveTransition( stateBefore, stateAfter);
				}
			}
		}
	}

	public class BadStateTransitionException extends Exception
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -8041595538802094L;

		public BadStateTransitionException()
		{
			super();
		}
	}

	public interface GuiDndTableStateTransitionListener
	{
		void receiveTransition( State stateBefore, State stateAfter );
	}

	public State getCurrentState()
	{
		return currentState;
	}
}
