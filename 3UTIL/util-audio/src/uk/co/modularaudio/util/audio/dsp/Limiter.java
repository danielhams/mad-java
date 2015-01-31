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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Limiter
{
	public static Log log = LogFactory.getLog( Limiter.class.getName() );
	
	private double knee = 0.0;
	private double upperLeg = 0.0;
	private double falloff = 0.0;
	
	private float kneeFloat = 0.0f;
	private float upperLegFloat = 0.0f;
	private float falloffFloat = 0.0f;
	
	public Limiter( double knee, double falloff )
	{
		this.knee = knee;
		this.upperLeg = 1.0 - knee;
		this.falloff = falloff;
		
		kneeFloat = (float)knee;
		upperLegFloat = (float)upperLeg;
		falloffFloat = (float)falloff;
	}
	
	public double filter( double[] testVals, int position, int length )
	{
		for( int i = position ; i < position + length ; i++ )
		{
			double testVal = testVals[ i ];
			int sign = ( testVal < 0.0f ? -1 : 1 );
			double absVal = testVal * sign;
			if( absVal > kneeFloat )
			{
				double amountOver = absVal - kneeFloat;
				double firstCompute = 1.0 / (( amountOver * falloffFloat ) + 1.0);
//				double secondCompute = (-1.0 * firstCompute) + 1.0;
				double secondCompute = 1.0 - firstCompute;
				double scaledVal = upperLegFloat * secondCompute;
				double retVal = (kneeFloat + scaledVal) * sign;
				testVals[i] = retVal;
			}
//			else
//			{
//				testVals[i] = testVal;
//			}
		}
		return testVals[0];
	}

	public float filter( float[] testVals, int position, int length )
	{
		for( int i = position ; i < position + length ; i++ )
		{
			float testVal = testVals[ i ];
			int sign = ( testVal < 0.0f ? -1 : 1 );
			float absVal = testVal * sign;
			if( absVal > kneeFloat )
			{
				float amountOver = absVal - kneeFloat;
				float firstCompute = 1.0f / (( amountOver * falloffFloat ) + 1.0f);
//				float secondCompute = (-1.0f * firstCompute) + 1.0f;
				float secondCompute = 1.0f - firstCompute;
				float scaledVal = upperLegFloat * secondCompute;
				float retVal = (kneeFloat + scaledVal) * sign;
				testVals[i] = retVal;
			}
//			else
//			{
//				testVals[i] = testVal;
//			}
		}
		return testVals[0];
	}

	public double getKnee()
	{
		return knee;
	}

	public void setKnee(double knee)
	{
		this.knee = knee;
		this.upperLeg = 1.0 - knee;
		this.kneeFloat = (float)knee;
		this.upperLegFloat = 1.0f - kneeFloat;
	}

	public double getFalloff()
	{
		return falloff;
	}

	public void setFalloff(double falloff)
	{
		this.falloff = falloff;
		this.falloffFloat = (float)falloff;
	}
}
