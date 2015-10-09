package test.uk.co.modularaudio.util.audio.math;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathFormatter;

public class FastMathTester extends TestCase
{
	private static Log log = LogFactory.getLog( FastMathTester.class.getName() );

	private final static int NUM_TEST_FLOATS = 8192;
	private final float[] testFloats = new float[NUM_TEST_FLOATS];
	private final float[] outputFloats = new float[NUM_TEST_FLOATS];

	public FastMathTester()
	{
		for( int i = 0 ; i < NUM_TEST_FLOATS ; ++i )
		{
			testFloats[i] = (float)Math.random();
		}
	}

	public void testTanhApproximation()
	{
		final float[] localFloatsToTest = new float[] {
				0.0f,
				0.1f,
				0.2f,
				0.3f,
				0.4f,
				0.5f,
				0.6f,
				0.7f,
				0.8f,
				0.9f,
				1.0f,
				1.1f,
				1.2f,
				10.0f,
				100.0f,
				200.0f,
				-200.0f,
				-100.0f,
				-10.0f,
				-1.0f,
				-0.5f,
				-0.1f,
		};

		final float[] localOutputFloats = new float[localFloatsToTest.length];

		for( int i = 0 ; i < localFloatsToTest.length ; ++i )
		{
			final float ftt = localFloatsToTest[i];
			final float jmResult = (float)Math.tanh( ftt );
			localOutputFloats[i] = jmResult;
			final float fmResult = FastMath.fastApproxTanh( ftt );
			localOutputFloats[i] = fmResult;
			final float amResult = AudioMath.tanhNoClip( ftt );
			localOutputFloats[i] = amResult;

			log.debug("Sour(" +
					MathFormatter.slowFloatPrint( ftt, 12, true ) +
					")");
			log.debug("Java(" +
					MathFormatter.slowFloatPrint( jmResult, 12, true )  +
					")");
			log.debug("Appr(" +
					MathFormatter.slowFloatPrint( fmResult, 12, true ) +
					")");
			log.debug("AMAp(" +
					MathFormatter.slowFloatPrint( amResult, 12, true ) +
					")");
		}

		for( int i = 0 ; i < 100 ; ++i )
		{
			doOneCycle();
		}
	}

	private void doOneCycle()
	{

		final long jnb = System.nanoTime();
		for( int i = 0 ; i < NUM_TEST_FLOATS ; ++i )
		{
			outputFloats[i] = (float)Math.tanh( testFloats[i] );
		}
		final long jna = System.nanoTime();
		final long fb = System.nanoTime();
		for( int i = 0 ; i < NUM_TEST_FLOATS ; ++i )
		{
			outputFloats[i] = FastMath.fastApproxTanh( testFloats[i] );
		}
		final long fa = System.nanoTime();
		final long amb = System.nanoTime();
		for( int i = 0 ; i < NUM_TEST_FLOATS ; ++i )
		{
			outputFloats[i] = AudioMath.tanhNoClip( testFloats[i] );
		}
		final long ama = System.nanoTime();
		final long javaTime = jna - jnb;
		final long fastTime = fa - fb;
		final long amTime = ama - amb;
		log.debug( "Java nanos " + javaTime + " fast nanos " + fastTime + " audio math tan nanos " + amTime );
	}

}
