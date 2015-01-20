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

public class TruncatingWaveTableRT
{
//	private RawWaveTable waveTable = null;
	private double currentPosition = -1;
	private float[] floatBuffer = null;
	private int floatBufferLength = -1;

	public TruncatingWaveTableRT( RawWaveTable waveTable )
	{
		setWaveTable( waveTable );
		currentPosition = 0;
	}
	
	public float oscillate( float[] output,
			float freq,
			int outputIndex,
			int length,
			long sampleRate)
	{
		float incr = freq * floatBufferLength / sampleRate;
		
		for( int i = 0 ; i < length ; i++ )
		{
			output[outputIndex + i] = floatBuffer[ (int)currentPosition ];
			currentPosition += incr;
			while( currentPosition >= floatBufferLength ) currentPosition -= floatBufferLength;
			while( currentPosition < 0 ) currentPosition += floatBufferLength;
		}
		return output[outputIndex];
	}
	
	public float oscillate( float[] output,
			float[] freqs,
			int outputIndex,
			int length,
			long sampleRate )
	{
		float incrMultiplier = (float)floatBufferLength / (float)sampleRate;
		
		for( int i = 0 ; i < length ; i++ )
		{
			float incr = freqs[i] * incrMultiplier;
			output[outputIndex + i] = floatBuffer[ (int)currentPosition ];
			currentPosition += incr;
			while( currentPosition >= floatBufferLength ) currentPosition -= floatBufferLength;
			while( currentPosition < 0 ) currentPosition += floatBufferLength;
		}
		return output[outputIndex];
	}

	public void setWaveTable( RawWaveTable waveTable )
	{
//		currentPosition = 0;
		floatBuffer = waveTable.floatBuffer;
		floatBufferLength = floatBuffer.length;
	}
}
