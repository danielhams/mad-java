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

package uk.co.modularaudio.util.audio.gui.mad;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessIOQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.table.Span;

public abstract class AbstractMadUiInstance<D extends MadDefinition<D, I>, I extends MadInstance<D,I>>
	implements IMadUiInstance<D, I>, MadInstance.InstanceLifecycleListener, IOQueueEventUiConsumer<I>
{
	private static Log log = LogFactory.getLog( AbstractMadUiInstance.class.getName() );

	protected final I instance;
	protected final MadUiDefinition<D,  I> uiDefinition;

	protected MadUiChannelInstance[] channelInstances = new MadUiChannelInstance[0];
	protected AbstractMadUiControlInstance<?,?,?>[] controlInstances = new AbstractMadUiControlInstance[0];
	protected AbstractMadUiControlInstance<?,?,?>[] displayProcessingControlInstances = new AbstractMadUiControlInstance[0];

	protected final boolean eventsPassedBetweenInstanceAndUi;

	protected MadLocklessIOQueue commandToUiQueue;
	protected MadLocklessIOQueue temporalToUiQueue;
	protected MadLocklessIOQueue commandToInstanceQueue;
	protected MadLocklessIOQueue temporalToInstanceQueue;

	protected final MadLocklessQueueBridge<I> localQueueBridge;

	private final IOQueueEvent outEvent = new IOQueueEvent();

	// Set during startup and cleared during stop
	protected MadFrameTimeFactory frameTimeFactory;
	protected long temporalValueFixedLatencyFrames;

	public AbstractMadUiInstance( final I instance, final MadUiDefinition<D, I> uiDefinition )
	{
		this.instance = instance;
		this.uiDefinition = uiDefinition;

		final D definition = instance.getDefinition();
		this.localQueueBridge = definition.getIoQueueBridge();

		final MadLocklessQueueBridge<I> bridge = definition.getIoQueueBridge();
		eventsPassedBetweenInstanceAndUi = bridge.hasQueueProcessing();

		if( eventsPassedBetweenInstanceAndUi )
		{
			commandToUiQueue = instance.getCommandToUiQueue();
			temporalToUiQueue = instance.getTemporalToUiQueue();
			commandToInstanceQueue = instance.getCommandToInstanceQueue();
			temporalToInstanceQueue = instance.getTemporalToInstanceQueue();
		}

		instance.addLifecycleListener( this );
	}

	@Override
	public void destroy()
	{
		instance.removeLifecycleListener( this );
		// Call destroy on all our child controls
		for( int i =0 ; i < controlInstances.length ; i++)
		{
			final AbstractMadUiControlInstance<?, ?, ?> ci = controlInstances[ i ];
			ci.destroy();
		}
		controlInstances = null;
		displayProcessingControlInstances = null;
		channelInstances = null;
		commandToUiQueue = null;
		temporalToUiQueue = null;
		commandToInstanceQueue = null;
		temporalToInstanceQueue = null;
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		this.frameTimeFactory = frameTimeFactory;
		// Need to offset by one buffer length at audio rate so events don't get bunched up and processed in blocks
		this.temporalValueFixedLatencyFrames = ratesAndLatency.getAudioChannelSetting().getChannelBufferLength();
	}

	@Override
	public void receiveStop()
	{
//		log.debug("Received instance stop notification on " + instance.instanceName);
		frameTimeFactory = null;
		if( instance.hasQueueProcessing() )
		{
			localQueueBridge.cleanupOrphanedEvents( instance, this );
		}
	}

	@Override
	public abstract Span getCellSpan();

	@Override
	public I getInstance()
	{
		return instance;
	}

	@Override
	public MadUiDefinition<D, I> getUiDefinition()
	{
		return uiDefinition;
	}

	@Override
	public AbstractMadUiControlInstance<?, ?, ?>[] getUiControlInstances()
	{
		return controlInstances;
	}

	@Override
	public MadUiChannelInstance[] getUiChannelInstances()
	{
		return channelInstances;
	}

	@Override
	public final void receiveDisplayTick( final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiFrameTime)
	{
		try
		{
			if( eventsPassedBetweenInstanceAndUi )
			{
				// Copy any events in the instance queues that should be processed into the temporary area

				// Not necessary as we are resetting the event counts in the following lines
				//guiTemporaryEventStorage.resetEventsToUi();
				guiTemporaryEventStorage.numCommandEventsToUi = commandToUiQueue.copyToTemp( guiTemporaryEventStorage.commandEventsToUi );
				guiTemporaryEventStorage.numTemporalEventsToUi = temporalToUiQueue.copyToTemp( guiTemporaryEventStorage.temporalEventsToUi,
						currentGuiFrameTime );
			}

			doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiFrameTime );

			if( eventsPassedBetweenInstanceAndUi )
			{
				// Copy any out events from the temporary area into the instance queues
				final int numCommands = guiTemporaryEventStorage.numCommandEventsToInstance;
				if( numCommands > 0 )
				{
					commandToInstanceQueue.write( guiTemporaryEventStorage.commandEventsToInstance, 0, numCommands );
				}
				final int numTemporals = guiTemporaryEventStorage.numTemporalEventsToInstance;
				if( numTemporals > 0 )
				{
					temporalToInstanceQueue.write( guiTemporaryEventStorage.temporalEventsToInstance, 0, numTemporals );
				}
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught during display tick processing: " + e.toString();
			log.error( msg, e );
		}
	}

	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTick )
	{
		for( int i =0 ; i < displayProcessingControlInstances.length ; i++)
		{
			final AbstractMadUiControlInstance<?, ?, ?> ci = displayProcessingControlInstances[ i ];
			ci.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
		}
	}

	@Override
	public abstract void receiveComponentNameChange( final String newName );

	public void setUiControlsAndChannels( final AbstractMadUiControlInstance<?, ?, ?>[] controlsIn,
			final AbstractMadUiControlInstance<?, ?, ?>[] displayProcessingControlsIn,
			final MadUiChannelInstance[] channelsIn )
	{
		this.controlInstances = controlsIn;
		this.displayProcessingControlInstances = displayProcessingControlsIn;
		this.channelInstances = channelsIn;
	}

	// Useful command senders for the UI
	// These use the immediate versions of send (i.e. not via TSES)
	protected void sendTemporalValueToInstance(final int command, final long value)
	{
		outEvent.command = command;
		outEvent.value = value;
		long outEventTimestamp;
		if( frameTimeFactory != null )
		{
			outEventTimestamp = frameTimeFactory.getCurrentUiFrameTime() + temporalValueFixedLatencyFrames;
//			outEventTimestamp = frameTimeFactory.getCurrentUiFrameTime();
		}
		else
		{
			outEventTimestamp = 0;
		}
		localQueueBridge.sendTemporalEventToInstance( instance, outEventTimestamp,  outEvent );
	}

	protected void sendTemporalObjectToInstance(final int command, final Object obj)
	{
		outEvent.command = command;
		outEvent.object = obj;
		long outEventTimestamp;
		if( frameTimeFactory != null )
		{
			outEventTimestamp = frameTimeFactory.getCurrentUiFrameTime() + temporalValueFixedLatencyFrames;
		}
		else
		{
			outEventTimestamp = 0;
		}
		localQueueBridge.sendTemporalEventToInstance( instance, outEventTimestamp,  outEvent );
	}

	protected void sendCommandValueToInstance( final int command, final long value )
	{
		outEvent.command = command;
		outEvent.value = value;
		localQueueBridge.sendCommandEventToInstance( instance, outEvent );
	}

	protected void sendCommandObjectToInstance( final int command, final Object obj )
	{
		outEvent.command = command;
		outEvent.object = obj;
		localQueueBridge.sendCommandEventToInstance( instance, outEvent );
	}
}
