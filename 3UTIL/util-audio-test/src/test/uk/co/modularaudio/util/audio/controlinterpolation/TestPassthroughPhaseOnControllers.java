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

	private static final String FILES_DIR = "/home/dan/Temp/";
	private static final String CONTROL_NAME = "bcd3000fadermovements";
//	private static final String CONTROL_NAME = "bcd3000eqknob";
	private static final String IN_CONTROL_FILENAME = FILES_DIR + CONTROL_NAME + ".wav";
	private static final String OUT_CONTROL_FILENAME = FILES_DIR + CONTROL_NAME + "_out.wav";

	private static final float FILTER_FREQ = 100.0f;
//	private static final float FILTER_FREQ = 70.0f;
//	private static final float FILTER_FREQ = 50.0f;

	public TestPassthroughPhaseOnControllers()
	{
	}

	public void go() throws Exception
	{
		log.info("Reading source control file from " + IN_CONTROL_FILENAME );

		final WaveFileReader controlReader = new WaveFileReader( IN_CONTROL_FILENAME );

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

		final boolean USE_INTERPOLATION_FIRST = false;

		if( USE_INTERPOLATION_FIRST )
		{
			float liStartValue = origFloats[0];
			final int periodLengthFrames = 1024;

			final ControlValueInterpolator valueInterpolator = new LinearInterpolator(-1.0f, 1.0f);
//			final ControlValueInterpolator valueInterpolator = new HalfHannWindowInterpolator();
			final int INTERPOLATION_LENGTH = 512;
			valueInterpolator.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, INTERPOLATION_LENGTH );
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
		}
		else
		{
			System.arraycopy( origFloats, 0, sourceFloats, 0, numFramesInt );
		}

		final int NUM_PASSES = 1;

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
			final float sourceFloat = origFloats[i];
			final float diffFloat = onePassFloats[i] - sourceFloat;
			diffFloats[i] = diffFloat;
			if( sourceFloat != prevSourceValue )
			{
				prevSourceValue = sourceFloat;
			}
			else
			{
				if( Math.abs(diffFloat) < AudioMath.MIN_SIGNED_FLOATING_POINT_16BIT_VAL_F )
				{
					prevSourceValue = sourceFloat;
				}
			}
		}

		final int NUM_OUT_CHANNELS = 4;

		final WaveFileWriter controlWriter = new WaveFileWriter( OUT_CONTROL_FILENAME, NUM_OUT_CHANNELS, sampleRate, (short)32 );

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
