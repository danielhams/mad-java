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

import uk.co.modularaudio.util.audio.buffer.LocklessPreallocatingGenericRingBuffer;

public class MadLocklessIOQueue extends LocklessPreallocatingGenericRingBuffer<IOQueueEvent>
{
//	private static Log log = LogFactory.getLog( MadLocklessIOQueue.class.getName() );

	private static final IOQueueEventCopier COPIER = new IOQueueEventCopier();

	public final static int DEFAULT_QUEUE_LENGTH = 64;

	public MadLocklessIOQueue( final Class<IOQueueEvent> clazz, final int capacity )
	{
		super( clazz, COPIER, capacity );

	}

	public final int copyToTemp( final IOQueueEvent[] destinationEventStorage )
	{
		final int curReadPosition = readPosition.get();
		final int curWritePosition = writePosition.get();
		final int numReadable = calcNumReadable( curReadPosition, curWritePosition );

		if( numReadable > 0 )
		{
			int numCopied = 0;
			for( int i = 0 ; i < numReadable ; i++ )
			{
				int posToCheck = curReadPosition + i;
				posToCheck = (posToCheck >= bufferLength ? posToCheck - bufferLength : posToCheck );
				COPIER.copyValues( buffer[ posToCheck ], destinationEventStorage[ i ] );
				numCopied++;
			}

			if( numCopied > 0 )
			{
				int newPosition = curReadPosition + numCopied;
				while( newPosition >= bufferLength ) newPosition -= bufferLength;

				while( !readPosition.compareAndSet( curReadPosition, newPosition ) )
				{
				}
			}
			return numCopied;
		}
		else
		{
			return 0;
		}
	}

	public final int copyToTemp( final IOQueueEvent[] destinationEventStorage, final long queuePullingFrameTime )
	{
		final int curReadPosition = readPosition.get();
		final int curWritePosition = writePosition.get();
		final int numReadable = calcNumReadable( curReadPosition, curWritePosition );

		if( numReadable > 0 )
		{
			int numCopied = 0;
			boolean done = false;
			for( int i = 0 ; !done && i < numReadable ; i++ )
			{
				int posToCheck = curReadPosition + i;
				posToCheck = (posToCheck >= bufferLength ? posToCheck - bufferLength : posToCheck );
				if( buffer[ posToCheck].frameTime <= queuePullingFrameTime )
				{
					COPIER.copyValues( buffer[ posToCheck ], destinationEventStorage[ i ] );
					numCopied++;
				}
				else
				{
					// We're done, don't want to process this one.
					done = true;
					break;
				}
			}

			if( numCopied > 0 )
			{
				int newPosition = curReadPosition + numCopied;
				while( newPosition >= bufferLength ) newPosition -= bufferLength;

				while( !readPosition.compareAndSet( curReadPosition, newPosition ) )
				{
				}
			}
			return numCopied;
		}
		else
		{
			return 0;
		}
	}
}
