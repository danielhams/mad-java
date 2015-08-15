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

package uk.co.modularaudio.util.audio.format;


public class FloatToByteConverter
{
//	private static Log log = LogFactory.getLog( FloatToByteConverter.class.getName());

	private final static long getMaxValueForFloatsToMultiBytes( final int bytesPerSample )
	{
		return (1L << ((bytesPerSample * 8) - 1)) - 1;
	}

	private final static long getMaxValueForMultiBytesToFloats( final int bytesPerSample )
	{
		return 1L << ((bytesPerSample * 8) - 1);
	}

	public static int byteToFloatConversion( final byte[] sourceBytes, final int position,
			final float[] destination, final int fpos, final int numFloats,
			final int bytesPerFloat )
		throws ArrayIndexOutOfBoundsException
	{
		if( bytesPerFloat > 1 )
		{
			return multiByteToFloatConversion( sourceBytes, position, destination, fpos, numFloats, bytesPerFloat );
		}
		else
		{
			return singleByteToFloatConversion( sourceBytes, position, destination, fpos, numFloats );
		}
	}

	public static int multiByteToFloatConversion( final byte[] sourceBytes, final int position,
			final float[] destination, final int fpos, final int numFloats,
			final int bytesPerFloat )
		throws ArrayIndexOutOfBoundsException
	{
		final long scaleValue = getMaxValueForMultiBytesToFloats( bytesPerFloat );
		int sourcePosition = position;
		int destPosition = fpos;

		final int bMax = bytesPerFloat - 1;

		for( int i = 0 ; i < numFloats ; i++ )
		{
			long curValue = (sourceBytes[sourcePosition++] & 0xFF);
			int b = 1;
			while( b < bMax )
			{
				final byte v = sourceBytes[sourcePosition++];
				curValue = curValue | ((v & 0xff) << (b*8));
				b++;
			}
			final byte v = sourceBytes[sourcePosition++];
			curValue = curValue | (v << (b*8));

			// Conversion to float
			final float asFloat = curValue / (float)scaleValue;
			destination[ destPosition++ ] = asFloat;
		}
		return numFloats;
	}

	public static int singleByteToFloatConversion( final byte[] sourceBytes, final int position,
			final float[] destination, final int fpos, final int numFloats )
		throws ArrayIndexOutOfBoundsException
	{
		final float offset = 1;
		final int maxValue = (1 << 8) - 1;
		final float scaleValue = 0.5f * (maxValue + 1);
		int sourcePosition = position;
		int destPosition = fpos;

		for( int i = 0 ; i < numFloats ; i++ )
		{
			final long curValue = (sourceBytes[sourcePosition++] & 0xFF);

			// Conversion to float
			final float asFloat = (curValue / scaleValue) - offset;
			destination[ destPosition++ ] = asFloat;
		}
		return numFloats;
	}

	public static int floatToByteConversion( final float[] sourceFloats, final int position, final int numFloats,
			final byte[] destination, final int bpos,
			final int bytesPerSample )
		throws ArrayIndexOutOfBoundsException
	{
		if( bytesPerSample > 1 )
		{
			return floatToMultiByteConversion( sourceFloats, position, numFloats, destination, bpos, bytesPerSample );
		}
		else
		{
			return floatToSingleByteConversion( sourceFloats, position, numFloats, destination, bpos );
		}
	}

	private static int floatToSingleByteConversion( final float[] sourceFloats, final int position, final int numFloats,
			final byte[] destination, final int bpos )
		throws ArrayIndexOutOfBoundsException
	{
		final float offset = 1;
		final int maxValue = (1 << 8) - 1;
		final float scaleValue = 0.5f * (maxValue + 1);
//		final float scaleValue = 0.5f * maxValue;

		for( int i = 0 ; i < numFloats ; i++ )
		{
			final int sourcePosition = position + i;
			int destPosition = bpos + i;

			final float fSample = sourceFloats[ sourcePosition ];

			// scaling and conversion to integer
//			long nSample = Math.round( (((fSample + offset) * maxValue) / 2) );

//			final float scaledSample = (fSample + offset) * scaleValue;
//			long nSample = (long)scaledSample;

			long nSample = Math.round( (fSample + offset) * scaleValue );

			nSample = (nSample > maxValue ? maxValue : (nSample < 0 ? 0 : nSample ));

			destination[ destPosition++ ] = (byte)(nSample & 0xFF);
		}

		return numFloats;
	}

