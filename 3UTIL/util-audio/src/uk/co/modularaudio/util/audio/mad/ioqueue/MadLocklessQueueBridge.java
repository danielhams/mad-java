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

package uk.co.modularaudio.util.audio.mad.ioqueue;

import uk.co.modularaudio.util.audio.mad.MadInstance;

public abstract class MadLocklessQueueBridge<I extends MadInstance<?, I>>
{
//	private final static Log log = LogFactory.getLog( MadLocklessQueueBridge.class.getName() );

	private final int commandEventsToInstanceCapacity;
	private final int temporalEventsToInstanceCapacity;
	private final int commandEventsToUiCapacity;
	private final int temporalEventsToUiCapacity;
	private final boolean hasQueueProcessing;

	protected MadLocklessQueueBridge( final int commandEventsToInstanceCapacity,
			final int temporalEventsToInstanceCapacity,
			final int commandEventsToUiCapacity,
			final int temporalEventsToUiCapacity )
	{
		this.commandEventsToInstanceCapacity = commandEventsToInstanceCapacity;
		this.temporalEventsToInstanceCapacity = temporalEventsToInstanceCapacity;
		this.commandEventsToUiCapacity = commandEventsToUiCapacity;
		this.temporalEventsToUiCapacity = temporalEventsToUiCapacity;
		hasQueueProcessing = calcHasQueueProcessing();
	}

	protected MadLocklessQueueBridge()
	{
		this.commandEventsToInstanceCapacity = MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH;
		this.temporalEventsToInstanceCapacity = MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH;
		this.commandEventsToUiCapacity = MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH;
		this.temporalEventsToUiCapacity = MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH;
		hasQueueProcessing = true;
	}

	public final int getCommandToInstanceQueueCapacity()
	{
		return commandEventsToInstanceCapacity;
	}

	public final int getCommandToUiQueueCapacity()
	{
		return commandEventsToUiCapacity;
	}

	public final int getTemporalToInstanceQueueCapacity()
	{
		return temporalEventsToInstanceCapacity;
	}

	public final int getTemporalToUiQueueCapacity()
	{
		return temporalEventsToUiCapacity;
	}

	public final boolean sendCommandEventToInstance( final I instance, final IOQueueEvent entry )
	{
		final MadLocklessIOQueue commandToInstanceQueue = instance.getCommandToInstanceQueue();
		return commandToInstanceQueue.writeOne( entry );
	}

	public final boolean sendTemporalEventToInstance( final I instance, final long guiFrameTime, final IOQueueEvent entry )
	{
		final MadLocklessIOQueue temporalToInstanceQueue = instance.getTemporalToInstanceQueue();
		entry.frameTime = guiFrameTime;
		return temporalToInstanceQueue.writeOne( entry );
	}

	public abstract void receiveQueuedEventsToInstance( I instance, ThreadSpecificTemporaryEventStorage tses,
			long periodTimestamp, IOQueueEvent queueEntry );

	public final void queueCommandEventToUi( final ThreadSpecificTemporaryEventStorage tses,
			final int command,
			final long value,
			final Object object )
	{
		final IOQueueEvent[] tempQueueEventStorage = tses.commandEventsToUi;
		final int storageOffset = tses.numCommandEventsToUi;
		tempQueueEventStorage[ storageOffset ].frameTime = 0;
		tempQueueEventStorage[ storageOffset ].command = command;
		tempQueueEventStorage[ storageOffset ].value = value;
		tempQueueEventStorage[ storageOffset ].object = object;
		tses.numCommandEventsToUi++;
	}

	public final void queueTemporalEventToUi( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int command,
			final long value,
			final Object object )
	{
		final IOQueueEvent[] tempQueueEventStorage = tses.temporalEventsToUi;
		final int storageOffset = tses.numTemporalEventsToUi;
		tempQueueEventStorage[ storageOffset ].frameTime = frameTime;
		tempQueueEventStorage[ storageOffset ].command = command;
		tempQueueEventStorage[ storageOffset ].value = value;
		tempQueueEventStorage[ storageOffset ].object = object;
		tses.numTemporalEventsToUi++;
	}

	public final void receiveQueuedEventsToUi( final ThreadSpecificTemporaryEventStorage tses, final I instance, final IOQueueEventUiConsumer<I> consumer )
	{
		for( int c = 0 ; c < tses.numCommandEventsToUi ; c++ )
		{
			consumer.consumeQueueEntry( instance, tses.commandEventsToUi[ c ] );
		}
		for( int t = 0 ; t < tses.numTemporalEventsToUi ; t++ )
		{
			consumer.consumeQueueEntry( instance, tses.temporalEventsToUi[ t ] );
		}
	}

	public final void cleanupOrphanedEvents( final I instance, final IOQueueEventUiConsumer<I> consumer )
	{
		final IOQueueEvent lastEvent = new IOQueueEvent();

		final MadLocklessIOQueue ctu = instance.getCommandToUiQueue();
		while( ctu.getNumReadable() > 0 )
		{
			ctu.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}

		final MadLocklessIOQueue ttu = instance.getTemporalToUiQueue();
		while( ttu.getNumReadable() > 0 )
		{
			ttu.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}

		final MadLocklessIOQueue cti = instance.getCommandToInstanceQueue();
		while( cti.getNumReadable() > 0 )
		{
			cti.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}

		final MadLocklessIOQueue tti = instance.getTemporalToInstanceQueue();
		while( tti.getNumReadable() > 0 )
		{
			tti.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}
	}

	private final boolean calcHasQueueProcessing()
	{
		return ( commandEventsToInstanceCapacity > 0 || temporalEventsToInstanceCapacity > 0 ||
				commandEventsToUiCapacity > 0 || temporalEventsToUiCapacity > 0 );
	}

	public boolean hasQueueProcessing()
	{
		return hasQueueProcessing;
	}
}
