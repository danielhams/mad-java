package test.uk.co.modularaudio.util.math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import uk.co.modularaudio.util.math.Float16;
import uk.co.modularaudio.util.math.MathFormatter;

public class Float16Tester
{
	private static Log log = LogFactory.getLog( Float16Tester.class.getName() );

	@Test
	public void testFromFloat()
	{
		final float[] testFloats = new float[]
		{
			Float16.MAX_VALUE,
			Float16.MIN_VALUE,

			// Interesting values from an audio perspective
			-1.0f,
			-0.001f,
			-0.0001f,
			0.0f,
			0.0001f,
			0.001f,
			1.0f,
			-1.1f,
			-0.9f,
			0.9f,
			1.1f,

			// And some values to examine precision
			192000.0f,
			41000.0f,
			22050.0f,
			Float.NaN,
			Float.NEGATIVE_INFINITY,
			Float.POSITIVE_INFINITY,
		};

		for( final float testFloat : testFloats )
		{
			final Float16 f16 = new Float16( testFloat );

			final float andBack = f16.asFloat();

			log.debug( "OF(" +
					MathFormatter.slowFloatPrint( testFloat, 16, true ) +
					") F16(" +
					MathFormatter.slowFloatPrint( andBack, 16, true ) +
					")");
		}
	}

}
