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

package uk.co.modularaudio.util.audio.stft.frame.processing.identity;

import java.util.ArrayList;

import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftFrameHistoryRing;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.frame.processing.StftAbstractPhaseUnwrappingWrappingFrameProcessor;
import uk.co.modularaudio.util.audio.stft.frame.processing.StftFrameProcessorVisualDebugger;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesisStep;
import uk.co.modularaudio.util.math.MathDefines;

public class StftIdentityFrameProcessor extends StftAbstractPhaseUnwrappingWrappingFrameProcessor
{
//	private static Log log = LogFactory.getLog( StftIdentityFrameProcessor.class.getName() );

	private StftParameters params = null;
	private int numChannels = -1;
	private int numBins = -1;
	private int fftSize = -1;

//	private PeakFinder peakFinder = null;

	private int[][] peakChannelBuffers = null;
	private int[][] binToPeakChannelBuffers = null;

	@Override
	public int getNumFramesNeeded()
	{
		return 1;
	}

	@Override
	public boolean isPeakProcessor()
	{
		return true;
	}

	@Override
	public int[][] getPeakChannelBuffers()
	{
		return peakChannelBuffers;
	}

	@Override
	public int[][] getBinToPeakChannelBuffers()
	{
		return binToPeakChannelBuffers;
	}

	@Override
	public void processUnwrappedIncomingFrame(final StftDataFrame outputFrame,
			final ArrayList<StftDataFrame> lookaheadFrames,
			final StftFrameSynthesisStep synthStep )
	{
		final float[][] outputComplexFrame = outputFrame.complexFrame;
		final float[][] outputAmps = outputFrame.amps;
		final float[][] outputPhases = outputFrame.phases;
		final float[][] outputFreqs = outputFrame.freqs;

		// Simply copy over frame[0]
		final StftDataFrame ifr = lookaheadFrames.get( 0 );
		for( int i = 0 ; i < numChannels ; i++ )
		{
			System.arraycopy( ifr.complexFrame[i], 0, outputComplexFrame[i], 0, fftSize );
			System.arraycopy( ifr.amps[i], 0, outputAmps[i], 0, numBins );
			System.arraycopy( ifr.phases[i], 0, outputPhases[i], 0, numBins );
			System.arraycopy( ifr.freqs[i], 0, outputFreqs[i], 0, numBins );
		}
		outputFrame.position = ifr.position;

//		float[] analAmps = ifr.amps;
//		float[] analPhases = ifr.phases;
//		float[] analFreqs = ifr.freqs;

//		peakFinder.identifyPeaks( analAmps, analPhases, peaksBuffer, binToPeakBuffer );
//		peakFinder.quickZeroingLeaveBins(binToPeakBuffer, outputAmps, 0, false, false, false);

		final float twoPiSynthStepSizeOverSampleRate = (MathDefines.TWO_PI_F * synthStep.getRoundedStepSize()) / params.getSampleRate();

		processWrappedOutgoingFrame( outputFrame, twoPiSynthStepSizeOverSampleRate, synthStep, true );

	}

	@Override
	public void processUnwrappedIncomingFrame(final StftDataFrame outputFrame,
			final StftFrameHistoryRing frameHistoryRing,
			final StftFrameSynthesisStep synthStep )
	{
		final float[][] outputComplexFrame = outputFrame.complexFrame;
		final float[][] outputAmps = outputFrame.amps;
		final float[][] outputPhases = outputFrame.phases;
		final float[][] outputFreqs = outputFrame.freqs;

		// Simply copy over frame[0]
		final StftDataFrame ifr = frameHistoryRing.getFrame( 0 );
		for( int i = 0 ; i < numChannels ; i++ )
		{
			System.arraycopy( ifr.complexFrame[i], 0, outputComplexFrame[i], 0, fftSize );
			System.arraycopy( ifr.amps[i], 0, outputAmps[i], 0, numBins );
			System.arraycopy( ifr.phases[i], 0, outputPhases[i], 0, numBins );
			System.arraycopy( ifr.freqs[i], 0, outputFreqs[i], 0, numBins );
		}
		outputFrame.position = ifr.position;

//		float[] analAmps = ifr.amps;
//		float[] analPhases = ifr.phases;
//		float[] analFreqs = ifr.freqs;

//		peakFinder.identifyPeaks( analAmps, analPhases, peaksBuffer, binToPeakBuffer );
//		peakFinder.quickZeroingLeaveBins(binToPeakBuffer, outputAmps, 0, false, false, false);

		final float twoPiSynthStepSizeOverSampleRate = (MathDefines.TWO_PI_F * synthStep.getRoundedStepSize()) / params.getSampleRate();

		processWrappedOutgoingFrame( outputFrame, twoPiSynthStepSizeOverSampleRate, synthStep, true );

	}

	@Override
	public void setParams(final StftParameters params)
	{
		super.setParams(params);
		this.params = params;
		this.numChannels = params.getNumChannels();
		this.numBins = params.getNumBins();
		this.fftSize = params.getNumReals();
		peakChannelBuffers = new int[ numChannels ][ numBins ];
		binToPeakChannelBuffers = new int[ numChannels ][ numBins ];

//		peakFinder = new PeakFinder( params );
	}

	@Override
	public void reset()
	{
	}

	@Override
	public boolean isSynthesisingProcessor()
	{
		// We create output, yes
		return true;
	}

	@Override
	public StftFrameProcessorVisualDebugger getDebuggingVisualComponent()
	{
		return null;
	}
}
