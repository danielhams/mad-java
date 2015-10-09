package test.uk.co.modularaudio.util.math;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.math.MathFormatter;

public class FastMathTester extends TestCase
{
	private static Log log = LogFactory.getLog( FastMathTester.class.getName() );

	public FastMathTester()
	{
	}

	public float fastTanh( final float halfInVal )
	{
		final float inVal = 2 * halfInVal;
		final int sign = (inVal < 0.0f ? -1 : 1 );
		float abs = inVal * sign;
		abs = 6 + abs * (6 + abs * (3+abs));
		return sign * (abs-6)/(abs+6);
	}

	public void testTanhApproximation()
	{
		final float[] floatsToTest = new float[] {
				-200.0f,
				-100.0f,
				-10.0f,
				-1.0f,
				-0.5f,
				-0.1f,
				0.0f,
				0.1f,
				0.5f,
				1.0f,
				10.0f,
				100.0f,
				200.0f
		};

		for( final float ftt : floatsToTest )
		{
			final float jmResult = (float)Math.tanh( ftt );
			final float fmResult = fastTanh( ftt );

			log.debug("Sour(" +
					MathFormatter.slowFloatPrint( ftt, 12, true ) +
					")");
			log.debug("Java(" +
					MathFormatter.slowFloatPrint( jmResult, 12, true )  +
					")");
			log.debug("Appr(" +
					MathFormatter.slowFloatPrint( fmResult, 12, true ) +
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
		for( int i = 0 ; i < 1000000 ; ++i )
		{
			final float res = (float)Math.tanh( 0.65f );
		}
		final long jna = System.nanoTime();
		final long fb = System.nanoTime();
		for( int i = 0 ; i < 1000000 ; ++i )
		{
			final float res = fastTanh( 0.65f );
		}
		final long fa = System.nanoTime();
		final long javaTime = jna - jnb;
		final long fastTime = fa - fb;
		log.debug( "Java nanos " + javaTime + " fast nanos " + fastTime );
	}

}
