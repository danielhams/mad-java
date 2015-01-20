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
	public static final double expMinMaxMapD( double inValue, double minValue, double maxValue )
	{
		double logMinValue = Math.log10( minValue + 10 );
		double logMaxValue = Math.log10( maxValue + 10 );
		double logDiff = logMaxValue - logMinValue;

		// Go from 1 to 10
		double unscaledVal = (Math.pow( 10.0, logMinValue + ( inValue * logDiff) ) - 10.0);

		double retVal = unscaledVal / maxValue;
		return retVal;
	}

	public static final float expMinMaxMapF( float inValue, float minValue, float maxValue )
	{
		return (float)expMinMaxMapD( inValue, minValue, maxValue );
	}
	
	public static final double expMapD( double inValue )
	{
		// Go from 1 to 10
		double retVal = (Math.pow( 10.0, inValue + 1.0 ) - 10.0) / 90.0;
		return retVal;
	}

	public static final float expMapF( float inValue )
	{
		return (float)expMapD( inValue );
	}
	
	public static final double logMinMaxMapD( double inValue, double minValue, double maxValue )
	{
		double logMinValue = Math.log10( minValue + 10 );
		double logMaxValue = Math.log10( maxValue + 10 );
		double logDiff = logMaxValue - logMinValue;
//
//		// Go from 1 to 10
//		double unscaledVal = (Math.pow( 10.0, logMinValue + ( inValue * logDiff) ) - 10.0);
//
//		double retVal = unscaledVal / maxValue;
		double scaledVal = inValue * maxValue;
		double plusTen = scaledVal + 10;
		double logTenOfIt = Math.log10( plusTen );
		double retVal = (logTenOfIt - logMinValue) / logDiff;
		return retVal;
	}

	public static final float logMinMaxMapF( float inValue, float minValue, float maxValue )
	{
		return (float)logMinMaxMapD( inValue, minValue, maxValue );
	}
	
	public static final float logMapF( float inValue )
	{
		return (float)logMapD( inValue );
	}
	
	public static final double logMapD( double inValue )
	{
		double retVal = Math.log10( 10.0 + (90.0 * inValue  ) ) - 1.0;
		return retVal;
	}
	
	public static float cosMapF( float inValue )
	{
		return (float)cosMapD( inValue );
	}
	
	public static final double cosMapD( double inValue )
	{
		return Math.cos( inValue * MathDefines.TWO_PI_D );
	}

	public static float sinMapF( float inValue )
	{
		return (float)sinMapD( inValue );
	}
	
	public static final double sinMapD( double inValue )
	{
		return Math.sin( inValue * MathDefines.TWO_PI_D );
	}
	
	public static float circleQuadOneF( float inValue )
	{
		return (float)circleQuadOneD( inValue );
	}
	
	public static double circleQuadOneD( double inValue )
	{
		return Math.sqrt( (1.0 - (inValue * inValue ) ) );
	}

	public static float circleQuadTwoF( float inValue )
	{
		return (float)circleQuadTwoD( inValue );
	}
	
	public static double circleQuadTwoD( double inValue )
	{
		return 1.0 - Math.sqrt( (1.0 - (inValue * inValue ) ) );
	}

	public static float circleQuadThreeF( float inValue )
	{
		return (float)circleQuadThreeD( inValue );
	}
	
	public static double circleQuadThreeD( double inValue )
	{
		double reversedX = 1.0 - inValue;
		return 1.0 - Math.sqrt( (1.0 - (reversedX * reversedX ) ) );
	}

	public static float circleQuadFourF( float inValue )
	{
		return (float)circleQuadFourD( inValue );
	}
	
	public static double circleQuadFourD( double inValue )
	{
		double reversedX = 1.0 - inValue;
		return Math.sqrt( (1.0 - (reversedX * reversedX) ) );
	}

}
