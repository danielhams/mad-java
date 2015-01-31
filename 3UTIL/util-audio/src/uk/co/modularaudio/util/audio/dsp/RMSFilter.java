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

package uk.co.modularaudio.util.audio.dsp;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;


public class RMSFilter
{
	public float currentSumOfSquares = 0.0f;
	public UnsafeFloatRingBuffer ringOfSquares;
	public int numSamplesForMin = -1;

	public RMSFilter( final int sampleRate, final float minSmoothedFrequency )
	{
		recompute(sampleRate, minSmoothedFrequency);
	}

	public void recompute( final int sampleRate, final float minSmoothedFrequency )
	{
		// Make the ring of squares long enough for a full cycle of the min smoothed frequency
		numSamplesForMin = (int)(sampleRate / minSmoothedFrequency);

		ringOfSquares = new UnsafeFloatRingBuffer(numSamplesForMin, true);
	}

	public void reset()
	{
		currentSumOfSquares = 0.0f;
		ringOfSquares.clearToZero();
	}

	public void filter( final float[] input, final int inPos, final float[] output, final int outPos, final int length )
	{
		final int rosBufferLength = ringOfSquares.bufferLength;
		int rosReadPos = ringOfSquares.readPosition;
		int rosWritePos = ringOfSquares.writePosition;

		int numLeft = length;
		int inIndex = inPos;
		int outIndex = outPos;

		do
		{
			final int numCanReadBefore = rosBufferLength - rosReadPos;
			final int numToReadBefore = (numLeft < numCanReadBefore ? numLeft : numCanReadBefore );

			if( rosReadPos != 0 )
			{
				// Do the loop
				for( int i = 0 ; i < numToReadBefore ; ++i )
				{
					final float squareToRemove = ringOfSquares.buffer[ rosReadPos++ ];
					currentSumOfSquares -= squareToRemove;
					final float inValue = input[ inIndex++ ];
					final float newSquare = ( inValue * inValue );
					ringOfSquares.buffer[ rosWritePos++ ] = newSquare;
					currentSumOfSquares += newSquare;
					currentSumOfSquares = (currentSumOfSquares < 0.0f ? 0.0f : currentSumOfSquares );
					output[ outIndex++ ] = (float)Math.sqrt( currentSumOfSquares / numSamplesForMin );
				}
				numLeft -= numToReadBefore;
			}

			if( numLeft > 0 )
			{
				rosReadPos = 0;
				// Straddler
				final float squareToRemove = ringOfSquares.buffer[ rosReadPos++ ];
				currentSumOfSquares -= squareToRemove;
				final float inValue = input[ inIndex++ ];
				final float newSquare = ( inValue * inValue );
				ringOfSquares.buffer[ rosWritePos++ ] = newSquare;
				currentSumOfSquares += newSquare;
				output[ outIndex++ ] = (float)Math.sqrt( currentSumOfSquares / numSamplesForMin );

				numLeft--;
				rosWritePos = 0;
			}
		}
		while( numLeft > 0 );

		// Move the ring of squares read/write indexes forward
		ringOfSquares.readPosition = rosReadPos;
		ringOfSquares.writePosition = rosWritePos;
	}

}
