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

import org.apache.mahout.math.map.OpenObjectIntHashMap;

import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessIOQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
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

//	private static Log log = LogFactory.getLog( MadInstance.class.getName() );
	
	protected String instanceName = null;
	protected final MD definition;
	protected final Map<MadParameterDefinition, String> creationParameterValues;
	protected MadChannelConfiguration channelConfiguration = null;
	protected MadChannelInstance[] channelInstances = null;
	protected OpenObjectIntHashMap<MadChannelInstance> channelInstanceToIndexMap = new OpenObjectIntHashMap<MadChannelInstance>();
	protected Map<String,MadChannelInstance> nameToChannelInstanceMap = new HashMap<String,MadChannelInstance>();
	
	protected MadState state = MadState.STOPPED;
	
	protected final MadLocklessQueueBridge<MI> localBridge;

	protected MadLocklessIOQueue commandToInstanceQueue = null;
	protected MadLocklessIOQueue temporalToInstanceQueue = null;
	
	protected MadLocklessIOQueue commandToUiQueue = null;
	protected MadLocklessIOQueue temporalToUiQueue = null;
	
	protected final boolean hasQueueProcessing;
	
	protected Vector<InstanceLifecycleListener> lifecycleListeners = new Vector<InstanceLifecycleListener>();
	
	public MadInstance( String instanceName,
			final MD definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
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
			MadChannelInstance ci = channelInstances[ c ];
			MadChannelDefinition cd = ci.definition;
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
		MadChannelDefinition[] channelDefinitionArray = channelConfiguration.getOrderedChannelDefinitions();
		int numChannels = channelDefinitionArray.length;
		
		MadChannelInstance[] retVal = new MadChannelInstance[ numChannels ];
		
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
		startup( hardwareChannelSettings, timingParameters, frameTimeFactory );
		for( InstanceLifecycleListener ill : lifecycleListeners )
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
	public final RealtimeMethodReturnCodeEnum preProcess( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime )
	{
		RealtimeMethodReturnCodeEnum retVal = RealtimeMethodReturnCodeEnum.SUCCESS;
//			log.debug("Doing queue preprocessing for " + instanceName );
		// Copy incoming (to instance) events into the temporary queues

		// This isn't necessary as we're resetting the event counts by the following lines
		//tempQueueEntryStorage.resetEventsToInstance();
		tempQueueEntryStorage.numCommandEventsToInstance = commandToInstanceQueue.copyToTemp( tempQueueEntryStorage.commandEventsToInstance,
				-1 );
		tempQueueEntryStorage.numTemporalEventsToInstance = temporalToInstanceQueue.copyToTemp( tempQueueEntryStorage.temporalEventsToInstance,
				periodStartFrameTime );
			
		// Now get the bridge to walk them
		int numCommands = tempQueueEntryStorage.numCommandEventsToInstance;
		for( int i = 0 ; i < numCommands ; i++ )
		{
			localBridge.receiveQueuedEventsToInstance( (MI) this, tempQueueEntryStorage, periodStartFrameTime, tempQueueEntryStorage.commandEventsToInstance[ i ] );
		}
		int numTemporals = tempQueueEntryStorage.numTemporalEventsToInstance;
		for( int i = 0 ; i < numTemporals ; i++ )
		{
			localBridge.receiveQueuedEventsToInstance( (MI)this, tempQueueEntryStorage, periodStartFrameTime, tempQueueEntryStorage.temporalEventsToInstance[ i ] );
		}
		
		return retVal;
	}

	public abstract RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
		final MadTimingParameters timingParameters,
		final long periodStartFrameTime,
		MadChannelConnectedFlags channelConnectedFlags,
		MadChannelBuffer[] channelBuffers,
		final int numFrames );

	public final RealtimeMethodReturnCodeEnum postProcess( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime )
	{
		RealtimeMethodReturnCodeEnum retVal = RealtimeMethodReturnCodeEnum.SUCCESS;
//			log.debug("Doing queue postprocessing for " + instanceName );
	
		// Push outgoing (to ui) events into their real queues.
	
		int numCommands = tempQueueEntryStorage.numCommandEventsToUi;
		if( numCommands > 0 )
		{
			commandToUiQueue.write( tempQueueEntryStorage.commandEventsToUi, 0, numCommands );
		}
		int numTemporals = tempQueueEntryStorage.numTemporalEventsToUi;
		if( numTemporals > 0 )
		{
			temporalToUiQueue.write( tempQueueEntryStorage.temporalEventsToUi, 0, numTemporals );
		}
		
		tempQueueEntryStorage.resetEventsToUi();
		
		return retVal;
	}

	public abstract void stop() throws MadProcessingException;

	public void internalEngineStop() throws MadProcessingException
	{
		stop();
		state = MadState.STOPPED;
		for( InstanceLifecycleListener ill : lifecycleListeners )
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
	
	public void setInstanceName( String newName )
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
	
	public int getChannelInstanceIndex( MadChannelInstance channelToLookFor )
		throws RecordNotFoundException
	{
		if( channelInstanceToIndexMap.containsKey( channelToLookFor ) )
		{
			return channelInstanceToIndexMap.get( channelToLookFor );
		}
		else
		{
			String msg = "Failed to find channel instance index for channel called " + channelToLookFor.definition.name + " in instance named " + getInstanceName();
			throw new RecordNotFoundException( msg );
		}
	}

	public int getChannelInstanceIndexByName( String channelInstanceName )
		throws RecordNotFoundException
	{
		MadChannelInstance ci = getChannelInstanceByName( channelInstanceName );
		return getChannelInstanceIndex( ci );
	}
	
	public MadChannelInstance getChannelInstanceByName( String channelName )
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
	
	public MadChannelInstance getChannelInstanceByNameReturnNull( String channelName )
	{
		try
		{
			return getChannelInstanceByName( channelName );
		}
		catch(RecordNotFoundException rnfe)
		{
			return null;
		}
	}

	public final MadState getState()
	{
		return state;
	}
	
	public String toString()
	{
		return( this.getClass().getSimpleName() + " named \"" + instanceName + "\"");
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
	
	public void addLifecycleListener( InstanceLifecycleListener lll )
	{
		lifecycleListeners.add( lll );
	}
	
	public void removeLifecycleListener( InstanceLifecycleListener lll )
	{
		lifecycleListeners.remove( lll );
	}
}
