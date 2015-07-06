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

package test.uk.co.modularaudio.util.audio.fft;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtransforms.fft.DoubleFFT_1D;

import uk.co.modularaudio.util.math.MathFormatter;

public class JTransformsTester
{
	private static Log log = LogFactory.getLog( JTransformsTester.class.getName() );

	public static void main( final String[] args )
	{
		final JTransformsTester tester = new JTransformsTester();
		tester.go();
	}

	private void go()
	{
		log.debug("Beginning.");

		final int numReals = 8;
		final int numBins = numReals + 1;
		final int fftSize = numReals * 2;
		final int fftComplexArraySize = numBins * 2;

		final DoubleFFT_1D fftEngine = new DoubleFFT_1D( fftSize );

		// Eight input values - same as frame size
//		double[] discreteIn = new double[] { 0.0, 0.5, 1.0, 0.5,
//				0.0, -0.5, -1.0, -0.5 };
		final double[] discreteIn = new double[] { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };

		// Sixteen output places for the spectrum data
		final double[] spectrumOut = new double[ fftComplexArraySize ];

		for( int i = 0 ; i < numReals ; i++ )
		{
			spectrumOut[ i ] = discreteIn[i];
		}

		debugArray( "SpectrumOut before FFT", spectrumOut );

		fftEngine.realForward( spectrumOut );

		debugArray( "SpectrumOut", spectrumOut );

		// Now try reverse
		fftEngine.realInverse( spectrumOut, true );

		debugArray( "InversedSpectrumOut", spectrumOut );

	}

	private void debugArray(final String name, final double[] out)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( name );
		sb.append( " is [" );
		for( int i = 0 ; i < out.length ; i++ )
		{
			if( i != 0 )
			{
				sb.append( ", " );
			}
			sb.append( MathFormatter.fastFloatPrint( (float)out[i], 3, false  ) );
		}
		sb.append("]");
		log.debug( sb.toString() );
	}
}
