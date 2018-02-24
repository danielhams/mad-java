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

public strictfp class FastMath
{

	public final static float atan2( final float y, final float x )
	{
		return (float)Math.atan2(y, x);
	}

	public final static float satan2(final float y, final float x)
	   {
	     float t0, t1, t3, t4;

	     final float xAbs = ( x < 0.0f ? -x : x );
	     final float yAbs = ( y < 0.0f ? -y : y );

	     t3 = xAbs;
	     t1 = yAbs;
	     t0 = ( t3 > t1 ? t3 : t1 );
	     t1 = ( t1 < t3 ? t1 : t3 );
	     t3 = ( t0 == 0.0f ? 0.0f : 1f / t0 );
	     t3 = t1 * t3;

	     t4 = t3 * t3;
	     t0 =         - 0.013480470f;
	     t0 = t0 * t4 + 0.057477314f;
	     t0 = t0 * t4 - 0.121239071f;
	     t0 = t0 * t4 + 0.195635925f;
	     t0 = t0 * t4 - 0.332994597f;
	     t0 = t0 * t4 + 0.999995630f;
	     t3 = t0 * t3;

	     t3 = (yAbs > xAbs) ? MathDefines.HALF_PI_F - t3 : t3;
	     t3 = (x < 0) ?  MathDefines.ONE_PI_F - t3 : t3;
	     t3 = (y < 0) ? -t3 : t3;

	     return t3;
	   }

	public final static double tan( final double theta )
	{
//		return org.apache.commons.math3.util.FastMath.tan( theta );
//		return 0.0000000001d;
//		return StrictMath.tan( theta );
		return Math.tan( theta );
	}

	public static double cos( final double theta )
	{
		return Math.cos( theta );
	}

	public static float fastApproxTanh( final float halfInVal )
	{
		final float inVal = 2 * halfInVal;
		final int sign = (inVal < 0.0f ? -1 : 1 );
		float abs = inVal * sign;
		abs = 6+abs * (6+abs * (3+abs));
		return sign * (abs-6)/(abs+6);
	}

	public final static int log2( final int n )
	{
		if( n<=0 ) throw new IllegalArgumentException();
		return 31 - Integer.numberOfLeadingZeros( n );
	}

	public final static int log2( final long n )
	{
		if( n<=0 ) throw new IllegalArgumentException();
		return 63 - Long.numberOfLeadingZeros( n );
	}

	private final static boolean isEven( final long v )
	{
		return (v % 2) == 0;
	}

	public final static long pow( final long a, final int b )
	{
	    if( b == 0 )        return 1;
	    if( b == 1 )        return a;
	    if( isEven( b ) )   return     pow( a * a, b/2 ); //even a=(a^2)^b/2
	    else                return a * pow( a * a, b/2 ); //odd  a=a*(a^2)^b/2

	}

	public final static float fastSinApprox( final float x )
	{
		final float B = MathDefines.FOUR_OVER_PI_F;
		final float C = MathDefines.MINUS_FOUR_OVER_PI_SQUARED_F;

		float y = B * x + C * x * (x < 0.0f ? -x : x );

		// Extra precision

		// Abs error minimisation
//		// final float Q = 0.775f;
//		final float P = 0.225f;
		// Relative error minimisation
		// final float Q = 0.782f;
		final float P = 0.218f;

		y = P * ( y * (y < 0.0f ? -y : y) - y ) + y; // Q * y + P * y * abs(y)

		return y;
	}

}
