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

package uk.co.modularaudio.util.audio.pvoc.frame.synthesis;


public strictfp class PvocStreamingCubicInterpolationResampler implements PvocStreamResampler
{
//	private static Log log = LogFactory.getLog( PvocStreamingCubicInterpolationResampler.class.getName() );
	
	private float posLeftOver = 2.0f;
	
	private float lastSampleMinusThree = 0.0f;
	private float lastSampleMinusTwo = 0.0f;
	private float lastSampleMinusOne = 0.0f;
	
	public PvocStreamingCubicInterpolationResampler()
	{
	}
	
	public int streamResample( float resampleRatio,
			float[] inputData,
			int numInputSamples,
			float[] outputData )
	{
		float actualPos = posLeftOver;
		int curPos = (int)actualPos;
		int outputIndex = 0;
		
		while( curPos < numInputSamples )
		{
			float fractionalIndex = actualPos - curPos;

			if( curPos == 0 )
			{
				outputData[ outputIndex ] = interpolate( lastSampleMinusThree, lastSampleMinusTwo, lastSampleMinusOne, inputData[0], fractionalIndex );
			}
			else if( curPos == 1 )
			{
				outputData[ outputIndex ] = interpolate( lastSampleMinusTwo, lastSampleMinusOne, inputData[0], inputData[1], fractionalIndex );
			}
			else if( curPos == 2 )
			{
				outputData[ outputIndex ] = interpolate( lastSampleMinusOne, inputData[0], inputData[1], inputData[2], fractionalIndex );
			}
			else
			{
				outputData[ outputIndex ] = interpolate( inputData[ curPos - 3 ], inputData[ curPos - 2 ], inputData[ curPos - 1], inputData[ curPos ], fractionalIndex );
			}
			
			outputIndex++;
			actualPos += resampleRatio;

			curPos = (int)actualPos;
		};
		
		// How much extra floating point have we over the integer index

		posLeftOver = actualPos - numInputSamples;

		// Store the last samples so that next iteration we can use them
		lastSampleMinusThree = inputData[ numInputSamples - 3 ];
		lastSampleMinusTwo = inputData[ numInputSamples - 2 ];
		lastSampleMinusOne = inputData[ numInputSamples -1 ];

		return outputIndex;
	}
	
	public void clear()
	{
		posLeftOver = 2.0f;
		lastSampleMinusThree = 0.0f;
		lastSampleMinusTwo = 0.0f;
		lastSampleMinusOne = 0.0f;
	}

	private final float interpolate( float y0, float y1, float y2, float y3, float frac )
	{
		// Now cubic interpolate it.
		float fracsq = frac * frac;
	
		float a0 = y3 - y2 - y0 + y1;
		float a1 = y0 - y1 -a0;
		float a2 = y2 - y0;
		float a3 = y1;
	
		return( (a0 * frac * fracsq)  + (a1 * fracsq) + (a2 * frac) + a3 );
	}
	
}
