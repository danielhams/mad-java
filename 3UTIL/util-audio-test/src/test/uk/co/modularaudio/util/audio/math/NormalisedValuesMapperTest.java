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
		float minVal = 0.0f;
		float maxVal = 22050.0f;
		float[] testvals = new float[] { 0.0f, 1000.0f, 5000.0f, 10000.0f, 17000.0f, 22000.0f };
		
		for( int i = 0 ; i < testvals.length ; i++ )
		{
			float testVal = testvals[i];
			
			float normalisedTestVal = testVal / maxVal;
			
			float mappedExpVal = NormalisedValuesMapper.logMinMaxMapF( normalisedTestVal, minVal, maxVal );
			
			log.debug( "TestVal(" + MathFormatter.slowFloatPrint( testVal, 5, true ) + ") NormVal(" +
					MathFormatter.slowFloatPrint( normalisedTestVal, 5, true ) + ") MappedVal(" +
					MathFormatter.slowFloatPrint( mappedExpVal, 5, true ) + ")");
			
			float mappedBackVal = NormalisedValuesMapper.expMinMaxMapF( mappedExpVal, minVal, maxVal );
			log.debug("MappedBackVal(" + MathFormatter.slowFloatPrint( mappedBackVal, 5, true ) + ")");
			
		}
	}
}
