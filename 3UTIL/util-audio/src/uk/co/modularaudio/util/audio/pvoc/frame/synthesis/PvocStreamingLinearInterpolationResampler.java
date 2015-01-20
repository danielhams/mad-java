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


public strictfp class PvocStreamingLinearInterpolationResampler implements PvocStreamResampler
{
//	private static Log log = LogFactory.getLog( PvocStreamingLinearInterpolationResampler.class.getName() );
	
	private float posLeftOver = 2.0f;
	
	private float lastSample = 0.0f;
	
	public PvocStreamingLinearInterpolationResampler()
	{
	}
	
	public strictfp int streamResample( float resampleRatio,
			float[] inputData,
			int numInputSamples,
			float[] outputData )
	{
		float actualPos = posLeftOver;
		int curPos = (int)actualPos;
		int outputIndex = 0;
		
		do
		{
			float fractionalIndex = actualPos - curPos;
			if( curPos == 0 )
			{
				outputData[ outputIndex ] = interpolate( lastSample, inputData[0], fractionalIndex );
			}
			else
			{
				int inDataPos = curPos - 1;
				outputData[ outputIndex ] = interpolate( inputData[ inDataPos ], inputData[ inDataPos + 1 ], fractionalIndex );
			}
			outputIndex++;
			actualPos += resampleRatio;
			curPos = (int)actualPos;
		}
		while( curPos < numInputSamples );
		
		posLeftOver = actualPos - numInputSamples;
		lastSample = inputData[ numInputSamples - 1 ];

		return outputIndex;
	}
	
	public void clear()
	{
		posLeftOver = 2.0f;
		lastSample = 0.0f;
	}

	private strictfp final float interpolate( float y0, float y1, float frac )
	{
		return (y0 +(frac * (y1 - y0) ) );
	}
	
}
