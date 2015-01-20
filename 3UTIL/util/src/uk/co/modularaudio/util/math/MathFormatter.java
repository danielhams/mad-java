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

import java.util.Formatter;

import uk.co.modularaudio.util.formatterpool.FormatterPool;

public class MathFormatter
{
	private static FormatterPool formatterPool = FormatterPool.getFormatterPool();

	public static String slowFloatPrint( float f )
	{
		return slowFloatPrint( f, 2, true );
	}

	private static String fastFloatPrint( float f )
	{
		return fastFloatPrint( f, 2, true );
	}

	public static String slowFloatPrint( float f, int numDecimals, boolean echoPlus )
	{
		Formatter formatter = formatterPool.getFormatter();
		String retVal;
		if( echoPlus || f < 0.0f)
		{
			retVal = formatter.format( "%+1." + numDecimals + "f", f ).toString();
		}
		else
		{
			retVal = formatter.format( "%1." + numDecimals + "f", f ).toString();
		}
		formatterPool.returnFormatter( formatter );
		return retVal;
	}
	
	public static String slowDoublePrint( double d, int numDecimals, boolean echoPlus )
	{
		Formatter formatter = formatterPool.getFormatter();
		String retVal;
		if( echoPlus || d < 0.0f)
		{
			retVal = formatter.format( "%+1." + numDecimals + "f", d ).toString();
		}
		else
		{
			retVal = formatter.format( "%1." + numDecimals + "f", d ).toString();
		}
		formatterPool.returnFormatter( formatter );
		return retVal;
	}
	
	public static void fastFloatPrint( StringBuilder outputStringBuilder, float f, int numDecimals, boolean echoPlus )
	{
		outputStringBuilder.append( slowFloatPrint( f, numDecimals, echoPlus ) );
	}
	
	public static String fastFloatPrint( float f, int numDecimals, boolean echoPlus )
	{
		int minDigits = numDecimals + 1;
		int asInt = (int)(f * Math.pow( 10, numDecimals) );
		boolean negative = asInt < 0;
		asInt = (negative ? -asInt : asInt );
		StringBuilder inReverse = new StringBuilder(64);

		for( int i = 0 ; i < minDigits || asInt > 0 ; i++ )
		{
			int curDigit = asInt % 10;
			asInt = asInt / 10;
			if( i == numDecimals && numDecimals > 0)
			{
				inReverse.append( '.' );
			}
			inReverse.append( Integer.toString( curDigit ) );
		}
		if( negative )
		{
			inReverse.append( '-' );
		}
		else if( echoPlus )
		{
			inReverse.append( '+' );
		}
		// Now back again
		inReverse.reverse();
		return inReverse.toString();
	}

	public static String floatArrayPrint( float[] floats )
	{
		StringBuilder retVal = new StringBuilder();
		
		for( int i = 0 ; i < floats.length ; i++ )
		{
			retVal.append( fastFloatPrint( floats[ i ] ) );
			if( i < floats.length - 1 )
			{
				retVal.append( ",");
			}
		}
		
		return retVal.toString();
	}
	
	public static String floatArrayPrint( float[] floats, int numDecimalPlaces )
	{
		StringBuilder retVal = new StringBuilder();
		
		for( int i = 0 ; i < floats.length ; i++ )
		{
			retVal.append( slowFloatPrint( floats[ i ], numDecimalPlaces, true ) );
			if( i < floats.length - 1 )
			{
				retVal.append( ",");
			}
		}
		
		return retVal.toString();
	}

	public static String intPrint( int d )
	{
		Formatter formatter = formatterPool.getFormatter();
		String retVal = formatter.format( "%04d", d ).toString();
		formatterPool.returnFormatter( formatter );
		return retVal;
	}
	
	public static String floatRadianToDegrees( float radiansIn, int numDecimalPlaces )
	{
		// Normalise to +/- PI
		while( radiansIn < 0 ) radiansIn += MathDefines.TWO_PI_F;
		while( radiansIn > MathDefines.TWO_PI_F ) radiansIn -= MathDefines.TWO_PI_F;
		
		float inDegress = (radiansIn / MathDefines.TWO_PI_F ) * 360.0f;
		
		return slowFloatPrint( inDegress, numDecimalPlaces, true );
	}
}
