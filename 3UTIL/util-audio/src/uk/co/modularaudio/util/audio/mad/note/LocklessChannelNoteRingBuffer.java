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

package uk.co.modularaudio.util.audio.mad.note;

import java.nio.BufferUnderflowException;

import uk.co.modularaudio.util.audio.buffer.LocklessPreallocatingGenericRingBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventCopier;

public class LocklessChannelNoteRingBuffer extends LocklessPreallocatingGenericRingBuffer<MadChannelNoteEvent>
{
	private final static MadChannelNoteEventCopier COPIER = new MadChannelNoteEventCopier();

	public LocklessChannelNoteRingBuffer( final int capacity )
	{
		super( MadChannelNoteEvent.class, COPIER, capacity );
		// Now initialise empty objects inside the internal array
		for( int i = 0 ; i < capacity ; i++ )
		{
			buffer[ i ] = new MadChannelNoteEvent();
		}
	}

	@Override
	public int read( final MadChannelNoteEvent[] target, final int pos, final int length )
			throws BufferUnderflowException
	{
		return super.read( target, pos, length );
	}

	@Override
	public int write( final MadChannelNoteEvent[] source, final int pos, final int length )
	{
		return super.write( source, pos, length );
	}

	public void peekCopyToDest( final MadChannelNoteEvent dest )
	{
		final int curReadPosition = readPosition.get();
		final int curWritePosition = writePosition.get();
		final int numReadable = calcNumReadable( curReadPosition, curWritePosition );
		if( numReadable > 0 )
		{
			COPIER.copyValues( buffer[ curReadPosition ], dest );
		}
		else
		{
			dest.reset();
		}
	}

	public void moveForward( final int readAmount )
	{
		final int curReadPosition = readPosition.get();
		int newPosition = curReadPosition + readAmount;

		if( newPosition > bufferLength )
		{
			newPosition -= bufferLength;
		}
		while( !readPosition.compareAndSet( curReadPosition, newPosition ) )
		{
		}
	}

}
