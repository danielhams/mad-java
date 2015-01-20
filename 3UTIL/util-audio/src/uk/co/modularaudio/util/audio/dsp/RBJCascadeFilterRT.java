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

import uk.co.modularaudio.util.math.MathDefines;

public class RBJCascadeFilterRT
{
	public final static float ZERO_RESONANCE = (float)Math.sqrt(0.5);

	protected float b0a0, b1a0, b2a0, a1a0, a2a0;
	protected float x1, x2, y1, y2;
	protected float cx1, cx2, cy1, cy2;

	public RBJCascadeFilterRT()
	{
		b0a0 = 0;
		b1a0 = 0;
		b2a0 = 0;
		a1a0 = 0;
		a2a0 = 0;
		x1 = 0;
		x2 = 0;
		y1 = 0;
		y2 = 0;
		cx1 = 0;
		cx2 = 0;
		cy1 = 0;
		cy2 = 0;
	}
	
	public void recompute( int sampleRate, FrequencyFilterMode filterMode, float f0, float Q )
	{
		float maxFreq = (sampleRate / 2.0f) - 10.0f;
		if( f0 > maxFreq )
		{
			f0 = maxFreq;
		}
		float omega, sn, cs, alpha;
		float a0, a1, a2, b0, b1, b2;
		
		omega = (MathDefines.TWO_PI_F * f0 ) / sampleRate;
		sn = (float)Math.sin( omega );
		cs = (float)Math.cos( omega );
		alpha = sn / (2.0f * Q);

		switch( filterMode )
		{
			case LP:
			{
				b0 = (1.0f - cs) / 2.0f;
				b1 = 1.0f - cs;
				b2 = (1.0f - cs) / 2.0f;
				a0 = 1.0f + alpha;
				a1 = -2.0f * cs;
				a2 = 1.0f - alpha;
				break;
			}
			case HP:
			{
				b0 = (1.0f + cs) / 2.0f;
				b1 = -(1.0f + cs);
				b2 = (1.0f + cs) / 2.0f;
				a0 = 1.0f + alpha;
				a1 = -2.0f * cs;
				a2 = 1.0f - alpha;
				break;
			}
			case BP:
			{
				b0 = sn / 2.0f;
				b1 = 0;
				b2 = -sn / 2.0f;
				a0 = 1.0f + alpha;
				a1 = -2.0f * cs;
				a2 = 1.0f - alpha;
				break;
			}
			default:
			{
				throw new RuntimeException("Unknown filter mode: " + filterMode);
			}
		}
		
		b0a0 = b0 / a0;
		b1a0 = b1 / a0;
		b2a0 = b2 / a0;
		a1a0 = a1 / a0;
		a2a0 = a2  / a0;
		
//		x1 = 0;
//		x2 = 0;
//		y1 = 0;
//		y2 = 0;
		
//		log.debug("Recomputed filter params with f0(" + f0 + ") and Q(" + Q +")");
	}
}
