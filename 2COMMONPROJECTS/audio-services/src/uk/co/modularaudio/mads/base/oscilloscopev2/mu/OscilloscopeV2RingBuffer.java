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

package uk.co.modularaudio.mads.base.oscilloscopev2.mu;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.buffer.WriteSemaphoreLocklessRingBuffer;

public class OscilloscopeV2RingBuffer extends WriteSemaphoreLocklessRingBuffer
{
//	private static Log log = LogFactory.getLog( OscilloscopeV2RingBuffer.class.getName() );
	
	private int maxRingBufferingInSamples = -1;
	private int numSamplesLatency = -1;
	
	private boolean connected = false;
	private boolean audio = false;
	
	public OscilloscopeV2RingBuffer( int maxRingBufferingInSamples, int numSamplesLatency )
	{
		super( maxRingBufferingInSamples * 2 );
		this.maxRingBufferingInSamples = maxRingBufferingInSamples;
		this.numSamplesLatency = numSamplesLatency;
	}
	
	public void setStatus( boolean isConnected, boolean isAudio )
	{
		this.connected = isConnected;
		this.audio = isAudio;
	}
	
	public void oscilloscopeReadNumSamplesWindow( float[] target, int targetPos, int length )
	{
		// See if we have enough samples in the ring
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		int numReadable = calcNumReadable( curReadPosition, curWritePosition );
		
		int numToDisposeOf = ( numReadable > maxRingBufferingInSamples ? numReadable - maxRingBufferingInSamples : 0 );
		
		if( numToDisposeOf > 0 )
		{
			// Throw away any over the amount we actually need.
			this.moveForward( numToDisposeOf );
			numReadable -= numToDisposeOf;
			curReadPosition = readPosition.get();
			calcNumReadable( curReadPosition, curWritePosition );
		}
		
		
		int numWithoutLatency = numReadable - numSamplesLatency;
		if( numWithoutLatency >= length )
		{
			readEmOut( target, targetPos, length, curWritePosition );
		}
		else
		{
			// Don't have enough, fill with zeros for samples we don't yet have
			if( numWithoutLatency >= 0 )
			{
				int numZeros = length - numWithoutLatency;
				Arrays.fill( target, targetPos, targetPos + numZeros , 0.0f );
				readEmOut( target, targetPos + numZeros, numWithoutLatency, curWritePosition );
			}
			else
			{
				Arrays.fill( target, targetPos, length, 0.0f );
			}
		}

	}

	private void readEmOut( float[] target, int targetPos, int length,
			int curWritePosition )
	{
		int readStartPos = curWritePosition - (numSamplesLatency + length + 1);
		if( readStartPos < 0 ) readStartPos += bufferLength;
		int readEndPos = readStartPos + length;
		if( readEndPos >= bufferLength )
		{
			int numFromEnd = bufferLength - readStartPos;
			int numFromStart = length - numFromEnd;
			System.arraycopy( buffer, readStartPos, target, targetPos, numFromEnd );
			System.arraycopy( buffer, 0, target, targetPos + numFromEnd, numFromStart );
		}
		else
		{
			// just read from start to end
			System.arraycopy( buffer, readStartPos, target, targetPos, length );
		}
	}

	public void writeZeros( int numZeros )
	{
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		
		if( curWritePosition < curReadPosition )
		{
			// Write comes before read, should be able to straight fill
			Arrays.fill( buffer, curWritePosition, curWritePosition + numZeros, 0.0f );
		}
		else
		{
			if( bufferLength - curWritePosition < numZeros )
			{
				// Split over the end + start
				int numAtEnd = bufferLength - curWritePosition;
				int numAtStart = numZeros - numAtEnd;
				Arrays.fill( buffer, curWritePosition, curWritePosition + numAtEnd, 0.0f );
				Arrays.fill( buffer, 0, numAtStart, 0.0f );
			}
			else
			{
				// Fits before buffer length
				Arrays.fill( buffer, curWritePosition, curWritePosition + numZeros, 0.0f );
			}
		}
		
		int newWritePosition = curWritePosition += numZeros;
		
		if( newWritePosition >= bufferLength ) newWritePosition -= bufferLength;
		writePosition.set( newWritePosition );
		
	}

	public boolean isConnected()
	{
		return connected;
	}

	public boolean isAudio()
	{
		return audio;
	}
}
