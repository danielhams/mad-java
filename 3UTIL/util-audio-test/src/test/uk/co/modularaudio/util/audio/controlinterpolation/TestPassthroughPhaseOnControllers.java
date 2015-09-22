package test.uk.co.modularaudio.util.audio.controlinterpolation;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.dsp.CDButterworthFilter;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.lang.ArrayUtils;

public class TestPassthroughPhaseOnControllers
{
	private static Log log = LogFactory.getLog( TestPassthroughPhaseOnControllers.class.getName() );

	private static final String SOURCE_CONTROL_WAV = "/home/dan/Temp/source_control2.wav";

	private static final String OUT_FILE = "/home/dan/Temp/processed_control.wav";

	private static final float FILTER_FREQ = 70.0f;

	public TestPassthroughPhaseOnControllers()
	{
	}

	public void go() throws Exception
	{
		log.info("Reading source control file from " + SOURCE_CONTROL_WAV );

		final WaveFileReader controlReader = new WaveFileReader( SOURCE_CONTROL_WAV );

		final int numChannels = controlReader.getNumChannels();
		final int sampleRate = controlReader.getSampleRate();
		final long numFrames = controlReader.getNumTotalFrames();

		log.info("File has " + numFrames + " frames of " + numChannels + " channels at " + sampleRate );

		if( numChannels != 1 )
		{
			throw new IOException("Don't support multi-channel controls for now");
		}

		final int numFramesInt = (int)numFrames;
		final float[] origFloats = new float[numFramesInt];

		final float[] sourceFloats = new float[numFramesInt];

		final float[] filter1Floats = new float[numFramesInt];
		final float[] filter2Floats = new float[numFramesInt];
		final float[] onePassFloats = new float[numFramesInt];

		final float[] diffFloats = new float[numFramesInt];

		controlReader.readFrames( origFloats, 0, 0, numFramesInt );
		controlReader.close();

		// Linear interpolate the values into the source floats array
		float liStartValue = origFloats[0];
		final int periodLengthFrames = 1024;

		final ControlValueInterpolator valueInterpolator = new LinearInterpolator();
		final int LINEAR_INTERPOLATION_LENGTH = 32;
		valueInterpolator.resetSampleRateAndPeriod( sampleRate, LINEAR_INTERPOLATION_LENGTH );
		valueInterpolator.hardSetValue( liStartValue );

		int currentFramePos = 0;
		int numLeft = numFramesInt;

		while( numLeft > 0 )
		{
			int numToCheck = ( numLeft < periodLengthFrames ? numLeft : periodLengthFrames );

			while( numToCheck > 0 )
			{
				int numThisRound = numToCheck;
				boolean wasChange = false;
				float checkValue;
				int f=0;
				do
				{
					checkValue = origFloats[currentFramePos+f];
					if( checkValue != liStartValue )
					{
						numThisRound = f;
						wasChange = true;
						break;
					}
					f++;
				}
				while( f < numToCheck );

				valueInterpolator.generateControlValues( sourceFloats, currentFramePos, numThisRound );
				valueInterpolator.checkForDenormal();

				if( wasChange )
				{
					valueInterpolator.notifyOfNewValue( checkValue );
					liStartValue = checkValue;
				}

				currentFramePos += numThisRound;
				numLeft -= numThisRound;

				numToCheck -= numThisRound;
			}
		}

		final int NUM_PASSES = 2;

		// Initialise our low pass filters
		// We only use non 24 db since we are doing two passes it's equivalent.
		final CDButterworthFilter filter1 = new CDButterworthFilter();
		final CDButterworthFilter filter2 = new CDButterworthFilter();

		// Copy over to onePassFloats ready for loop
		System.arraycopy( origFloats, 0, onePassFloats, 0, numFramesInt );

		for( int p = 0 ; p < NUM_PASSES ; ++p )
		{
			System.arraycopy( onePassFloats, 0, filter1Floats, 0, numFramesInt );
			// Forward filter pass
			filter1.filter( filter1Floats, 0, numFramesInt, FILTER_FREQ, 1.0f, FrequencyFilterMode.LP, sampleRate );

			if( (NUM_PASSES % 2 == 1) &&
					p == NUM_PASSES - 1 )
			{
				// Single filter application
				System.arraycopy( filter1Floats, 0, onePassFloats, 0, numFramesInt );
			}
			else
			{
				// Copy over to filter2 and reverse pass
				System.arraycopy( filter1Floats, 0, filter2Floats, 0, numFramesInt );
				ArrayUtils.reverse( filter2Floats, 0, numFramesInt );
				filter2.filter( filter2Floats, 0, numFramesInt, FILTER_FREQ, 1.0f, FrequencyFilterMode.LP, sampleRate );

				// Copy over to onepass and reverse
				System.arraycopy( filter2Floats, 0, onePassFloats, 0, numFramesInt );
				ArrayUtils.reverse( onePassFloats, 0, numFramesInt );
			}

		}

		// Now create the diff
		float prevSourceValue = 0.0f;
		for( int i = 0 ; i < numFramesInt ; ++i )
		{
			final float sourceFloat = sourceFloats[i];
			final float diffFloat = sourceFloat - onePassFloats[i];
			diffFloats[i] = diffFloat;
			if( sourceFloat != prevSourceValue )
			{
				prevSourceValue = sourceFloat;
			}
			else
			{
				if( Math.abs(diffFloat) < AudioMath.MIN_FLOATING_POINT_16BIT_VAL_F )
				{
					prevSourceValue = sourceFloat;
				}
			}
		}

		final int NUM_OUT_CHANNELS = 4;

		final WaveFileWriter controlWriter = new WaveFileWriter( OUT_FILE, NUM_OUT_CHANNELS, sampleRate, (short)32 );

		final float[] tmpFrame = new float[NUM_OUT_CHANNELS];

		for( int i = 0 ; i < numFramesInt ; ++i )
		{
			tmpFrame[0] = origFloats[i];
			tmpFrame[1] = sourceFloats[i];
			tmpFrame[2] = onePassFloats[i];
			tmpFrame[3] = diffFloats[i];

			controlWriter.writeFrames( tmpFrame, 0, 1 );
		}
		controlWriter.close();
	}

	public static void main( final String[] args ) throws Exception
	{
		final TestPassthroughPhaseOnControllers t = new TestPassthroughPhaseOnControllers();
		t.go();
	}

}
