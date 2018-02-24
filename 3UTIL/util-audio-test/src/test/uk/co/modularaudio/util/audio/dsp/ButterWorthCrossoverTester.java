/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package test.uk.co.modularaudio.util.audio.dsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.dsp.ButterworthCrossover;
import uk.co.modularaudio.util.audio.dsp.ButterworthFilter;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;

public class ButterWorthCrossoverTester
{
	private final static Log LOG = LogFactory.getLog( ButterWorthCrossoverTester.class );

	private final static String TEST_FILE = "/home/dan/Music/CanLoseMusic/SimpleWavs/ExampleBeats.wav";
	private final static String OUT_REG_LP_FILE = "tmpoutput/bwco_reg_lp_out.wav";
	private final static String OUT_REG_HP_FILE = "tmpoutput/bwco_reg_hp_out.wav";
	private final static String OUT_CO_LP_FILE = "tmpoutput/bwco_co_lp_out.wav";
	private final static String OUT_CO_HP_FILE = "tmpoutput/bwco_co_hp_out.wav";

	public ButterWorthCrossoverTester()
	{
	}

	public void go() throws Exception
	{
		final ButterworthFilter regularLp = new ButterworthFilter();
		final ButterworthFilter regularHp = new ButterworthFilter();
		final ButterworthCrossover crossover = new ButterworthCrossover();

		final float FREQUENCY = 500.0f;

		final WaveFileReader inputFile = new WaveFileReader( TEST_FILE );

		final int sampleRate = inputFile.getSampleRate();

		final short numBitsForFileWriting = 32;
		final WaveFileWriter outRegLp = new WaveFileWriter( OUT_REG_LP_FILE, 1, sampleRate, numBitsForFileWriting );
		final WaveFileWriter outRegHp = new WaveFileWriter( OUT_REG_HP_FILE, 1, sampleRate, numBitsForFileWriting );
		final WaveFileWriter outCoLp = new WaveFileWriter( OUT_CO_LP_FILE, 1, sampleRate, numBitsForFileWriting );
		final WaveFileWriter outCoHp = new WaveFileWriter( OUT_CO_HP_FILE, 1, sampleRate, numBitsForFileWriting );

		final int BUFFER_LENGTH = 2048;
		final float[] samplesToFilter = new float[BUFFER_LENGTH];

		final int numChannels = inputFile.getNumChannels();
		final float[] ifBuffer = new float[BUFFER_LENGTH * numChannels];

		final float[] rlpBuffer = new float[BUFFER_LENGTH];
		final float[] rhpBuffer = new float[BUFFER_LENGTH];
		final float[] clpBuffer = new float[BUFFER_LENGTH];
		final float[] chpBuffer = new float[BUFFER_LENGTH];

		final long numTotalFrames = inputFile.getNumTotalFrames();
		long frameReadPosition = 0;

		final long maxSampleIndex = 4096 * 4;

		while( frameReadPosition < maxSampleIndex )
		{
			final long numLeft = (numTotalFrames - frameReadPosition);
			final int numThisRound = (int)( numLeft > BUFFER_LENGTH ? BUFFER_LENGTH : numLeft );
			inputFile.readFrames( ifBuffer, 0, frameReadPosition, numThisRound );

			if( numChannels == 1 )
			{
				System.arraycopy( ifBuffer, 0, samplesToFilter, 0, numThisRound );
			}
			else
			{
				for( int i = 0 ; i < numThisRound ; ++i )
				{
					samplesToFilter[i] = ifBuffer[ i * numChannels ];
				}
			}

			LOG.info( "Processing frames " + frameReadPosition + " to " + (frameReadPosition + numThisRound ) );

			System.arraycopy( samplesToFilter, 0, rlpBuffer, 0, numThisRound );
			System.arraycopy( samplesToFilter, 0, rhpBuffer, 0, numThisRound );

			regularLp.filter( rlpBuffer, 0, numThisRound, FREQUENCY, 0.0f, FrequencyFilterMode.LP, sampleRate );
			regularHp.filter( rhpBuffer, 0, numThisRound, FREQUENCY, 0.0f, FrequencyFilterMode.HP, sampleRate );

			crossover.filter( samplesToFilter, 0, numThisRound, FREQUENCY, sampleRate, clpBuffer, 0, chpBuffer, 0 );

			outRegLp.writeFrames( rlpBuffer, 0, numThisRound );
			outRegHp.writeFrames( rhpBuffer, 0, numThisRound );
			outCoLp.writeFrames( clpBuffer, 0, numThisRound );
			outCoHp.writeFrames( chpBuffer, 0, numThisRound );

			frameReadPosition += numThisRound;
		}

		outRegLp.close();
		outRegHp.close();
		outCoLp.close();
		outCoHp.close();
	}

	public static void main( final String[] argv ) throws Exception
	{
		final ButterWorthCrossoverTester t = new ButterWorthCrossoverTester();

		t.go();
	}
}
