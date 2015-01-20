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

package uk.co.modularaudio.util.audio.stft.tools;

import uk.co.modularaudio.util.audio.buffer.UnsafeOverlapAndAddRingBuffer;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class OverlapAdder
{
//	private static Log log = LogFactory.getLog( OverlapAdder.class.getName() );
	
	private int stepSize;
	private int ringBufferCapacity;
	private UnsafeOverlapAndAddRingBuffer ringBuffer;
	
	private int position;
	
	public OverlapAdder( int inputStepSize, int numOverlaps )
	{
		this.stepSize = inputStepSize;
		// We overlap the step size N times before we get output
		ringBufferCapacity = 4 * (inputStepSize * (numOverlaps + 1));
		ringBuffer = new UnsafeOverlapAndAddRingBuffer( ringBufferCapacity );
		position = ringBuffer.readPosition;

		reset();
	}

	public final RealtimeMethodReturnCodeEnum addOverlap( float[] frameData, int stepSize, int lengthOfData )
	{
		// The first stepsize floats
		ringBuffer.add( frameData, 0, stepSize );
		position = ringBuffer.readPosition;
		ringBuffer.addNoMove( frameData, stepSize, lengthOfData - stepSize );
		
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public final void readOutput( float[] output, int stepSize )
	{
		ringBuffer.readAndZero( output, 0, stepSize );
	}

	public float[] getRingBuffer()
	{
		return ringBuffer.buffer;
	}

	public int getOutputRingPosition()
	{
		return position;
	}

	public void reset()
	{
		ringBuffer.clear();		
		position = ringBuffer.readPosition;

		// Need to "prime" the ring buffer to have enough zeros that when reading we aren't moving over the
		// currently add/overlap region
		float[] zero = new float[1];
		zero[0] = 0.0f;
		for( int i = 0 ; i < stepSize ; i++ )
		{
			ringBuffer.write( zero, 0, 1 );
		}
	}

}
