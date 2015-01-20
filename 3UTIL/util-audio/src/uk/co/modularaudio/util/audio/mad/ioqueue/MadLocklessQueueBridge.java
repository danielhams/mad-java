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

public abstract class MadLocklessQueueBridge<MI extends MadInstance<?, MI>>
{
//	private final static Log log = LogFactory.getLog( MadLocklessQueueBridge.class.getName() );
	
	private int commandEventsToInstanceCapacity = -1;
	private int temporalEventsToInstanceCapacity = -1;
	private int commandEventsToUiCapacity = -1;
	private int temporalEventsToUiCapacity = -1;
	private boolean hasQueueProcessing = false;
	
	protected MadLocklessQueueBridge( int commandEventsToInstanceCapacity,
			int temporalEventsToInstanceCapacity,
			int commandEventsToUiCapacity,
			int temporalEventsToUiCapacity )
	{
		this.commandEventsToInstanceCapacity = commandEventsToInstanceCapacity;
		this.temporalEventsToInstanceCapacity = temporalEventsToInstanceCapacity;
		this.commandEventsToUiCapacity = commandEventsToUiCapacity;
		this.temporalEventsToUiCapacity = temporalEventsToUiCapacity;
		calcHasQueueProcessing();
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
	
	public final boolean sendCommandEventToInstance( MI instance, IOQueueEvent entry )
	{
		MadLocklessIOQueue commandToInstanceQueue = instance.getCommandToInstanceQueue();
		return commandToInstanceQueue.writeOne( entry );
	}

	public final boolean sendTemporalEventToInstance( MI instance, long guiFrameTime, IOQueueEvent entry )
	{
		MadLocklessIOQueue temporalToInstanceQueue = instance.getTemporalToInstanceQueue();
		entry.frameTime = guiFrameTime;
		return temporalToInstanceQueue.writeOne( entry );
	}

	public abstract void receiveQueuedEventsToInstance( MI instance, ThreadSpecificTemporaryEventStorage tses,
			long periodTimestamp, IOQueueEvent queueEntry );
	
	public final void queueCommandEventToUi( ThreadSpecificTemporaryEventStorage tses,
			int command,
			long value,
			Object object )
	{
		IOQueueEvent[] tempQueueEventStorage = tses.commandEventsToUi;
		int storageOffset = tses.numCommandEventsToUi;
		tempQueueEventStorage[ storageOffset ].frameTime = 0;
		tempQueueEventStorage[ storageOffset ].command = command;
		tempQueueEventStorage[ storageOffset ].value = value;
		tempQueueEventStorage[ storageOffset ].object = object;
		tses.numCommandEventsToUi++;
	}
	
	public final void queueTemporalEventToUi( ThreadSpecificTemporaryEventStorage tses,
			long frameTime,
			int command,
			long value,
			Object object )
	{
		IOQueueEvent[] tempQueueEventStorage = tses.temporalEventsToUi;
		int storageOffset = tses.numTemporalEventsToUi;
		tempQueueEventStorage[ storageOffset ].frameTime = frameTime;
		tempQueueEventStorage[ storageOffset ].command = command;
		tempQueueEventStorage[ storageOffset ].value = value;
		tempQueueEventStorage[ storageOffset ].object = object;
		tses.numTemporalEventsToUi++;
	}
	
//	public final void queueCommandEventToInstance( ThreadSpecificTemporaryEventStorage tses,
//			int command,
//			long value,
//			Object object )
//	{
//		IOQueueEvent[] tempQueueEventStorage = tses.commandEventsToInstance;
//		int storageOffset = tses.numCommandEventsToInstance;
//		tempQueueEventStorage[ storageOffset ].frameTime = 0;
//		tempQueueEventStorage[ storageOffset ].command = command;
//		tempQueueEventStorage[ storageOffset ].value = value;
//		tempQueueEventStorage[ storageOffset ].object = object;
//		tses.numCommandEventsToInstance++;
//	}
//
//	public final void queueTemporalEventToInstance( ThreadSpecificTemporaryEventStorage tses,
//			long frameTime,
//			int command,
//			long value,
//			Object object )
//	{
//		IOQueueEvent[] tempQueueEventStorage = tses.temporalEventsToInstance;
//		int storageOffset = tses.numTemporalEventsToInstance;
//		tempQueueEventStorage[ storageOffset ].frameTime = frameTime;
//		tempQueueEventStorage[ storageOffset ].command = command;
//		tempQueueEventStorage[ storageOffset ].value = value;
//		tempQueueEventStorage[ storageOffset ].object = object;
//		tses.numTemporalEventsToInstance++;
//	}
	
	public final void receiveQueuedEventsToUi( ThreadSpecificTemporaryEventStorage tses, MI instance, IOQueueEventUiConsumer<MI> consumer )
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
	
	public final void cleanupOrphanedEvents( MI instance, IOQueueEventUiConsumer<MI> consumer )
	{
		IOQueueEvent lastEvent = new IOQueueEvent();

		MadLocklessIOQueue ctu = instance.getCommandToUiQueue();
		while( ctu.getNumReadable() > 0 )
		{
			ctu.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}

		MadLocklessIOQueue ttu = instance.getTemporalToUiQueue();
		while( ttu.getNumReadable() > 0 )
		{
			ttu.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}

		MadLocklessIOQueue cti = instance.getCommandToInstanceQueue();
		while( cti.getNumReadable() > 0 )
		{
			cti.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}

		MadLocklessIOQueue tti = instance.getTemporalToInstanceQueue();
		while( tti.getNumReadable() > 0 )
		{
			tti.readOneCopyToDest( lastEvent );
			consumer.consumeQueueEntry( instance, lastEvent );
		}
	}

	private final void calcHasQueueProcessing()
	{
		if( commandEventsToInstanceCapacity > 0 || temporalEventsToInstanceCapacity > 0 ||
				commandEventsToUiCapacity > 0 || temporalEventsToUiCapacity > 0 )
		{
			hasQueueProcessing = true;
		}
		else
		{
			hasQueueProcessing = false;
		}
	}
	
	public boolean hasQueueProcessing()
	{
		return hasQueueProcessing;
	}
}
