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

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.MathFormatter;

public class LevelToDbAndBackThrowaway
{
	private static Log log = LogFactory.getLog( LevelToDbAndBackThrowaway.class.getName() );

	private final static int NUM_7BIT_VALUES = (int)Math.pow( 2, 7 );
	private final static float MIN_7BIT_MIDI_LEVEL = 1.0f / NUM_7BIT_VALUES;
	private final static float MIN_7BIT_MIDI_DB = AudioMath.levelToDbF( MIN_7BIT_MIDI_LEVEL );

	private final static int NUM_14BIT_VALUES = (int)Math.pow( 2, 14 );
	private final static float MIN_14BIT_MIDI_LEVEL = 1.0f / NUM_14BIT_VALUES;
	private final static float MIN_14BIT_MIDI_DB = AudioMath.levelToDbF( MIN_14BIT_MIDI_LEVEL );

	public static void main( final String[] args )
	{
		log.info( "7bit midi has " + NUM_7BIT_VALUES );
		log.info( "The min midi 7bit level change is " +
				MathFormatter.slowFloatPrint( MIN_7BIT_MIDI_LEVEL, 8, false ) );
		log.info( "As DB this is " +
				MathFormatter.slowFloatPrint( MIN_7BIT_MIDI_DB, 8, false ) );

		log.info( "14bit midi has " + NUM_14BIT_VALUES );
		log.info( "The min midi 14bit level change is " +
				MathFormatter.slowFloatPrint( MIN_14BIT_MIDI_LEVEL, 8, false ) );
		log.info( "As DB this is " +
				MathFormatter.slowFloatPrint( MIN_14BIT_MIDI_DB, 8, false ) );

		final float[] testLevelValues = new float[]
		{
				0.85f,
				0.15f,
				0.97f
		};

		for( final float f : testLevelValues )
		{
			final float dbValue = AudioMath.levelToDbF( f );
			log.info( "The level (" +
					MathFormatter.slowFloatPrint( f, 8, false ) +
					") maps to the dbValue (" +
					MathFormatter.slowFloatPrint( dbValue, 8, false ) +
					")");
		}
	}

}
