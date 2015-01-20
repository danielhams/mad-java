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

package test.uk.co.modularaudio.util.dump;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.math.FastMath;

public class MathTester
{
	private static Log log = LogFactory.getLog( MathTester.class.getName() );

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		float real = -0.5f;
		float imag = 0.5f;
		log.debug( "Real( " + real + " ) Imag( " + imag + " )");
		
		float amp = (float)Math.sqrt( ((real * real) + (imag * imag)) );
		float phase = FastMath.atan2( imag, real );

		log.debug("Amp( " + amp + " ) Phase( " + phase + " )");
	}

}
