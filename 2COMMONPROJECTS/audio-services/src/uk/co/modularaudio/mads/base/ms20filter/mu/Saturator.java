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

package uk.co.modularaudio.mads.base.ms20filter.mu;


public class Saturator
{
//	private static Log log = LogFactory.getLog( Saturator.class.getName() );

//	private Limiter limiter = new Limiter();
//	private LimiterRT limiterRt = new LimiterRT( 0.98f, 5.0f );

	public Saturator()
	{
	}

	public float processValue( final float filterThreshold, final float value )
	{
//		limiterRt.setFalloff( 0.0 );
//		limiterRt.setKnee( saturationThreshold );
//		return limiterRt.limitIt( value );

		final int sign = ( value < 0.0f ? -1 : 1 );
		final float absValue = sign * value;
		if( absValue > filterThreshold )
		{
//			log.debug("Saturator throttling value: " + absValue );
			float amountOver = absValue - filterThreshold;
			amountOver = (amountOver > filterThreshold ? filterThreshold : amountOver );
			float haveWe = filterThreshold - amountOver;
			haveWe = (haveWe < 0.0f ? 0.0f : haveWe );
			final float retVal = sign * haveWe;
//			float retVal = sign * (saturationThreshold - ( amountOver ) );
			return retVal;
//			return 0.0f;
//			return saturationThreshold;
		}
		else
		{
			return value;
		}
	}
}