	private static int floatToMultiByteConversion( final float[] sourceFloats, final int position, final int numFloats,
			final byte[] destination, final int bpos,
			final int bytesPerSample )
		throws ArrayIndexOutOfBoundsException
	{
		final long maxValue = getMaxValueForFloatsToMultiBytes( bytesPerSample );
		final long scaleValue = maxValue + 1;

		for( int i = 0 ; i < numFloats ; i++ )
		{
			final int sourcePosition = position + i;
			int destPosition = bpos + (i * bytesPerSample);

			final float fSample = sourceFloats[ sourcePosition ];

			// scaling and conversion to integer
			long nSample = Math.round( fSample * scaleValue );
			nSample = (nSample > maxValue ? maxValue : (nSample < -maxValue ? -maxValue : nSample ) );

			for( int b = 0 ; b < bytesPerSample ; b++ )
			{
				destination[ destPosition++ ] = (byte)(nSample & 0xFF);
				nSample >>= 8;
			}
		}

		return numFloats;
	}

	public static int doubleToByteConversion( final double[] sourceFloats, final int position, final int numFloats,
			final byte[] destination, final int bpos,
			final int bytesPerSample )
		throws ArrayIndexOutOfBoundsException
	{
		if( bytesPerSample > 1 )
		{
			return doubleToMultiByteConversion( sourceFloats, position, numFloats, destination, bpos, bytesPerSample );
		}
		else
		{
			return doubleToSingleByteConversion( sourceFloats, position, numFloats, destination, bpos );
		}
	}

	private static int doubleToSingleByteConversion( final double[] sourceFloats, final int position, final int numFloats,
			final byte[] destination, final int bpos )
		throws ArrayIndexOutOfBoundsException
	{
		final double offset = 1;
		final int maxValue = (1 << 8) - 1;
		final double scaleValue = 0.5 * (maxValue + 1);

		for( int i = 0 ; i < numFloats ; i++ )
		{
			final int sourcePosition = position + i;
			int destPosition = bpos + i;

			final double dSample = sourceFloats[ sourcePosition ];

			long nSample = Math.round( (dSample + offset) * scaleValue );

			nSample = (nSample > maxValue ? maxValue : (nSample < 0 ? 0 : nSample ));

			destination[ destPosition++ ] = (byte)(nSample & 0xFF);
		}

		return numFloats;
	}

	private static int doubleToMultiByteConversion( final double[] sourceFloats, final int position, final int numFloats,
			final byte[] destination, final int bpos,
			final int bytesPerSample )
		throws ArrayIndexOutOfBoundsException
	{
		final long maxValue = getMaxValueForFloatsToMultiBytes( bytesPerSample );
		final long scaleValue = maxValue + 1;

		for( int i = 0 ; i < numFloats ; i++ )
		{
			final int sourcePosition = position + i;
			int destPosition = bpos + (i * bytesPerSample);

			final double dSample = sourceFloats[ sourcePosition ];

			// scaling and conversion to integer
			long nSample = Math.round( dSample * scaleValue );
			nSample = (nSample > maxValue ? maxValue : (nSample < -maxValue ? -maxValue : nSample ) );

			for( int b = 0 ; b < bytesPerSample ; b++ )
			{
				destination[ destPosition++ ] = (byte)(nSample & 0xFF);
				nSample >>= 8;
			}
		}

		return numFloats;
	}
}
