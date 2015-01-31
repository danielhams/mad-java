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

public class FloatToByteConverterTester extends TestCase
{
	private static Log log = LogFactory.getLog( FloatToByteConverter.class.getName() );
	
	public void testFloatToBytes() throws Exception
	{
		float[] someFloats = { 0.0f, 0.5f, 1.0f, -0.5f, -1.0f, 0.0567065f, 0.0749479f, 0.102622f };
		
		byte[] outputBytes = new byte[ someFloats.length * 2 ];
		
		FloatToByteConverter.floatToByteConversion( someFloats, 0, someFloats.length, outputBytes, 0, true );
		
		for( int i = 0 ; i < someFloats.length ; i++ )
		{
			log.debug( "For float " + i + "(" + someFloats[i] + ") converted to bytes(" +
					(int)outputBytes[ (i*2) ] + ", " +
					(int)outputBytes[ (i*2) + 1 ] +")");
		}			
	}
}
