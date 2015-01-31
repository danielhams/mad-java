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

public class LowPassFilter
{
	private double feedbackDelaySample = 0.0;

	public LowPassFilter()
	{
	}

	public float filter( final float[] input, final float freq, final float sr )
	{
		double costh, coef;
		costh = 2.0 - Math.cos( 2 * Math.PI * freq / sr );
		coef = Math.sqrt( costh * costh - 1 ) - costh;

		for( int i = 0 ; i < input.length ; i++ )
		{
			input[i] = (float) (input[i] * (1 + coef ) - feedbackDelaySample * coef );
			feedbackDelaySample = input[i];
		}

		return input[0];
	}
}
