package test.uk.co.modularaudio.util.audio.oscillatortable;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.oscillatortable.CubicInterpolatingWaveTableValueFetcher;
import uk.co.modularaudio.util.audio.oscillatortable.CubicPaddedRawWaveTable;
import uk.co.modularaudio.util.audio.oscillatortable.LinearInterpolatingWaveTableValueFetcher;
import uk.co.modularaudio.util.audio.oscillatortable.SineRawWaveTableGenerator;
import uk.co.modularaudio.util.audio.oscillatortable.TruncatingWaveTableValueFetcher;
import uk.co.modularaudio.util.audio.oscillatortable.WaveTableValueFetcher;
import uk.co.modularaudio.util.math.MathFormatter;


public class InterpolationTester extends TestCase
{
	private static Log log = LogFactory.getLog( InterpolationTester.class.getName() );

	public void testTruncatingInterpolator()
	{
		log.debug("Testing truncating interpolator");

		final TruncatingWaveTableValueFetcher valueFetcher = new TruncatingWaveTableValueFetcher();
		internalDoWithFetcher( valueFetcher );
	}

	public void testLinearInterpolator()
	{
		log.debug("Testing linear interpolator");

		final LinearInterpolatingWaveTableValueFetcher valueFetcher = new LinearInterpolatingWaveTableValueFetcher();
		internalDoWithFetcher( valueFetcher );
	}

	public void testCubicInterpolator()
	{
		log.debug("Testing cubic interpolator");

		final CubicInterpolatingWaveTableValueFetcher valueFetcher = new CubicInterpolatingWaveTableValueFetcher();
		internalDoWithFetcher( valueFetcher );
	}

	private void internalDoWithFetcher( final WaveTableValueFetcher valueFetcher )
	{
		final SineRawWaveTableGenerator sineGenerator = new SineRawWaveTableGenerator();
		final CubicPaddedRawWaveTable sineTable = sineGenerator.reallyGenerateWaveTable( 4, 1 );

//		final float[] sourceData = new float[] {
//				-0.25f,		//-1
//				0.0f,		// 0
//				0.25f,		// 1
//				0.5f,		// 2
//				0.25f,		// 3
//				0.0f,		// 4
//				-0.25f,		// 5
//				0.0f		// 6
//		};
		final float[] sourceData = sineTable.buffer;

		final int sourceDataLength = sourceData.length - CubicPaddedRawWaveTable.NUM_EXTRA_SAMPLES_IN_BUFFER;

//		final CubicPaddedRawWaveTable sourceWaveTable = new CubicPaddedRawWaveTable( sourceData );
		final CubicPaddedRawWaveTable sourceWaveTable = sineTable;

		final int numStepPositions = (sourceDataLength * 2) + 1;
		final float[] stepPositions = new float[numStepPositions];
		for( int i = 0 ; i < numStepPositions ; ++i )
		{
			stepPositions[i] = ((float)i / (numStepPositions-1) );
		}
		stepPositions[numStepPositions-1] = 0.9999999f;

		final float[] stepResults = getValuesUsingValueFetcher( stepPositions, valueFetcher, sourceWaveTable );

		for( int i = 0 ; i < numStepPositions ; ++i )
		{
			final float pos = stepPositions[i];
			final float value = stepResults[i];

			log.debug( "Val " + i + " at pos " +
					MathFormatter.slowFloatPrint( pos, 10, true ) + " is " +
					MathFormatter.slowFloatPrint( value, 10, true ) );
		}

		final float[] testPositions = new float[] {
				0.0000001f,
				0.0001f,
				0.9999f,
				0.9999999f,
				0.125f
		};

		final float[] eenieResults = getValuesUsingValueFetcher( testPositions, valueFetcher, sourceWaveTable );

		for( int i = 0 ; i < testPositions.length ; ++i )
		{
			final float testPos = testPositions[i];
			final float value = eenieResults[i];
			log.debug( "Value at pos " +
					MathFormatter.slowFloatPrint( testPos, 10, true ) + " is " +
					MathFormatter.slowFloatPrint( value, 10, true ) );
		}
	}

	private float[] getValuesUsingValueFetcher( final float[] testPositions, final WaveTableValueFetcher fetcher,
			final CubicPaddedRawWaveTable sourceWaveTable )
	{
		final float[] results = new float[ testPositions.length ];
		for( int i = 0 ; i < testPositions.length ; ++i )
		{
			final float testPos = testPositions[i];
			results[i] = fetcher.getValueAtNormalisedPosition( sourceWaveTable, testPos );
		}
		return results;
	}
}
