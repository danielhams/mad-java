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

package test.uk.co.modularaudio.util.audio.format;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.format.FloatToByteConverter;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.MathFormatter;

public class FloatToByteConverterTester extends TestCase
{
	private static Log log = LogFactory.getLog( FloatToByteConverter.class.getName() );

	public FloatToByteConverterTester()
	{
		log.info( "Float to byte conversion tests with min float 16bit value as " +
				MathFormatter.slowFloatPrint( AudioMath.MIN_SIGNED_FLOATING_POINT_16BIT_VAL_F, 8, false ) );
	}

	public void testFloatToBytesMultiByte() throws Exception
	{
		final int numBytesPerSample = 2;
		final float[] someFloats = { -0.5f, 0.5f, 1.0f, -0.5f, -1.0f, 0.0567065f, 0.0749479f, 0.102622f, 0.0f };

		final byte[] outputBytes = new byte[ someFloats.length * numBytesPerSample ];

		FloatToByteConverter.floatToByteConversion( someFloats, 0, someFloats.length, outputBytes, 0, numBytesPerSample );

		for( int i = 0 ; i < someFloats.length ; i++ )
		{
			log.debug( "For float " + i + "(" + someFloats[i] + ") converted to bytes(" +
					(int)outputBytes[ (i*2) ] + ", " +
					(int)outputBytes[ (i*2) + 1 ] +")");
		}

		final float[] testFloats = new float[someFloats.length];
		FloatToByteConverter.byteToFloatConversion( outputBytes, 0, testFloats, 0, someFloats.length, numBytesPerSample );
		for( int f = 0 ; f < someFloats.length ; ++f )
		{
			final float expectedFloat = someFloats[f];
			final float computedFloat = testFloats[f];
			final float absDiff = Math.abs(expectedFloat - computedFloat);

			log.debug( "Converted " + MathFormatter.slowFloatPrint( expectedFloat, 8, true ) + " back to float " +
					MathFormatter.slowFloatPrint( computedFloat, 8, true ) + " with absDiff " +
					MathFormatter.slowFloatPrint( absDiff, 8, true )
					);

			if( absDiff > AudioMath.MIN_SIGNED_FLOATING_POINT_16BIT_VAL_F )
			{
				log.error("Failed on conversion of float: " + expectedFloat + " to " +
						computedFloat );
			}
//			assertTrue( someFloats[f] == testFloats[f] );
		}
	}
}
