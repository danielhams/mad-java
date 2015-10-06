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

package uk.co.modularaudio.mads.base.specampgen.util;

import java.util.Arrays;

import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftException;
import uk.co.modularaudio.util.audio.stft.StftParameters;

public class OneshotStftProcessor
{
//		private static Log log = LogFactory.getLog( OneshotStftProcessor.class.getName() );

	private final StftParameters params;
	private final SpectralPeakAmpAccumulator frameProcessor;

	private final FloatFFT_1D fftComputer;
	private final FftWindow fftWindow;

	private final StftDataFrame internalDataFrame;

	public OneshotStftProcessor( final StftParameters params, final SpectralPeakAmpAccumulator frameProcessor )
			throws StftException
	{
		this.params = params;
		this.frameProcessor = frameProcessor;
		frameProcessor.setParams( params );

		fftComputer = new FloatFFT_1D( params.getNumReals() );
		fftWindow = params.getFftWindow();

		internalDataFrame = new StftDataFrame( params.getNumChannels(),
				params.getNumReals(),
				params.getComplexArraySize(),
				params.getNumBins() );
	}

	public StftParameters getParameters()
	{
		return params;
	}

	public void doStftSingleArg( final float[][] wolaArray, final int readStartOffset, final int numStraightRead )
	{
		// Copy into data frame and then call internal processing
		for( int c = 0 ; c < params.getNumChannels() ; ++c )
		{
			System.arraycopy( wolaArray[c], readStartOffset, internalDataFrame.complexFrame[c], 0, numStraightRead );
		}
		doInternalStft();
	}

	public void doStftDoubleArg( final float[][] wolaArray,
			final int readStartOffset, final int numStraightRead,
			final int wrappedStartOffset, final int numWrappedRead )
	{
		// Copy into data frame and then call internal processing
		for( int c = 0 ; c < params.getNumChannels() ; ++c )
		{
			System.arraycopy( wolaArray[c], readStartOffset, internalDataFrame.complexFrame[c], 0, numStraightRead );
			System.arraycopy( wolaArray[c], wrappedStartOffset, internalDataFrame.complexFrame[c],
					numStraightRead,
					numWrappedRead );
		}
		doInternalStft();
	}

	private void doInternalStft()
	{
		for( int c = 0 ; c < params.getNumChannels() ; ++c )
		{
			Arrays.fill( internalDataFrame.complexFrame[c], params.getWindowLength(), params.getComplexArraySize(), 0.0f );
			// Window it
			fftWindow.apply( internalDataFrame.complexFrame[c] );
			// Forward fft it
			fftComputer.realForward( internalDataFrame.complexFrame[c] );
		}

		// Now pass the completed frame to the frame processor.
		frameProcessor.processIncomingFrame( internalDataFrame );
	}

}
