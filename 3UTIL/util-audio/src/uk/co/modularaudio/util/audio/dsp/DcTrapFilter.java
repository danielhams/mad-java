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

import uk.co.modularaudio.util.audio.math.AudioMath;


public class DcTrapFilter
{
	private float r;
	private float previousValue;
	private float previousOutValue;

	public DcTrapFilter( final int sampleRate )
	{
		recomputeR( sampleRate );
	}

	public final void recomputeR( final int sampleRate )
	{
		// -3dB @ 40hz  R = 1 - (250 / sampleRate)
		// -3dB @ 30hz  R = 1 - (190 / sampleRate)
		// -3dB @ 20hz  R = 1 - (126 / sampleRate)
		r = 1.0f - (126.0f / sampleRate );
	}

	public void filter( final float[] samples, final int position, final int length )
	{
		float tmpPrevValue = previousValue;
		for( int i = position ; i < position + length ; ++i )
		{
			float curValue = samples[i];
			if( curValue > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && curValue < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				curValue = 0.0f;
			}
			previousOutValue = curValue - tmpPrevValue + (r * previousOutValue );
			if( previousOutValue > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && previousOutValue < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				previousOutValue = 0.0f;
			}
			samples[i] = previousOutValue;
			tmpPrevValue = curValue;
		}
		previousValue = tmpPrevValue;
	}
}
