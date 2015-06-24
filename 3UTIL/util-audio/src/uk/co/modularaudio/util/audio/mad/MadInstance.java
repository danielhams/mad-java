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

package uk.co.modularaudio.util.audio.mad;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessIOQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public abstract class MadInstance<MD extends MadDefinition<MD,MI>, MI extends MadInstance<MD, MI>>
{
	public interface InstanceLifecycleListener
	{
		void receiveStartup( final HardwareIOChannelSettings hardwareChannelSettings,
				final MadTimingParameters timingParameters,
				final MadFrameTimeFactory frameTimeFactory );
		void receiveStop();
	};

	private static Log log = LogFactory.getLog( MadInstance.class.getName() );

	protected String instanceName;
	protected final MD definition;
	protected final Map<MadParameterDefinition, String> creationParameterValues;
	protected final MadChannelConfiguration channelConfiguration;
	protected final MadChannelInstance[] channelInstances;
	protected final OpenObjectIntHashMap<MadChannelInstance> channelInstanceToIndexMap = new OpenObjectIntHashMap<MadChannelInstance>();
	protected final Map<String,MadChannelInstance> nameToChannelInstanceMap = new HashMap<String,MadChannelInstance>();

	protected MadState state = MadState.STOPPED;

	protected final MadLocklessQueueBridge<MI> localBridge;

	protected MadLocklessIOQueue commandToInstanceQueue;
	protected MadLocklessIOQueue temporalToInstanceQueue;

	protected MadLocklessIOQueue commandToUiQueue;
	protected MadLocklessIOQueue temporalToUiQueue;

	protected final boolean hasQueueProcessing;

	protected final Vector<InstanceLifecycleListener> lifecycleListeners = new Vector<InstanceLifecycleListener>();
	protected int temporalUiToInstanceFrameOffset;

	public MadInstance( final String instanceName,
			final MD definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		this.instanceName = instanceName;
		this.definition = definition;

		this.localBridge = definition.getIoQueueBridge();
		this.hasQueueProcessing = localBridge.hasQueueProcessing();

		this.creationParameterValues = creationParameterValues;
		this.channelConfiguration = channelConfiguration;
		this.channelInstances = createChannelInstances();
		for( int c = 0 ; c < channelInstances.length ; c++ )
		{
			final MadChannelInstance ci = channelInstances[ c ];
			final MadChannelDefinition cd = ci.definition;
			nameToChannelInstanceMap.put( cd.name, ci );
			channelInstanceToIndexMap.put( ci, c );
		}

		if( hasQueueProcessing )
		{
			commandToInstanceQueue = new MadLocklessIOQueue( IOQueueEvent.class, localBridge.getCommandToInstanceQueueCapacity() );
			commandToUiQueue = new MadLocklessIOQueue( IOQueueEvent.class, localBridge.getCommandToUiQueueCapacity() );

			temporalToInstanceQueue = new MadLocklessIOQueue( IOQueueEvent.class, localBridge.getTemporalToInstanceQueueCapacity() );
			temporalToUiQueue = new MadLocklessIOQueue( IOQueueEvent.class, localBridge.getTemporalToUiQueueCapacity() );
		}
	}

	private MadChannelInstance[] createChannelInstances()
	{
		final MadChannelDefinition[] channelDefinitionArray = channelConfiguration.getOrderedChannelDefinitions();
		final int numChannels = channelDefinitionArray.length;

		final MadChannelInstance[] retVal = new MadChannelInstance[ numChannels ];

		for( int i = 0 ; i < numChannels ; i++ )
		{
			retVal[ i ] = new MadChannelInstance( channelDefinitionArray[ i ], this );
		}

		return retVal;
	}

	public void internalEngineStartup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		state = MadState.RUNNING;
		// One buffer length delay for events from the UI so we get
		// smooth temporal spacing
		temporalUiToInstanceFrameOffset = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
		startup( hardwareChannelSettings, timingParameters, frameTimeFactory );
		for( final InstanceLifecycleListener ill : lifecycleListeners )
		{
			ill.receiveStartup(hardwareChannelSettings, timingParameters, frameTimeFactory );
		}
	}

	public abstract void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException;

	// Check the instance queues and push into the temp storage
	@SuppressWarnings("unchecked")
	protected final RealtimeMethodReturnCodeEnum preProcess( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime )
	{
		final RealtimeMethodReturnCodeEnum retVal = RealtimeMethodReturnCodeEnum.SUCCESS;
//			log.debug("Doing queue preprocessing for " + instanceName );
		// Copy incoming (to instance) events into the temporary queues

		// This isn't necessary as we're resetting the event counts by the following lines
		//tempQueueEntryStorage.resetEventsToInstance();
		final long queuePullingFrameTime = periodStartFrameTime + temporalUiToInstanceFrameOffset;

		tempQueueEntryStorage.numCommandEventsToInstance = commandToInstanceQueue.copyToTemp( tempQueueEntryStorage.commandEventsToInstance );
		tempQueueEntryStorage.numTemporalEventsToInstance = temporalToInstanceQueue.copyToTemp( tempQueueEntryStorage.temporalEventsToInstance,
				queuePullingFrameTime );

//		if( tempQueueEntryStorage.numTemporalEventsToInstance > 0 )
//		{
//			log.debug("preProcess found " + tempQueueEntryStorage.numTemporalEventsToInstance + " temp events waiting");
//		}

		// Now get the bridge to walk the commands
		// we'll leave the temporal ones to be
		// processed by the instance when it chooses
		final int numCommands = tempQueueEntryStorage.numCommandEventsToInstance;
		for( int i = 0 ; i < numCommands ; i++ )
		{
			localBridge.receiveQueuedEventsToInstance( (MI)this, tempQueueEntryStorage, periodStartFrameTime, tempQueueEntryStorage.commandEventsToInstance[ i ] );
		}
		// We don't push the temporal events here - it happens in the processWithEvents call

		return retVal;
	}

	@SuppressWarnings("unchecked")
	private int consumeTimestampedEvents( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final int numTemporalEvents,
			final int iCurEventIndex,
			final long curPeriodStartFrameTime )
	{
		int curEventIndex = iCurEventIndex;
//		log.debug("Start consume from event " + curEventIndex );
		while( curEventIndex < numTemporalEvents )
		{
			if( tempQueueEntryStorage.temporalEventsToInstance[curEventIndex].frameTime > curPeriodStartFrameTime )
			{
//				log.debug("Hit future event");
				break;
			}
			else
			{
				localBridge.receiveQueuedEventsToInstance( (MI)this,
						tempQueueEntryStorage,
						curPeriodStartFrameTime,
						tempQueueEntryStorage.temporalEventsToInstance[curEventIndex++] );
//				log.debug("Consumed event");
			}
		}
//		log.debug("Leaving consume before event " + curEventIndex );
		return curEventIndex;
	}

	@SuppressWarnings("unchecked")
	public RealtimeMethodReturnCodeEnum processWithEvents( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int numFrames )
	{
//		log.debug("ProcessWithEvents in " + instanceName);
		RealtimeMethodReturnCodeEnum retVal = RealtimeMethodReturnCodeEnum.SUCCESS;

		preProcess( tempQueueEntryStorage, timingParameters, periodStartFrameTime );
		final int numTemporalEvents = tempQueueEntryStorage.numTemporalEventsToInstance;

		if( numTemporalEvents > 0 )
		{
//			log.debug( "Have " + numTemporalEvents + " temporal events to handle" );
			// Chop period up into chunks up to the next event
			// process the event and carry on.
			int curEventIndex = 0;

			int numLeft = numFrames;
			int curFrameIndex = 0;
			long curPeriodStartFrameTime = periodStartFrameTime;

			// Process any events that should be taken on board before doing any dsp
			curEventIndex = consumeTimestampedEvents( tempQueueEntryStorage,
					numTemporalEvents,
					curEventIndex,
					curPeriodStartFrameTime );

			if( log.isTraceEnabled() && curEventIndex > 1)
			{
				log.trace( instanceName + " consumed " + curEventIndex + " temporal events before beginning the period." );
			}

			// Now loop around doing chunks of DSP until we exhaust
			// the frames
			while( numLeft > 0 )
			{
				final int numToNextEventInt = ( curEventIndex == numTemporalEvents ? numLeft :
					(int)(tempQueueEntryStorage.temporalEventsToInstance[curEventIndex].frameTime -
							curPeriodStartFrameTime) );

//				if( numToNextEventInt == 0 )
//				{
//					log.error("NTNE is zero in loop");
//				}

				final int numThisRound = (numToNextEventInt < numLeft ? numToNextEventInt : numLeft);
//				if( numThisRound == 0 )
//				{
//					log.error("Have a zero length round :-(");
//				}
//				else
//				{
//					log.trace( "Have a round of length " + numThisRound );
//				}

				if( (retVal = process( tempQueueEntryStorage,
						timingParameters,
						curPeriodStartFrameTime,
						channelConnectedFlags,
						channelBuffers,
						curFrameIndex,
						numThisRound ) )
					!=
					RealtimeMethodReturnCodeEnum.SUCCESS )
				{
					return retVal;
				}

				// Process any events for this frame index
				curPeriodStartFrameTime += numThisRound;
				curEventIndex = consumeTimestampedEvents( tempQueueEntryStorage,
						numTemporalEvents,
						curEventIndex,
						curPeriodStartFrameTime );

				curFrameIndex += numThisRound;
				numLeft -= numThisRound;

			}

			// And process any events left over
			final int numExtra = numTemporalEvents - curEventIndex;
			if( log.isWarnEnabled() && numExtra > 1 )
			{
				log.warn( instanceName + " consumed " + numExtra + " temporal events that fall at the end of the period." );
			}
			while( curEventIndex < numTemporalEvents )
			{
				localBridge.receiveQueuedEventsToInstance( (MI)this,
						tempQueueEntryStorage,
						curPeriodStartFrameTime,
						tempQueueEntryStorage.temporalEventsToInstance[curEventIndex++] );
			}
		}
		else
		{
			// Can be processed as one big chunk
			if( (retVal = process( tempQueueEntryStorage,
					timingParameters,
					periodStartFrameTime,
					channelConnectedFlags,
					channelBuffers,
					0,
					numFrames ) )
				!=
				RealtimeMethodReturnCodeEnum.SUCCESS )
			{
				return retVal;
			}
		}

		postProcess( tempQueueEntryStorage, timingParameters, periodStartFrameTime );

		return retVal;
	}


	public RealtimeMethodReturnCodeEnum processNoEvents( final ThreadSpecificTemporaryEventStorage tempEventQueue,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int numFrames )
	{
//		log.debug("ProcessNoEvents in " + instanceName );
		// Can be processed as one big chunk
		return process( tempEventQueue,
				timingParameters,
				periodStartFrameTime,
				channelConnectedFlags,
				channelBuffers,
				0,
				numFrames );
	}

	public abstract RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers,
			int frameOffset,
			final int numFrames );

	protected final RealtimeMethodReturnCodeEnum postProcess( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime )
	{
		final RealtimeMethodReturnCodeEnum retVal = RealtimeMethodReturnCodeEnum.SUCCESS;

		// Push outgoing (to ui) events into their real queues.

		final int numCommands = tempQueueEntryStorage.numCommandEventsToUi;
		if( numCommands > 0 )
		{
			final int numWritten = commandToUiQueue.write( tempQueueEntryStorage.commandEventsToUi, 0, numCommands );
			if( numWritten != numCommands )
			{
				if( log.isWarnEnabled() )
				{
					log.warn("Overflow in postProcess command write of " + instanceName );
				}
				if( log.isDebugEnabled() )
				{
					log.debug("Queue readable is " + commandToUiQueue.getNumReadable() );
					log.debug("Queue writeable is " + commandToUiQueue.getNumWriteable() );
				}
			}
		}
		final int numTemporals = tempQueueEntryStorage.numTemporalEventsToUi;
		if( numTemporals > 0 )
		{
			final int numWritten = temporalToUiQueue.write( tempQueueEntryStorage.temporalEventsToUi, 0, numTemporals );
			if( numWritten != numTemporals )
			{
				if( log.isWarnEnabled() )
				{
					log.warn("Overflow in postProcess command write of " + instanceName );
				}
				if( log.isDebugEnabled() )
				{
					log.debug("Queue readable is " + temporalToUiQueue.getNumReadable() );
					log.debug("Queue writeable is " + temporalToUiQueue.getNumWriteable() );
				}
			}
		}

		tempQueueEntryStorage.resetEventsToUi();

		return retVal;
	}

	public abstract void stop() throws MadProcessingException;

	public void internalEngineStop() throws MadProcessingException
	{
		stop();
		state = MadState.STOPPED;
		for( final InstanceLifecycleListener ill : lifecycleListeners )
		{
			ill.receiveStop();
		}
	}

	public void destroy()
	{
		commandToInstanceQueue = null;
		temporalToInstanceQueue = null;
		commandToUiQueue = null;
		temporalToUiQueue = null;
		// Any cleanup of the instance (samples, buffers etc) in here.
	}

	public String getInstanceName()
	{
		return instanceName;
	}

	public void setInstanceName( final String newName )
	{
		this.instanceName = newName;
	}

	public final MD getDefinition()
	{
		return definition;
	}

	public MadChannelInstance[] getChannelInstances()
	{
		return channelInstances;
	}

	public int getChannelInstanceIndex( final MadChannelInstance channelToLookFor )
		throws RecordNotFoundException
	{
		if( channelInstanceToIndexMap.containsKey( channelToLookFor ) )
		{
			return channelInstanceToIndexMap.get( channelToLookFor );
		}
		else
		{
			final String msg = "Failed to find channel instance index for channel called " + channelToLookFor.definition.name + " in instance named " + getInstanceName();
			throw new RecordNotFoundException( msg );
		}
	}

	public int getChannelInstanceIndexByName( final String channelInstanceName )
		throws RecordNotFoundException
	{
		final MadChannelInstance ci = getChannelInstanceByName( channelInstanceName );
		return getChannelInstanceIndex( ci );
	}

	public MadChannelInstance getChannelInstanceByName( final String channelName )
		throws RecordNotFoundException
	{
		if( nameToChannelInstanceMap.containsKey( channelName ) )
		{
			return nameToChannelInstanceMap.get( channelName );
		}
		else
		{
			throw new RecordNotFoundException( "No such channel: " + channelName );
		}
	}

	public MadChannelInstance getChannelInstanceByNameReturnNull( final String channelName )
	{
		try
		{
			return getChannelInstanceByName( channelName );
		}
		catch(final RecordNotFoundException rnfe)
		{
			return null;
		}
	}

	public final MadState getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " named \"" + instanceName + "\"";
	}

	public final Map<MadParameterDefinition, String> getCreationParameterValues()
	{
		return creationParameterValues;
	}

	public final MadLocklessIOQueue getCommandToInstanceQueue()
	{
		return commandToInstanceQueue;
	}

	public final MadLocklessIOQueue getCommandToUiQueue()
	{
		return commandToUiQueue;
	}

	public final MadLocklessIOQueue getTemporalToInstanceQueue()
	{
		return temporalToInstanceQueue;
	}

	public final MadLocklessIOQueue getTemporalToUiQueue()
	{
		return temporalToUiQueue;
	}

	public final boolean hasQueueProcessing()
	{
		return hasQueueProcessing;
	}

	public boolean isContainer()
	{
		return false;
	}

	public void addLifecycleListener( final InstanceLifecycleListener lll )
	{
		lifecycleListeners.add( lll );
	}

	public void removeLifecycleListener( final InstanceLifecycleListener lll )
	{
		lifecycleListeners.remove( lll );
	}
}
