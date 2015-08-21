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

package test.uk.co.modularaudio.util.audio.stft;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.math.MathFormatter;

public class FindBinFreqForTests
{
	private static Log log = LogFactory.getLog( FindBinFreqForTests.class.getName() );

	public void go() throws Exception
	{
		final FftWindow fftWindow = new HannFftWindow( 1024 );
		final int numChannels = 1;
		final int windowLength = 2048;
		final int numOverlaps = 4;
		final int numReals = 16384;
		final StftParameters paramsForTest = new StftParameters( DataRate.CD_QUALITY,
				numChannels, windowLength, numOverlaps, numReals, fftWindow );

		final float[] binCenterFreqs = paramsForTest.getBinCenterFreqs();

		for( int i = 0 ; i < 400 ; ++i )
		{
			final String index = String.format( "%04d", i );
			log.info( "Bin " + index + " has center frequency " +
					MathFormatter.slowFloatPrint( binCenterFreqs[i], 12, true ));
		}
	}

	public static void main( final String[] args ) throws Exception
	{
		final FindBinFreqForTests t = new FindBinFreqForTests();
		t.go();
	}

}
