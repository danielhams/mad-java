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


public class RMSFilter
{
	public static void filterIt( RMSFilterRT rt, float[] input, int inPos, float[] output, int outPos, int length )
	{
		float currentSumOfSquares = rt.currentSumOfSquares;
		
		int rosBufferLength = rt.ringOfSquares.bufferLength;
		int rosReadPos = rt.ringOfSquares.readPosition;
		int rosWritePos = rt.ringOfSquares.writePosition;

		int numLeft = length;
		int inIndex = inPos;
		int outIndex = outPos;
		
		do
		{
			int numCanReadBefore = rosBufferLength - rosReadPos;
			int numToReadBefore = (numLeft < numCanReadBefore ? numLeft : numCanReadBefore );

			if( rosReadPos != 0 )
			{
				// Do the loop
				for( int i = 0 ; i < numToReadBefore ; ++i )
				{
					float squareToRemove = rt.ringOfSquares.buffer[ rosReadPos++ ];
					currentSumOfSquares -= squareToRemove;
					float inValue = input[ inIndex++ ];
					float newSquare = ( inValue * inValue );
					rt.ringOfSquares.buffer[ rosWritePos++ ] = newSquare;
					currentSumOfSquares += newSquare;
					currentSumOfSquares = (currentSumOfSquares < 0.0f ? 0.0f : currentSumOfSquares );
					output[ outIndex++ ] = (float)Math.sqrt( currentSumOfSquares / rt.numSamplesForMin );
				}
				numLeft -= numToReadBefore;
			}
			
			if( numLeft > 0 )
			{
				rosReadPos = 0;
				// Straddler
				float squareToRemove = rt.ringOfSquares.buffer[ rosReadPos++ ];
				currentSumOfSquares -= squareToRemove;
				float inValue = input[ inIndex++ ];
				float newSquare = ( inValue * inValue );
				rt.ringOfSquares.buffer[ rosWritePos++ ] = newSquare;
				currentSumOfSquares += newSquare;
				output[ outIndex++ ] = (float)Math.sqrt( currentSumOfSquares / rt.numSamplesForMin );
				
				numLeft--;
				rosWritePos = 0;
			}
		}
		while( numLeft > 0 );
		
		// Move the ring of squares read/write indexes forward
		rt.ringOfSquares.readPosition = rosReadPos;
		rt.ringOfSquares.writePosition = rosWritePos;
		
		rt.currentSumOfSquares = currentSumOfSquares;
	}
	
}
