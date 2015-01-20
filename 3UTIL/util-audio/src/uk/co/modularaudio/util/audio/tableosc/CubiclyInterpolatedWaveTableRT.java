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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;

public class CubiclyInterpolatedWaveTableRT
{
	private static Log log = LogFactory.getLog( CubiclyInterpolatedWaveTableRT.class.getName() );
	
	RawWaveTable waveTable = null;
	float currentPosition = -1;
	float[] floatBuffer = null;
	int floatBufferLength = -1;
	
	public CubiclyInterpolatedWaveTableRT( RawWaveTable waveTable )
	{
		// Make the table one larger
		this.waveTable = new RawWaveTable( waveTable.capacity + 2, false );
		// Copy over the data duplicating the first value at the end
		System.arraycopy( waveTable.floatBuffer, 0, this.waveTable.floatBuffer, 0, waveTable.capacity );
		this.waveTable.floatBuffer[waveTable.capacity -1] = this.waveTable.floatBuffer[0];
		this.waveTable.floatBuffer[waveTable.capacity] = this.waveTable.floatBuffer[1];
		currentPosition = 0.0f;
		floatBuffer = this.waveTable.floatBuffer;
		// Use the original length
		floatBufferLength = waveTable.floatBuffer.length;
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

		float pos, frac, y0, y1, y2, y3, fracsq, fracb,tmp;
		
		int offset = (int)(phase*floatBufferLength)%floatBufferLength;
		
		for( int i = 0 ; i < length ; i++ )
		{
			pos = currentPosition + offset;
			if( pos > floatBufferLength )
			{
				log.debug("It happened!");
				pos = pos % floatBufferLength;
			}
			
			// Cubic interpolation
			frac = pos - (int)pos;
			y0 = ((int)pos > 0 ? floatBuffer[ (int)pos-1] : floatBuffer[ floatBufferLength - 1] );
			y1 = floatBuffer[(int)pos];
			y2 = floatBuffer[(int)pos + 1];
			y3 = floatBuffer[(int)pos + 2];
			
			tmp = y3 + 3.0f * y1;
			fracsq = frac * frac;
			fracb = frac * fracsq;
			
			output[outputIndex + i] = (fracb * (- y0 - 3.0f * y2 + tmp ) / 6.0f +
					fracsq * ( (y0 + y2)  / 2.0f - y1 ) +
					frac * (y2 + (-2.0f * y0 - tmp) / 6.0f) + y1);
			
			currentPosition += incr;
			
			while( currentPosition >= floatBufferLength ) currentPosition -= floatBufferLength;
			while( currentPosition < 0 ) currentPosition += floatBufferLength;
		}
		return output[0];
	}
}
