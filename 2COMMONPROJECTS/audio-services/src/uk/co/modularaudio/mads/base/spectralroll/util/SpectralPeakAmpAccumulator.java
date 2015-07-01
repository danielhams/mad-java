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

package uk.co.modularaudio.mads.base.spectralroll.util;

import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftFrameHistoryRing;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.frame.processing.StftFrameProcessor;
import uk.co.modularaudio.util.audio.stft.frame.processing.StftFrameProcessorVisualDebugger;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesisStep;
import uk.co.modularaudio.util.audio.stft.tools.ComplexPolarConverter;

public class SpectralPeakAmpAccumulator implements StftFrameProcessor
{
//	private static Log log = LogFactory.getLog( SpectralPeakAmpAccumulator.class.getName());

	private int numChannels;
	private int numReals;
	private int complexArraySize;
	private int numBins;
	private ComplexPolarConverter complexPolarConverter;
	private float[][] computedAmps;

	private static final float CUR_VAL_WEIGHT = 0.5f;
	private static final float PREV_VAL_WEIGHT = 0.5f;

	// Debugging
	private StftDataFrame lastDataFrame;

	private boolean ampsTaken = true;

	public SpectralPeakAmpAccumulator()
	{
	}

	@Override
	public boolean isSynthesisingProcessor()
	{
		return false;
	}

	@Override
	public int getNumFramesNeeded()
	{
		return 1;
	}

	@Override
	public void setParams( final StftParameters params )
	{
//		this.parameters = params;
		this.numChannels = params.getNumChannels();
		this.numReals = params.getNumReals();
		this.complexArraySize = params.getComplexArraySize();
		this.numBins = params.getNumBins();
		this.complexPolarConverter = new ComplexPolarConverter( params );
		this.computedAmps = new float[numChannels][];
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			computedAmps[chan] = new float[ numBins ];
		}

		lastDataFrame = new StftDataFrame( numChannels, numReals, complexArraySize, numBins );
//		peakChannelBuffers = new int[numChannels][numBins];
//		binToPeakChannelBuffers = new int[numChannels][numBins];
	}


	@Override
	public int processIncomingFrame( final StftDataFrame outputFrame,
			final StftFrameHistoryRing frameHistoryRing,
			final StftFrameSynthesisStep synthStep )
	{
		final StftDataFrame curFrame = frameHistoryRing.getFrame( 0 );
		complexPolarConverter.complexToPolarAmpsOnly( curFrame );
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			for( int s = 0 ; s < numBins ; s++ )
			{
				final float prevValue = computedAmps[chan][s];
//				if( Float.isNaN( prevValue ) )
//				{
//					log.error("Was the previous value..");
//					prevValue = 0.0f;
//				}
				final float newValue = curFrame.amps[chan][s];
//				if( Float.isNaN(newValue) )
//				{
//					// Came from dodgy fft results...
//					log.error("It begins...");
//					newValue = 0.0f;
//				}
				computedAmps[chan][s] = (prevValue * PREV_VAL_WEIGHT) +
						(newValue * CUR_VAL_WEIGHT );
//				if( Float.isNaN( computedAmps[chan][s] ) )
//				{
//					log.error("It was the computed value");
//					computedAmps[chan][s] = 0.0f;
//				}
			}
//			System.arraycopy( curFrame.amps[chan], 0, computedAmps[chan], 0, numBins );
		}
		lastDataFrame = outputFrame;
		ampsTaken = false;

		return 0;
	}

	@Override
	public boolean isPeakProcessor()
	{
		return false;
	}

	@Override
	public void reset()
	{
	}

	@Override
	public StftFrameProcessorVisualDebugger getDebuggingVisualComponent()
	{
		return null;
	}

	@Override
	public StftDataFrame getLastDataFrame()
	{
		return lastDataFrame;
	}

	@Override
	public int[][] getPeakChannelBuffers()
	{
		return new int[0][];
	}

	@Override
	public int[][] getBinToPeakChannelBuffers()
	{
		return new int[0][];
	}

	public float[][] getComputedAmpsMarkTaken()
	{
		ampsTaken = true;
		return computedAmps;
	}

	public boolean hasNewAmps()
	{
		return !ampsTaken;
	}

}
