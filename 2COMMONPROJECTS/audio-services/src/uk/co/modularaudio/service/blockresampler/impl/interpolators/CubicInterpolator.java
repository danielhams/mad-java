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

package uk.co.modularaudio.service.blockresampler.impl.interpolators;

public class CubicInterpolator implements Interpolator
{
//	private static Log log = LogFactory.getLog( CubicInterpolator.class.getName() );

	@Override
	public final float interpolate( final float[] sourceBuffer, final int pos, final float frac )
	{
		final float y0 = sourceBuffer[ pos - 1];
		final float y1 = sourceBuffer[ pos ];
		final float y2 = sourceBuffer[ pos + 1 ];
		final float y3 = sourceBuffer[ pos + 2 ];
//		log.debug("CubicInterpolate between y0(" + y0 + ") y1(" + y1 + ") y2(" + y2 + ") y3(" + y3 + ")");

//		if( Math.abs(y0) >= BlockResamplerService.EXCESSIVE_FLOAT ||
//				Math.abs(y1) >= BlockResamplerService.EXCESSIVE_FLOAT ||
//				Math.abs(y2) >= BlockResamplerService.EXCESSIVE_FLOAT ||
//				Math.abs(y3) >= BlockResamplerService.EXCESSIVE_FLOAT )
//		{
//			log.error("Failed on frame " + pos + " with vals " + y0 +
//					" " + y1 +
//					" " + y2 +
//					" " + y3 );
//		}

		final float fracSq = frac * frac;

//		float a0 = y3 - y2 - y0 + y1;
//		float a1 = y0 - y1 - a0;
//		float a2 = y2 - y0;
//		float a3 = y1;

		final float a0 = -0.5f*y0 + 1.5f*y1 - 1.5f*y2 + 0.5f*y3;
		final float a1 = y0 - 2.5f*y1 + 2.0f*y2 - 0.5f*y3;
		final float a2 = -0.5f*y0 + 0.5f*y2;
		final float a3 = y1;


		final float result = (a0 * frac * fracSq) + (a1 * fracSq) + (a2 * frac) + a3;

//		if( Math.abs(result)  >= BlockResamplerService.EXCESSIVE_FLOAT )
//		{
//			log.debug("Dicky value at index " + pos + ":" + frac );
//			final float[] debugArray = new float[6];
//			System.arraycopy( sourceBuffer, pos - 2, debugArray, 0, 6 );
//			log.debug("The six around where we are: " + Arrays.toString( debugArray ) );
//		}

		return result;
	}
}
