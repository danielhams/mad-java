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

import uk.co.modularaudio.util.math.FastMath;

public class LimiterTanhApprox
{
	public static Log log = LogFactory.getLog( LimiterTanhApprox.class.getName() );

	private double knee = 0.0;
	private double upperLeg = 0.0;
	private double falloff = 0.0;

	private float kneeFloat = 0.0f;
	private float upperLegFloat = 0.0f;
	private float falloffFloat = 0.0f;

	public LimiterTanhApprox( final double knee, final double falloff )
	{
		this.knee = knee;
		this.upperLeg = 1.0 - knee;
		this.falloff = falloff;

		kneeFloat = (float)knee;
		upperLegFloat = (float)upperLeg;
		falloffFloat = (float)falloff;
	}

	public double getKnee()
	{
		return knee;
	}

	public void setKnee(final double knee)
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

	public void setFalloff(final double falloff)
	{
		this.falloff = falloff;
		this.falloffFloat = (float)falloff;
	}

	public void filter( final float[] output, final int outOffset, final int length,
			final float[] kneeFloats, final int kneeTmpIndex,
			final float[] falloffFloats, final int falloffTmpIndex )
	{
		for( int s = 0 ; s < length ; ++s )
		{
			final float knee = kneeFloats[kneeTmpIndex+s];
			final float value = output[outOffset+s];

			final int sign = ( value < 0.0f ? -1 : 1 );
			final float absVal = value * sign;

			if( knee >= 1.0f )
			{
				output[outOffset+s] = sign * (absVal < 1.0f ? absVal : 1.0f );
			}
			else if( absVal > knee )
			{
				final float upperLeg = 1.0f - knee;
				final float falloff = falloffFloats[falloffTmpIndex+s];

				final float amountOver = absVal - knee;
				final float normalisedAmountOver = (amountOver / upperLeg);

				final float normalisedScalor = normalisedAmountOver * falloff;

				final float mappedValue = FastMath.fastApproxTanh( normalisedScalor );

				final float scaledAmountOverValue = upperLeg * mappedValue;
				output[outOffset+s] = (knee + scaledAmountOverValue) * sign;
			}
		}
	}

	public void filter( final float[] output, final int outOffset, final int length )
	{
		if( knee >= 1.0f )
		{
			final int lastIndex = outOffset + length;
			for( int s = outOffset ; s < lastIndex ; ++s )
			{
				final float value = output[outOffset+s];

				final int sign = ( value < 0.0f ? -1 : 1 );
				final float absVal = value * sign;
				output[s] = sign * (absVal < 1.0f ? absVal : 1.0f);
			}
		}
		else
		{
			for( int s = 0 ; s < length ; ++s )
			{
				final float value = output[outOffset+s];

				final int sign = ( value < 0.0f ? -1 : 1 );
				final float absVal = value * sign;
				if( absVal > knee )
				{
					final float amountOver = absVal - kneeFloat;
					final float normalisedAmountOver = (amountOver / upperLegFloat);

					final float normalisedScalor = normalisedAmountOver * falloffFloat;

					final float mappedValue = FastMath.fastApproxTanh( normalisedScalor );

					final float scaledAmountOverValue = upperLegFloat * mappedValue;
					output[outOffset+s] = (kneeFloat + scaledAmountOverValue) * sign;
				}
			}
		}
	}

}
