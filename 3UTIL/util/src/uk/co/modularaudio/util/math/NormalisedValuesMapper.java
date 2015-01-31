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

package uk.co.modularaudio.util.math;


public class NormalisedValuesMapper
{
	public static final double expMinMaxMapD( final double inValue, final double minValue, final double maxValue )
	{
		final double logMinValue = Math.log10( minValue + 10 );
		final double logMaxValue = Math.log10( maxValue + 10 );
		final double logDiff = logMaxValue - logMinValue;

		// Go from 1 to 10
		final double unscaledVal = (Math.pow( 10.0, logMinValue + ( inValue * logDiff) ) - 10.0);

		final double retVal = unscaledVal / maxValue;
		return retVal;
	}

	public static final float expMinMaxMapF( final float inValue, final float minValue, final float maxValue )
	{
		return (float)expMinMaxMapD( inValue, minValue, maxValue );
	}

	public static final double expMapD( final double inValue )
	{
		// Go from 1 to 10
		final double retVal = (Math.pow( 10.0, inValue + 1.0 ) - 10.0) / 90.0;
		return retVal;
	}

	public static final float expMapF( final float inValue )
	{
		return (float)expMapD( inValue );
	}

	public static final double logMinMaxMapD( final double inValue, final double minValue, final double maxValue )
	{
		final double logMinValue = Math.log10( minValue + 10 );
		final double logMaxValue = Math.log10( maxValue + 10 );
		final double logDiff = logMaxValue - logMinValue;
//
//		// Go from 1 to 10
//		double unscaledVal = (Math.pow( 10.0, logMinValue + ( inValue * logDiff) ) - 10.0);
//
//		double retVal = unscaledVal / maxValue;
		final double scaledVal = inValue * maxValue;
		final double plusTen = scaledVal + 10;
		final double logTenOfIt = Math.log10( plusTen );
		final double retVal = (logTenOfIt - logMinValue) / logDiff;
		return retVal;
	}

	public static final float logMinMaxMapF( final float inValue, final float minValue, final float maxValue )
	{
		return (float)logMinMaxMapD( inValue, minValue, maxValue );
	}

	public static final float logMapF( final float inValue )
	{
		return (float)logMapD( inValue );
	}

	public static final double logMapD( final double inValue )
	{
		final double retVal = Math.log10( 10.0 + (90.0 * inValue  ) ) - 1.0;
		return retVal;
	}

	public static float cosMapF( final float inValue )
	{
		return (float)cosMapD( inValue );
	}

	public static final double cosMapD( final double inValue )
	{
		return Math.cos( inValue * MathDefines.TWO_PI_D );
	}

	public static float sinMapF( final float inValue )
	{
		return (float)sinMapD( inValue );
	}

	public static final double sinMapD( final double inValue )
	{
		return Math.sin( inValue * MathDefines.TWO_PI_D );
	}

	public static float circleQuadOneF( final float inValue )
	{
		return (float)circleQuadOneD( inValue );
	}

	public static double circleQuadOneD( final double inValue )
	{
		return Math.sqrt( (1.0 - (inValue * inValue ) ) );
	}

	public static float circleQuadTwoF( final float inValue )
	{
		return (float)circleQuadTwoD( inValue );
	}

	public static double circleQuadTwoD( final double inValue )
	{
		return 1.0 - Math.sqrt( (1.0 - (inValue * inValue ) ) );
	}

	public static float circleQuadThreeF( final float inValue )
	{
		return (float)circleQuadThreeD( inValue );
	}

	public static double circleQuadThreeD( final double inValue )
	{
		final double reversedX = 1.0 - inValue;
		return 1.0 - Math.sqrt( (1.0 - (reversedX * reversedX ) ) );
	}

	public static float circleQuadFourF( final float inValue )
	{
		return (float)circleQuadFourD( inValue );
	}

	public static double circleQuadFourD( final double inValue )
	{
		final double reversedX = 1.0 - inValue;
		return Math.sqrt( (1.0 - (reversedX * reversedX) ) );
	}

}
