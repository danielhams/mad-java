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



public class LinearInterpolator implements Interpolator
{
//	private static Log log = LogFactory.getLog( LinearInterpolator.class.getName() );

	@Override
	public final float interpolate( final float[] sourceBuffer, final int pos, final float frac )
	{
		final float y0 = sourceBuffer[pos];
		final float y1 = sourceBuffer[pos+1];
//		if( Math.abs(y0) > BlockResamplerService.EXCESSIVE_FLOAT ||
//				Math.abs(y1) > BlockResamplerService.EXCESSIVE_FLOAT )
//		{
//			log.error("Failed on frame " + pos + " with vals " + y0 +
//					" " + y1 );
//		}
		return ((1.0f - frac) * y0) + (frac * y1);
	}
}
