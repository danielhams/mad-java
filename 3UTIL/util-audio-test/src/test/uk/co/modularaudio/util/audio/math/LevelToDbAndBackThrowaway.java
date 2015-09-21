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
	}

}
