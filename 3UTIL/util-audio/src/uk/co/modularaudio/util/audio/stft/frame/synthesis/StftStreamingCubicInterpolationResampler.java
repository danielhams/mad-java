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

package uk.co.modularaudio.util.audio.stft.frame.synthesis;




public class StftStreamingCubicInterpolationResampler implements StftStreamResampler
{
//	private static Log log = LogFactory.getLog( StftStreamingCubicInterpolationResampler.class.getName());

	private float posLeftOver = 2.0f;
	
	private float lastSampleMinusThree = 0.0f;
	private float lastSampleMinusTwo = 0.0f;
	private float lastSampleMinusOne = 0.0f;
	
	public StftStreamingCubicInterpolationResampler()
	{
	}
	
	public int streamResample( float resampleRatio,
			float[] inputData,
			int numInputSamples,
			float[] outputData )
	{
		float iterationIncrementAmount = resampleRatio;
//		log.debug("Have " + numInputSamples + " to resample at fractional increment of " + iterationIncrementAmount);
		
		float actualPos = posLeftOver;
		int curPos = (int)actualPos;
		int outputIndex = 0;
		
		while( curPos < numInputSamples )
		{
			float fractionalIndex = actualPos - curPos;
//			log.debug("Curpos currently " + curPos + " with frac " + fractionalIndex );
			if( curPos == 0 )
			{
				outputData[ outputIndex ] = cinterpolate( lastSampleMinusThree, lastSampleMinusTwo, lastSampleMinusOne, inputData[0], fractionalIndex );
			}
			else if( curPos == 1 )
			{
				outputData[ outputIndex ] = cinterpolate( lastSampleMinusTwo, lastSampleMinusOne, inputData[0], inputData[1], fractionalIndex );
			}
			else if( curPos == 2 )
			{
				outputData[ outputIndex ] = cinterpolate( lastSampleMinusOne, inputData[0], inputData[1], inputData[2], fractionalIndex );
			}
			else
			{
				outputData[ outputIndex ] = cinterpolate( inputData[ curPos - 3 ], inputData[ curPos - 2 ], inputData[ curPos - 1], inputData[ curPos ], fractionalIndex );
			}
//			if( outputData[ outputIndex ] != 0.0f )
//			{
//				log.debug("Output " + outputData[ outputIndex ] );
//			}

			outputIndex++;
			actualPos += iterationIncrementAmount;
//			log.debug("Actualpos updated to " + actualPos );
			curPos = (int)actualPos;
		};
		
		// How much extra floating point have we over the integer index
//		log.debug("Previous posLeftOver was " + posLeftOver );
		posLeftOver = actualPos - curPos - (numInputSamples - curPos);
//		log.debug("Leaving posLeftOver at " + posLeftOver );
		// Store the last samples so that next iteration we can use them
//		log.debug("Pulling from " + (curPos - 3) + " to " + (curPos - 1) );
		lastSampleMinusThree = inputData[ numInputSamples - 3 ];
		lastSampleMinusTwo = inputData[ numInputSamples - 2 ];
		lastSampleMinusOne = inputData[ numInputSamples -1 ];

		return outputIndex;
	}
	
	public void clear()
	{
		posLeftOver = 0.0f;
		lastSampleMinusThree = 0.0f;
		lastSampleMinusTwo = 0.0f;
		lastSampleMinusOne = 0.0f;
	}

	protected final float linterpolate( float y0, float y1, float y2, float y3, float frac )
	{
		return y1 + ((y2 - y1) * frac);
	}
	
	protected final float cinterpolate( float y0, float y1, float y2, float y3, float frac )
	{
		// Now cubic interpolate it.
		float fracsq;
		
		// Cubic interpolation
		fracsq = frac * frac;
	
		float a0 = y3 - y2 - y0 + y1;
		float a1 = y0 - y1 -a0;
		float a2 = y2 - y0;
		float a3 = y1;
	
		return( (a0 * frac * fracsq)  + (a1 * fracsq) + (a2 * frac) + a3 );
	}
	
	protected final float p4o3bsplineinterpolate( float y0, float y1, float y2, float y3, float frac )
	{
		float ym1py1 = y0 + y2;
		float c0 = (1.0f / 6.0f * ym1py1) + (2.0f / 3.0f * y1);
		float c1 = 1.0f / 2.0f * ( y2 - y0 );
		float c2 = (1.0f / 2.0f * ym1py1) - y1;
		float c3 = (1.0f / 2.0f * (y1 - y0 )) + (1.0f / 6.0f * (y3-y0));
		
		return ((c3*frac+c2) * frac + c1) * frac + c0;
	}
}
