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

public class MathFormatter
{
//	private static Log log = LogFactory.getLog( MathFormatter.class.getName() );

	public static String slowFloatPrint( final float f )
	{
		return slowFloatPrint( f, 2, true );
	}
	private final static int MAX_DECIMALS = 13;

	private static final long POW10[] = {
		1,
		10,
		100,
		1000,
		10000,
		100000,
		1000000,
		10000000,
		100000000,
		1000000000,
		10000000000L,
		100000000000L,
		1000000000000L,
		10000000000000L,
		100000000000000L
	};

	// Ugly unpleasant mess for printing floating point numbers
	// to a string. There's undoubtedly some precision loss and
	// boundary cases here, but for formatting floats for users with "regular"
	// kinds of bounds, it should be ok.
	public static String fastFloatPrint( float val, final int numDecimals, final boolean echoPlus ) // NOPMD by dan on 08/07/15 13:35
	{
		if( numDecimals > MAX_DECIMALS )
		{
			throw new RuntimeException("FastFloatPrint doesn't support more than " + MAX_DECIMALS + " decimal digits due to rounding errors. Use slowFloatPrint instead."); // NOPMD by dan on 08/07/15 13:35
		}

		final String retVal;
		if( val == Float.NEGATIVE_INFINITY )
		{
			retVal = "-Inf";
		}
		else if( val == Float.POSITIVE_INFINITY )
		{
			retVal = "Inf";
		}
		else if( val == Float.NaN )
		{
			retVal = "NaN";
		}
		else
		{
			final StringBuilder sb = new StringBuilder( 20 );
			if( val < 0.0f )
			{
				sb.append('-');
				val = -val;
			}
			else if( echoPlus )
			{
				sb.append('+');
			}

			long integerPortion = (long)val;

			final double remainder = ((double)val) - integerPortion;

			final long oExp = POW10[numDecimals+1];
			final double overScaledRemainder = remainder * oExp;
			final int extraTen = (overScaledRemainder % 10.0 >= 5.0 ? 10 : 0 );
			long roundedRemainder = (long)(overScaledRemainder + extraTen);

			if( roundedRemainder >= oExp )
			{
				integerPortion += 1;
				roundedRemainder -= oExp;
			}
			final long decimalsForPrint = roundedRemainder / 10;

			sb.append( integerPortion );

			if( numDecimals > 0 )
			{
				sb.append('.');
				for( int p = numDecimals - 1 ; p > 0 && decimalsForPrint < POW10[p]; --p )
				{
					sb.append('0');
				}

				sb.append(decimalsForPrint);
			}

			retVal = sb.toString();
		}

		return retVal;
	}

	public static String slowFloatPrint( final float f, final int numDecimals, final boolean echoPlus )
	{
		String retVal;
		if( f == Float.NEGATIVE_INFINITY )
		{
			retVal = "-Inf";
		}
		else if( f == Float.POSITIVE_INFINITY )
		{
			retVal = "Inf";
		}
		else if( f == Float.NaN )
		{
			retVal = "NaN";
		}
		else if( echoPlus )
		{
			retVal = String.format( "%+1." + numDecimals + "f", f);
		}
		else
		{
			retVal = String.format( "%1." + numDecimals + "f", f);
		}
		return retVal;
	}

	public static String slowDoublePrint( final double d, final int numDecimals, final boolean echoPlus )
	{
		String retVal;
		if( d == Double.NEGATIVE_INFINITY )
		{
			retVal = "-Inf";
		}
		else if( d == Double.POSITIVE_INFINITY )
		{
			retVal = "Inf";
		}
		else if( d == Double.NaN )
		{
			retVal = "NaN";
		}
		else if( echoPlus )
		{
			retVal = String.format( "%+1." + numDecimals + "f", d );
		}
		else
		{
			retVal = String.format( "%1." + numDecimals + "f", d );
		}
		return retVal;
	}

	public static String floatArrayPrint( final float[] floats )
	{
		final StringBuilder retVal = new StringBuilder();

		for( int i = 0 ; i < floats.length ; i++ )
		{
			retVal.append( slowFloatPrint( floats[ i ] ) );
			if( i < floats.length - 1 )
			{
				retVal.append( ",");
			}
		}

		return retVal.toString();
	}

	public static String floatArrayPrint( final float[] floats, final int numDecimalPlaces )
	{
		final StringBuilder retVal = new StringBuilder();

		for( int i = 0 ; i < floats.length ; i++ )
		{
			retVal.append( fastFloatPrint( floats[ i ], numDecimalPlaces, true ) );
			if( i < floats.length - 1 )
			{
				retVal.append( ",");
			}
		}

		return retVal.toString();
	}

	public static String intPrint( final int d )
	{
		return String.format( "%04d", d );
	}

	public static String floatRadianToDegrees( final float iRadiansIn, final int numDecimalPlaces )
	{
		// Normalise to +/- PI
		float radiansIn = iRadiansIn;
		while( radiansIn < 0 ) radiansIn += MathDefines.TWO_PI_F;
		while( radiansIn > MathDefines.TWO_PI_F ) radiansIn -= MathDefines.TWO_PI_F;

		final float inDegress = (radiansIn / MathDefines.TWO_PI_F ) * 360.0f;

		return fastFloatPrint( inDegress, numDecimalPlaces, true );
	}
}
