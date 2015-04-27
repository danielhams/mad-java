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
			log.debug("Value (" + MathFormatter.slowFloatPrint( v, NUM_DEC, true ) + ") -> step(" +
					MathFormatter.slowFloatPrint( step, NUM_DEC, true ) + ")");
			final float nv = dbc.toNormalisedSliderLevelFromDb( v );

			log.debug("Value (" + MathFormatter.slowFloatPrint( v, NUM_DEC, true ) + ") -> (" +
					MathFormatter.slowFloatPrint( nv, NUM_DEC, true ) + ")");

			final float abv = dbc.toDbFromNormalisedLevel( nv );

			log.debug("AndBa (" + MathFormatter.slowFloatPrint( nv, NUM_DEC, true ) + ") -> (" +
					MathFormatter.slowFloatPrint( abv, NUM_DEC, true ) + ")");
		}
	}

	public static void main( final String[] args ) throws Exception
	{
		final DbToLevelChecker dtlc = new DbToLevelChecker();
		dtlc.go();
	}
}
