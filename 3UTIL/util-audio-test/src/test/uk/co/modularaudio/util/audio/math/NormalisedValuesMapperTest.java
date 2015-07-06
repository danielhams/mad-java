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

package test.uk.co.modularaudio.util.audio.math;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class NormalisedValuesMapperTest extends TestCase
{
	private static Log log = LogFactory.getLog( NormalisedValuesMapperTest.class.getName() );

	public void testExpLogMapping() throws Exception
	{
		final float minVal = 0.0f;
		final float maxVal = 22050.0f;
		final float[] testvals = new float[] { 0.0f, 1000.0f, 5000.0f, 10000.0f, 17000.0f, 22000.0f };

		for( int i = 0 ; i < testvals.length ; i++ )
		{
			final float testVal = testvals[i];

			final float normalisedTestVal = testVal / maxVal;

			final float mappedExpVal = NormalisedValuesMapper.logMinMaxMapF( normalisedTestVal, minVal, maxVal );

			log.debug( "TestVal(" + MathFormatter.fastFloatPrint( testVal, 5, true ) + ") NormVal(" +
					MathFormatter.fastFloatPrint( normalisedTestVal, 5, true ) + ") MappedVal(" +
					MathFormatter.fastFloatPrint( mappedExpVal, 5, true ) + ")");

			final float mappedBackVal = NormalisedValuesMapper.expMinMaxMapF( mappedExpVal, minVal, maxVal );
			log.debug("MappedBackVal(" + MathFormatter.fastFloatPrint( mappedBackVal, 5, true ) + ")");

		}
	}
}
