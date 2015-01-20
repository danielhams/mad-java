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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AudioIOEngineStateMachine
{
	private static Log log = LogFactory.getLog( AudioIOEngineStateMachine.class.getName() );
	
	// Main states
	private final static int MS_UNCONFIGURED = 0;
	private final static int MS_CONFIGURED = 1;
	
	// Audio Engine Events
	public final static int AEE_SETATOMICPLRENDERINGQUEUE = 0;
	public final static int AEE_UNSETPLANRENDERINGQUEUE = 1;
	public final static int AEE_STARTWITHHARDWAREIO = 2;
	public final static int AEE_STOP = 3;
	public final static int AEE_AUDIOIOERROR = 4;
	public final static int AEE_PRERUNNINGFINISHED =5;
	public final static int AEE_RUNNINGFINISHED = 6;
	public final static int AEE_POSTRUNNINGFINISHED = 7;
	public final static int AEE_PRESTOPPINGFINISHED = 8;
	public final static int AEE_STOPPINGFINISHED = 9;
	public final static int AEE_POSTSTOPPINGFINISHED = 10;
	public final static int AEE_DESTROY = 11;

	// Configured sub states
	private final static int SS_CONFIGURED_STARTED = 0;
	private final static int SS_CONFIGURED_STOPPED = 1;

	// Started sub sub states
	private final static int SSS_STARTED_PRERUNNING = 0;
	private final static int SSS_STARTED_RUNNING = 1;
	private final static int SSS_STARTED_POSTRUNNING = 2;

	// Stopped sub sub states
	private final static int SSS_STOPPED_PRESTOPPING = 0;
	private final static int SSS_STOPPED_STOPPING = 1;
	private final static int SSS_STOPPED_POSTSTOPPING = 2;
	
	private int curState = MS_UNCONFIGURED;
	private int curSubState = -1;
	private int curSubSubState = -1;
	
	public AudioIOEngineStateMachine()
	{
	}
	
	public void receiveEvent( int event ) throws BadStateTransitionException
	{
		log.trace( "Event received in state(" + curState + "," + curSubState + ", " + curSubSubState + ") event(" + event + ")");
		try
		{
			switch( curState )
			{
				case MS_UNCONFIGURED:
				{
					processUnconfiguredEvent( event );
					break;
				}
				case MS_CONFIGURED:
				{
					processConfiguredEvent( event );
					break;
				}
				default:
				{
					throw new BadStateTransitionException("In an unknown state: " + curState );
				}
			}
		}
		finally
		{
			log.trace( "Event handling completed am now in state(" + curState + "," + curSubState + ", " + curSubSubState + ")");
		}
	}

	private void processUnconfiguredEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_SETATOMICPLRENDERINGQUEUE:
			{
				processSetAtomicRenderingQueueEvent();
				break;
			}
			case AEE_DESTROY:
			{
				processDestroyEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown unconfigured event:" + event );
			}
		}
	}

	private void processSetAtomicRenderingQueueEvent()
	{
		// Do any post/pre condition things
		
		// Now set the new state
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STOPPED;
		curSubSubState = -1;
	}

	private void processDestroyEvent()
	{
		// Do any cleanup here
		
		// Now set the state to something that will give errors should any more events occur
		curState = -1;
		curSubState = -1;
		curSubSubState = -1;
	}

	private void processConfiguredEvent( int event ) throws BadStateTransitionException
	{
		switch( curSubState )
		{
			case SS_CONFIGURED_STARTED:
			{
				processConfiguredStartedEvent( event );
				break;
			}
			case SS_CONFIGURED_STOPPED:
			{
				processConfiguredStoppedEvent( event );
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown configured sub state:" + curSubState );
			}
		}	
	}

	private void processConfiguredStartedEvent( int event ) throws BadStateTransitionException
	{
		switch( curSubSubState )
		{
			case -1:
			{
				// Is main state
				processStartedMainStateEvent( event );
				break;
			}
			case SSS_STARTED_PRERUNNING:
			{
				processStartedPreRunningEvent( event );
				break;
			}
			case SSS_STARTED_RUNNING:
			{
				processStartedRunningEvent( event );
				break;
			}
			case SSS_STARTED_POSTRUNNING:
			{
				processStartedPostRunningEvent( event );
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown configured started sub sub state:" + curSubSubState );
			}
		}
	}

	private void processStartedMainStateEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_STOP:
			{
				processStartedStopEvent();
				break;
			}
			case AEE_AUDIOIOERROR:
			{
				processStartedAudioIOErrorEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown event in main started substate: " + event );
			}
		}
	}

	private void processStartedStopEvent()
	{
		// Do pre/post things
		
		// Now set the new state
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STOPPED;
		curSubSubState = SSS_STOPPED_PRESTOPPING;
	}

	private void processStartedAudioIOErrorEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STOPPED;
		curSubSubState = SSS_STOPPED_PRESTOPPING;
	}

	private void processStartedPreRunningEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_PRERUNNINGFINISHED:
			{
				processPreRunningFinishedEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown event in started prerunning substate: " + event );
			}
		}
	}

	private void processPreRunningFinishedEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STARTED;
		curSubSubState = SSS_STARTED_RUNNING;
	}

	private void processStartedRunningEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_RUNNINGFINISHED:
			{
				processRunningFinishedEvent();
				break;
			}
			case AEE_AUDIOIOERROR:
			{
				processRunningAudioIOErrorEvent();
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown event in started running substate: " + event );
			}
		}
	}

	private void processRunningAudioIOErrorEvent()
	{
		// Do something to halt stuff?
		
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STOPPED;
		curSubSubState = SSS_STOPPED_PRESTOPPING;		
	}

	private void processRunningFinishedEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STARTED;
		curSubSubState = SSS_STARTED_POSTRUNNING;
	}

	private void processStartedPostRunningEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_POSTRUNNINGFINISHED:
			{
				processPostRunningFinishedEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown event in started postrunning substate: " + event );
			}
		}
	}

	private void processPostRunningFinishedEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STARTED;
		curSubSubState = -1;
	}

	private void processConfiguredStoppedEvent( int event ) throws BadStateTransitionException
	{
		switch( curSubSubState )
		{
			case -1:
			{
				processStoppedMainStateEvent( event );
				break;
			}
			case SSS_STOPPED_PRESTOPPING:
			{
				processStoppedPreStoppingEvent( event );
				break;
			}
			case SSS_STOPPED_STOPPING:
			{
				processStoppedStoppingEvent( event );
				break;
			}
			case SSS_STOPPED_POSTSTOPPING:
			{
				processStoppedPostStoppingEvent( event );
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown configured stopped sub sub state:" + curSubSubState );
			}
		}
	}

	private void processStoppedMainStateEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_STARTWITHHARDWAREIO:
			{
				processStoppedStartWithHardwareIOEvent();
				break;
			}
			case AEE_UNSETPLANRENDERINGQUEUE:
			{
				processStoppedUnsetPlanRenderingQueueEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown stopped main state event: " + event );
			}
		}
	}

	private void processStoppedStartWithHardwareIOEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STARTED;
		curSubSubState = SSS_STARTED_PRERUNNING;
	}

	private void processStoppedUnsetPlanRenderingQueueEvent()
	{
		curState = MS_UNCONFIGURED;
		curSubState = -1;
		curSubSubState = -1;
	}

	private void processStoppedPreStoppingEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_PRESTOPPINGFINISHED:
			{
				processPreStoppingFinishedEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown stopped prestopping event: " + event );
			}
		}
	}

	private void processPreStoppingFinishedEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STOPPED;
		curSubSubState = SSS_STOPPED_STOPPING;
	}

	private void processStoppedStoppingEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_STOPPINGFINISHED:
			{
				processStoppingFinishedEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown stopped stopping event: " + event );
			}
		}
	}

	private void processStoppingFinishedEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STOPPED;
		curSubSubState = SSS_STOPPED_POSTSTOPPING;
	}

	private void processStoppedPostStoppingEvent( int event ) throws BadStateTransitionException
	{
		switch( event )
		{
			case AEE_POSTSTOPPINGFINISHED:
			{
				processPostStoppingFinishedEvent();
				break;
			}
			default:
			{
				throw new BadStateTransitionException( "Unknown stopped poststopping event: " + event );
			}
		}
	}

	private void processPostStoppingFinishedEvent()
	{
		curState = MS_CONFIGURED;
		curSubState = SS_CONFIGURED_STOPPED;
		curSubSubState = -1;
	}

}
