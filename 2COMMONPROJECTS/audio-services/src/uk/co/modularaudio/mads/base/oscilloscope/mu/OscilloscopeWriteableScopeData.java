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

package uk.co.modularaudio.mads.base.oscilloscope.mu;


public class OscilloscopeWriteableScopeData
{
	public enum FloatType
	{
		AUDIO, CV
	};
	public float[] floatBuffer0 = null;
	public FloatType floatBuffer0Type = FloatType.AUDIO;
	public float[] floatBuffer1 = null;
	public FloatType floatBuffer1Type = FloatType.AUDIO;
	public boolean float0Written = false;
	public boolean float1Written = false;
	public boolean written = false;
	public int currentWriteIndex = 0;
	public int desiredDataLength = -1;
	public int internalBufferLength = -1;
	
	public OscilloscopeWriteableScopeData( int bufferLength )
	{
		this.internalBufferLength = bufferLength;
		floatBuffer0 = new float[ internalBufferLength ];
		floatBuffer1 = new float[ internalBufferLength ];
	}
}
