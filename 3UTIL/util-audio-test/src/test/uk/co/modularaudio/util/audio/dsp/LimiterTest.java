package test.uk.co.modularaudio.util.audio.dsp;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.dsp.LimiterCrude;
import uk.co.modularaudio.util.audio.dsp.LimiterTanh;
import uk.co.modularaudio.util.audio.dsp.LimiterTanhApprox;
import uk.co.modularaudio.util.math.MathFormatter;

public class LimiterTest extends TestCase
{
	private static Log log = LogFactory.getLog( LimiterTest.class.getName() );

	private static final float TEST_KNEE = 0.5f;
	private static final float TEST_FALLOFF = 1.0f;

	public LimiterTest()
	{
	}

	public void testLimits()
		throws Exception
	{
		final float[] testValues = {
				0.0f,
				0.01f,
				0.5f,
				0.9f,
				0.979f,
				0.98f,
				0.981f,
				0.982f,
				0.99999f,
				0.5f,
				0.6f,
				0.7f,
				0.8f,
				0.9f,
				0.99999f,
				1.0f,
				1.00001f,
				1.01f,
				2.0f,
				10.0f,
				100.0f,
				1000.0f,
				10000.0f,
				};

		final float[] resultsCrude = new float[ testValues.length ];
		final float[] resultsTanhApprox = new float[ testValues.length ];
		final float[] resultsTanh = new float[ testValues.length ];

		System.arraycopy( testValues, 0, resultsCrude, 0, testValues.length );
		System.arraycopy( testValues, 0, resultsTanhApprox, 0, testValues.length );
		System.arraycopy( testValues, 0, resultsTanh, 0, testValues.length );

		final LimiterCrude limiterCrude = new LimiterCrude( TEST_KNEE, TEST_FALLOFF );
		final LimiterTanhApprox limiterTanhApprox = new LimiterTanhApprox( TEST_KNEE, TEST_FALLOFF );
		final LimiterTanh limiterTanh = new LimiterTanh( TEST_KNEE, TEST_FALLOFF );

		limiterCrude.filter( resultsCrude, 0, testValues.length );
		limiterTanhApprox.filter( resultsTanhApprox, 0, testValues.length );
		limiterTanh.filter( resultsTanh, 0, testValues.length );

		for( int i = 0 ; i < testValues.length ; ++i )
		{
			final float sourceVal = testValues[i];
			final float resultCrude = resultsCrude[i];
			final float resultTanhApprox = resultsTanhApprox[i];
			final float resultTanh = resultsTanh[i];

			assertTrue( resultCrude >= 0.0f );
			assertTrue( resultCrude <= 1.0f );

			log.debug("Mapped value " +
					MathFormatter.slowFloatPrint( sourceVal, 18, true ) +
					" to C(" +
					MathFormatter.slowFloatPrint( resultCrude, 18, true ) +
					") TA(" +
					MathFormatter.slowFloatPrint( resultTanhApprox, 18, true ) +
					") T(" +
					MathFormatter.slowFloatPrint( resultTanh, 18, true ) +
					")");
		}
	}
}
