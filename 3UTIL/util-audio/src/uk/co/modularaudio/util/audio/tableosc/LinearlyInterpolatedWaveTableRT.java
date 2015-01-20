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

package uk.co.modularaudio.util.audio.tableosc;

import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;

public class LinearlyInterpolatedWaveTableRT
{
//	private static Log log = LogFactory.getLog( LinearlyInterpolatedWaveTableRT.class.getName() );
	
	RawWaveTable waveTable = null;
	float currentPosition = -1;
	float[] floatBuffer = null;
	int floatBufferLength = -1;
	
	public LinearlyInterpolatedWaveTableRT( RawWaveTable iWaveTable )
	{
		// Make the table one larger
		this.waveTable = new RawWaveTable( iWaveTable.capacity + 1, false );
		// Copy over the data duplicating the first value at the end
		System.arraycopy( iWaveTable.floatBuffer, 0, this.waveTable.floatBuffer, 0, iWaveTable.capacity );
		this.waveTable.floatBuffer[iWaveTable.capacity] = this.waveTable.floatBuffer[0];
		currentPosition = 0.0f;
		floatBuffer = this.waveTable.floatBuffer;
		// Use the original length
		floatBufferLength = iWaveTable.floatBuffer.length;
	}
	
	public float oscillate( float[] output,
			float freq,
			float phase,
			int outputIndex,
			int length,
			long sampleRate)
	{
		float incr = freq * floatBufferLength / sampleRate;
		phase = ( phase < 0 ? phase + 1 : phase );
		float pos, frac, a, b;
		int offset = (int)(phase*floatBufferLength)%floatBufferLength;
		
		for( int i = 0 ; i < length ; i++ )
		{
			pos = currentPosition + offset;
			if( pos > floatBufferLength )
			{
//				log.debug("It happened!");
				pos = pos - floatBufferLength;
			}
			
			// Linear interpolation
			frac = pos - (int)pos;
			a = floatBuffer[(int)pos];
			b = floatBuffer[(int)pos+1];
			float bMinusA = b - a;
			float fracTimesBMinusA = frac * bMinusA;
			float aPlusStuff = a + fracTimesBMinusA;
			output[ outputIndex + i] = aPlusStuff;
			
			currentPosition += incr;
			
			while( (int)currentPosition >= floatBufferLength ) currentPosition -= floatBufferLength;
			while( (int)currentPosition < 0 ) currentPosition += floatBufferLength;
		}
		return output[0];
	}
}
