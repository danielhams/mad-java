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

package uk.co.modularaudio.mads.base.ms20filter.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;

public class Oversampler
{
	// One sample before, two samples after
	private static final int CUBIC_INTERPOLATE_PRE_SAMPLES = 1;
	private static final int CUBIC_INTERPOLATE_POST_SAMPLES = 2;
	private static final int CUBIC_INTERPOLATE_EXTRA_SAMPLES = CUBIC_INTERPOLATE_PRE_SAMPLES + CUBIC_INTERPOLATE_POST_SAMPLES;

	private static Log log = LogFactory.getLog( Oversampler.class.getName() );
	
//	private int undersampledSampleRate = -1;
	private int undersampledPeriodLength = -1;
	private int oversampleRatio = -1;
	private float oneOverOversampleRatio = 0.0f;
	private int oversampledPeriodLength = -1;
	
	private float[] internalBuffer = null;
	
	private UnsafeFloatRingBuffer inputRingBuffer = null;
	
	private UnsafeFloatRingBuffer outputRingBuffer = null;
	
	public Oversampler( int sampleRate, int periodLength, int oversampleRatio )
	{
//		this.undersampledSampleRate = sampleRate;
		this.undersampledPeriodLength = periodLength;
		this.oversampleRatio = oversampleRatio;
		this.oneOverOversampleRatio = 1.0f / oversampleRatio;
		this.oversampledPeriodLength = periodLength * oversampleRatio;
		
		inputRingBuffer = new UnsafeFloatRingBuffer( oversampledPeriodLength );
		// Prime the input ring buffer with one sample (for the cubic interpolation)
		inputRingBuffer.writeOne( 0.0f );
		
		outputRingBuffer = new UnsafeFloatRingBuffer( oversampledPeriodLength + CUBIC_INTERPOLATE_EXTRA_SAMPLES );
		outputRingBuffer.writeOne( 0.0f );
		
		internalBuffer = new float[ oversampledPeriodLength + CUBIC_INTERPOLATE_EXTRA_SAMPLES ];
	}
	
	public int getOversampledPeriodLength()
	{
		return oversampledPeriodLength;
	}
	
	public int oversample( float[] input, int numFrames, float[] output )
	{
		inputRingBuffer.write( input, 0, numFrames );

		int numInRing = inputRingBuffer.getNumReadable();

		int numAvailableForResampling = numInRing - CUBIC_INTERPOLATE_EXTRA_SAMPLES;
		int numAsInputThisRound = (numAvailableForResampling < undersampledPeriodLength ? numAvailableForResampling : undersampledPeriodLength );
		int numToReallyRead = numAsInputThisRound;
		inputRingBuffer.read( internalBuffer, 0, numToReallyRead );

		// Leave the last sample in the ring buffer so it's re-used next time around
		inputRingBuffer.readNoMove( internalBuffer, numToReallyRead, CUBIC_INTERPOLATE_EXTRA_SAMPLES );
		
		int numOutputThisRound = numAsInputThisRound * oversampleRatio;
				
		for( int n = 0 ; n < numOutputThisRound ; n++ )
		{
			// We add one to start at index 1
			float inputPos = 1.0f + ( n * oneOverOversampleRatio );

			// Now interpolate it.
			float y0, y1;
			// For cubic
//			float y2, y3;
			
			// Cubic interpolation
			int intPos = (int)inputPos;
//			if( (intPos) < 1 || (intPos + 2) > numOutputThisRound )
//			{
//				log.error("Overstepped the limits.");
//			}
			y0 = internalBuffer[ intPos - 1 ];
			y1 = internalBuffer[ intPos ];
			// For cubic
//			y2 = internalBuffer[ intPos + 1 ];
//			y3 = internalBuffer[ intPos + 2 ];
			
//			float newSample = cubicInterpolate( inputPos, y0, y1, y2, y3, intPos );
			float newSample = linearInterpolate( inputPos, y0, y1, intPos );
			
			output[ n ] = newSample;
		}
		return numOutputThisRound;
	}

	protected float cubicInterpolate( float inputPosFloat,
			float y0, float y1, float y2, float y3,
			int inputPosInt )
	{
		float frac,  fracsq;
		frac = inputPosFloat - inputPosInt;
		fracsq = frac * frac;

		float a0 = y3 - y2 - y0 + y1;
		float a1 = y0 - y1 -a0;
		float a2 = y2 - y0;
		float a3 = y1;

		float newSample = ( a0 * frac * fracsq  + a1 * fracsq + a2 * frac + a3 );
		return newSample;
	}
	
	protected float linearInterpolate( float inputPosFloats,
			float y0,
			float y1,
			int inputPosInt )
	{
		float frac = inputPosFloats - inputPosInt;
		return ( (y0 * (1.0f - frac)) + (y1 * frac ) );
	}
	
	public int undersample( float[] input, int numFramesInput, float[] output )
	{
		outputRingBuffer.write( input, 0, numFramesInput );

		int numInRing = outputRingBuffer.getNumReadable();

		int numAvailableForResampling = numInRing - CUBIC_INTERPOLATE_EXTRA_SAMPLES;
		int numAsInputThisRound = (numAvailableForResampling < oversampledPeriodLength ? numAvailableForResampling : oversampledPeriodLength );
		int numToReallyRead = numAsInputThisRound;
		outputRingBuffer.read( internalBuffer, 0, numToReallyRead );

		// Leave the last sample in the ring buffer so it's re-used next time around
		outputRingBuffer.readNoMove( internalBuffer, numToReallyRead, CUBIC_INTERPOLATE_EXTRA_SAMPLES );
		
		int numOutputThisRound = numAsInputThisRound / oversampleRatio;
		
		for( int n = 0 ; n < numOutputThisRound ; n++ )
		{
			// We add one to start at index 1
			float inputPos = 1.0f + ( n * oversampleRatio );

			// Now cubic interpolate it.
//			float y0, y1, y2, y3;
			float y0, y1;
			
			// Cubic interpolation
			int intPos = (int)inputPos;
			if( (intPos) < 1 || (intPos + 2) > numToReallyRead )
			{
				log.error("Overstepped the limits.");
			}
			y0 = internalBuffer[ intPos - 1 ];
			y1 = internalBuffer[ intPos ];
//			y2 = internalBuffer[ intPos + 1 ];
//			y3 = internalBuffer[ intPos + 2 ];
			
//			float newSample = cubicInterpolate( inputPos, y0, y1, y2, y3, intPos );
			float newSample = linearInterpolate( inputPos, y0, y1, intPos );
			
			output[ n ] = newSample;
		}
		return numOutputThisRound;
	}

}
