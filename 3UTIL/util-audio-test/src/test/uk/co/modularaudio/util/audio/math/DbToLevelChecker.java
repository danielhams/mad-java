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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.MixdownSliderDbToLevelComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class DbToLevelChecker
{
	private static Log log = LogFactory.getLog( DbToLevelChecker.class.getName() );

	private final static int NUM_DEC = 4;

	public DbToLevelChecker()
	{
	}

	public void go() throws Exception
	{
		final MixdownSliderDbToLevelComputer dbc = new MixdownSliderDbToLevelComputer( 100 );

		final float[] testValues = new float[] {
				10.0f,
				0.0f,
				-88.0f,
				-89.0f,
				-89.9f,
				-89.9999f,
				-90.0f,
				Float.NEGATIVE_INFINITY
		};

		for( final float v : testValues )
		{
			final float step = dbc.toStepFromDb( v );
			log.debug("Value (" + MathFormatter.fastFloatPrint( v, NUM_DEC, true ) + ") -> step(" +
					MathFormatter.fastFloatPrint( step, NUM_DEC, true ) + ")");
			final float nv = dbc.toNormalisedSliderLevelFromDb( v );

			log.debug("Value (" + MathFormatter.fastFloatPrint( v, NUM_DEC, true ) + ") -> (" +
					MathFormatter.fastFloatPrint( nv, NUM_DEC, true ) + ")");

			final float abv = dbc.toDbFromNormalisedLevel( nv );

			log.debug("AndBa (" + MathFormatter.fastFloatPrint( nv, NUM_DEC, true ) + ") -> (" +
					MathFormatter.fastFloatPrint( abv, NUM_DEC, true ) + ")");
		}
	}

	public static void main( final String[] args ) throws Exception
	{
		final DbToLevelChecker dtlc = new DbToLevelChecker();
		dtlc.go();
	}
}
