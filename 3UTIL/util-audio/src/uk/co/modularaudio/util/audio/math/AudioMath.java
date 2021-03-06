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

package uk.co.modularaudio.util.audio.math;

public class AudioMath
{
	public static final double MIN_GAIN = -100.0D;

	public static final double MAX_GAIN = 20.0D;

	public static final double MIN_FREQUENCY = 1.0D;

	public static final double MAX_FREQUENCY  = 44100 / 2.0D - 1.0D;

	public static final double MIN_TUNE = 16.0D;

	public static final double STD_TUNE = 440.0D;

	public static final double MAX_TUNE = 22050.0D;

	public static final int MAX_SHORT_VALUE = Short.MAX_VALUE;

	public static final long MAX_7BIT_UINT_VALUE = (1L << 7) - 1;
	public static final double MIN_SIGNED_FLOATING_POINT_7BIT_VAL_D = 2.0 / MAX_7BIT_UINT_VALUE;
	public static final float MIN_SIGNED_FLOATING_POINT_7BIT_VAL_F = (float)MIN_SIGNED_FLOATING_POINT_7BIT_VAL_D;

	public static final long MAX_8BIT_UINT_VALUE = (1L << 8) - 1;
	public static final double MIN_SIGNED_FLOATING_POINT_8BIT_VAL_D = 2.0 / MAX_8BIT_UINT_VALUE;
	public static final float MIN_SIGNED_FLOATING_POINT_8BIT_VAL_F = (float)MIN_SIGNED_FLOATING_POINT_8BIT_VAL_D;

	public static final long MAX_14BIT_UINT_VALUE = (1L << 14) - 1;
	public static final double MIN_SIGNED_FLOATING_POINT_14BIT_VAL_D = 2.0 / MAX_14BIT_UINT_VALUE;
	public static final float MIN_SIGNED_FLOATING_POINT_14BIT_VAL_F = (float)MIN_SIGNED_FLOATING_POINT_14BIT_VAL_D;

	public static final long MAX_16BIT_UINT_VALUE = (1L << 16) - 1;
	public static final double MIN_SIGNED_FLOATING_POINT_16BIT_VAL_D = 2.0 / MAX_16BIT_UINT_VALUE;
	public static final float MIN_SIGNED_FLOATING_POINT_16BIT_VAL_F = (float)MIN_SIGNED_FLOATING_POINT_16BIT_VAL_D;

	public static final long MAX_24BIT_UINT_VALUE = (1L << 24) - 1;
	public static final double MIN_SIGNED_FLOATING_POINT_24BIT_VAL_D = 2.0 / MAX_24BIT_UINT_VALUE;
	public static final float MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F = (float)MIN_SIGNED_FLOATING_POINT_24BIT_VAL_D;

	public static final long MAX_32BIT_UINT_VALUE = (1L << 32) - 1;
	public static final double MIN_SIGNED_FLOATING_POINT_32BIT_VAL_D = 2.0 / MAX_32BIT_UINT_VALUE;
	public static final float MIN_SIGNED_FLOATING_POINT_32BIT_VAL_F = (float)MIN_SIGNED_FLOATING_POINT_32BIT_VAL_D;


//	static
//	{
//		MAX_FREQUENCY = AudioConfig.DEFAULT_SAMPLE_RATE.value() / 2.0D - 1.0D;
//	}

	public static int log2( final int iSize)
	{
		int size = iSize;
		assert ((size & size - 1) == 0) : "size must be a power of two";
		int log = 0;
		if (size >= 65536)
		{
			log += 16;
			size >>>= 16;
		}

		if (size >= 256)
		{
			log += 8;
			size >>>= 8;
		}

		if (size >= 16)
		{
			log += 4;
			size >>>= 4;
		}

		if (size >= 4)
		{
			log += 2;
			size >>>= 2;
		}

		if (size >= 2)
			log++;

		return log;
	}

	public static double dbToLevel(final double value)
	{
		return Math.pow(10.0D, value / 20.0D);
	}

	public static float dbToLevelF(final float value)
	{
		return (float)Math.pow(10.0D, value / 20.0f);
	}

	public static float levelToDbF(final float value)
	{
		return 20.0f * (float)(Math.log10(value));
	}

	public static double levelToDb(final double value)
	{
		return 20.0D * (Math.log10(value));
	}

	public static double dbToLevel0(final double value)
	{
		if (value <= -100.0D)
			return 0.0D;
		return dbToLevel(value);
	}

	public static double levelToDb0(final double value)
	{
		final double result = levelToDb(value);
		if (result < -100.0D)
			return -100.0D;
		return result;
	}

	public static double dbToPowerLevel(final double value)
	{
		return Math.pow(10.0D, value / 10.0D);
	}

	public static double powerLevelToDb(final double value)
	{
		return 10.0D * Math.log10(value);
	}

	public static double powerLevelToDb0(final double value)
	{
		final double result = powerLevelToDb(value);
		if (result < -100.0D)
			return -100.0D;
		return result;
	}

	public static float energy(final float[] samples)
	{
		float total = 0.0F;
		final float[] arrayOfFloat = samples;
		final int j = samples.length;
		for (int i = 0; i < j; i++)
		{
			final float value = arrayOfFloat[i];
			total += value * value;
		}
		return (float) Math.sqrt(total / samples.length);
	}

	public static double gcd( final double ia, final double ib)
	{
		double a = ia;
		double b = ib;
		final double epsilon = 2.328306436538696E-10D;
		while (a > epsilon)
		{
			if (a < b)
			{
				final double swap = a;
				a = b;
				b = swap;
			}
			a %= b;
		}
		return b;
	}

	public static double gcd(final double[] values)
	{
		if (values.length == 0)
			throw new IllegalArgumentException("gcd() requires at least one value");
		double result = values[0];
		for (int i = 1; i < values.length; i++)
			result = gcd(result, values[i]);
		return result;
	}

	public static boolean isPowerOfTwo(final int value)
	{
		if (value <= 0)
			return false;
		return (value & value - 1) == 0;
	}

	public final static float limitIt( final float valueToLimit, final float lowerLimit, final float upperLimit )
	{
		return valueToLimit > upperLimit ? upperLimit : (valueToLimit < lowerLimit ? lowerLimit : valueToLimit );
	}

	public static float tanhNoClip( final float inVal )
	{
		// Ugly hack not particularly musically great tanh approximation
		if( inVal < -3.0f )
		{
			return -1.0f;
		}
		else if( inVal > 3.0f )
		{
			return 1.0f;
		}
		else
		{
			return inVal * (27 + inVal * inVal) / (27 + 9 * inVal * inVal);
		}
	}

	public static float tanhNoClipOnlyPositive( final float inVal )
	{
		// Ugly hack not particularly musically great tanh approximation
		if( inVal > 3.0f )
		{
			return 1.0f;
		}
		else
		{
			return inVal * (27 + inVal * inVal) / (27 + 9 * inVal * inVal);
		}
	}
}
