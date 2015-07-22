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

package test.uk.co.modularaudio.util.audio.mvc.displayslider;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownSliderIntToFloatConverter;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestMixdownSliderIntToFloatConverter extends TestCase
{
	private static Log log = LogFactory.getLog( TestMixdownSliderIntToFloatConverter.class.getName() );

	public TestMixdownSliderIntToFloatConverter()
	{
	}

	public void testMixdownSliderConversions() throws IOException
	{
		final MixdownSliderIntToFloatConverter intToFloatConverter = new MixdownSliderIntToFloatConverter();

		intToFloatTests( intToFloatConverter );

		floatToIntTests( intToFloatConverter );

	}

	private void intToFloatTests( final MixdownSliderIntToFloatConverter intToFloatConverter )
	{
		final int numCompressedSteps = intToFloatConverter.getNumCompressedSteps();
		final int numLinearSteps = intToFloatConverter.getNumLinearSteps();
		final int numTotalSteps = intToFloatConverter.getNumTotalSteps();

		log.debug("IntToFloatConverter - NCS(" + numCompressedSteps + ") NLS(" + numLinearSteps + ") NTS(" + numTotalSteps + ")");

		for( int i = 0 ; i < numTotalSteps ; ++i )
		{
			final float sliderFloatVal = intToFloatConverter.sliderIntValueToFloatValue( null, i );
			log.debug("IntToFloat " + i + " to " +
					MathFormatter.fastFloatPrint( sliderFloatVal, 5, true ) );
		}
	}

	private void floatToIntTests( final MixdownSliderIntToFloatConverter intToFloatConverter )
	{
		final float[] testFloatVals = new float[] {
				10.0f,
				5.0f,
				1.0f,
				0.0f,
				-1.0f,
				-5.0f,
				-10.0f,
				-15.0f,
				-29.0f,
				-30.0f,
				-31.0f,
				-50.0f,
				-69.0f,
				-70.0f,
				-71.0f,
				-80.0f,
				-90.0f,
				-120.0f,		// This should get rounded down to -INF since -90.0dB is the lowest value
				-1000.0f,
				Float.NEGATIVE_INFINITY
		};

		for( final float f : testFloatVals )
		{
			final int sliderIntVal = intToFloatConverter.floatValueToSliderIntValue( null, f );
			if( f == Float.NEGATIVE_INFINITY )
			{
				log.debug("FloatToInt -Inf as int: " +
						sliderIntVal );
			}
			else
			{
				log.debug("FloatToInt " + MathFormatter.fastFloatPrint( f, 5, true ) + " as int: " +
						sliderIntVal );
			}
		}
	}
}
