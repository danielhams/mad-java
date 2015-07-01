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

package uk.co.modularaudio.util.audio.stft.frame.processing;

import java.util.ArrayList;

import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftFrameHistoryRing;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesisStep;
import uk.co.modularaudio.util.audio.stft.tools.ComplexPolarConverter;
import uk.co.modularaudio.util.audio.stft.tools.PhaseFrequencyConverter;

public abstract class StftAbstractPhaseUnwrappingWrappingFrameProcessor implements StftFrameProcessor
{
//	private static Log log = LogFactory.getLog( StftAbstractPhaseUnwrappingWrappingFrameProcessor.class.getName() );

	protected StftParameters params;
	protected int numReals;
	protected int complexArraySize;
	protected int numBins;

	protected int analysisStepSize;
	protected int sampleRate;
	protected int numChannels;
	protected float totalFrequencyHeadroom;
	protected float freqPerBin;
	protected float[][] oldAnalPhases;
	protected float[][] oldSynthPhases;

	protected ComplexPolarConverter complexPolarConverter;
	protected PhaseFrequencyConverter phaseFrequencyConverter;

	protected StftDataFrame lastDataFrame;

	@Override
	public abstract int getNumFramesNeeded();

	@Override
	public void setParams( final StftParameters params )
	{
		this.params = params;
		this.numChannels = params.getNumChannels();
		this.numReals = params.getNumReals();
		this.complexArraySize = params.getComplexArraySize();
		this.numBins = params.getNumBins();
		this.analysisStepSize = params.getStepSize();
		this.sampleRate = params.getSampleRate();
		this.totalFrequencyHeadroom = sampleRate / 2;
		// phase freq conversion multipliers
		freqPerBin = totalFrequencyHeadroom / (numBins - 1);

		this.oldAnalPhases = new float[ numChannels ][];
		this.oldSynthPhases = new float[ numChannels ][];
		for( int i = 0 ; i < numChannels ; i++ )
		{
		this.oldAnalPhases[i] = new float[ numBins ];
		this.oldSynthPhases[i] = new float[ numBins ];
		}

		complexPolarConverter = new ComplexPolarConverter( params );

		phaseFrequencyConverter = new PhaseFrequencyConverter( params );

		lastDataFrame = new StftDataFrame( numChannels, numReals, complexArraySize, numBins );

	}

	@Override
	public int processIncomingFrame( final StftDataFrame outputFrame,
			final StftFrameHistoryRing frameHistoryRing,
			final StftFrameSynthesisStep synthStep )
	{
		final StftDataFrame curFrame = frameHistoryRing.getFrame( 0 );

		final float[][] complexFrame = curFrame.complexFrame;
		final float[][] amps = curFrame.amps;
		final float[][] phases = curFrame.phases;
		final float[][] freqs = curFrame.freqs;
		final float inputScal = curFrame.inputScal;
		final float inputFac = curFrame.inputFac;

		complexToPolarUnwrapPhase( curFrame, complexFrame, amps, phases, freqs, oldAnalPhases, inputScal, inputFac );

		processUnwrappedIncomingFrame( outputFrame,
				frameHistoryRing,
				synthStep );

		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			System.arraycopy( phases[chan], 0, oldAnalPhases[chan], 0, numBins );
			System.arraycopy( outputFrame.phases[chan], 0, oldSynthPhases[chan], 0, numBins );

			System.arraycopy( outputFrame.amps[chan], 0, lastDataFrame.amps[chan], 0, numBins );
			System.arraycopy( outputFrame.freqs[chan], 0, lastDataFrame.freqs[chan], 0, numBins );
			System.arraycopy( outputFrame.phases[chan], 0, lastDataFrame.phases[chan], 0, numBins );
		}
		return 0;
	}

	public abstract void processUnwrappedIncomingFrame( StftDataFrame outputFrame,
			ArrayList<StftDataFrame> lookaheadFrames,
			StftFrameSynthesisStep synthStep );

	public abstract void processUnwrappedIncomingFrame( StftDataFrame outputFrame,
			StftFrameHistoryRing frameHistoryRing,
			StftFrameSynthesisStep synthStep );

	public void processWrappedOutgoingFrame( final StftDataFrame outputFrame,
			final float twoPiSynthStepSizeOverSampleRate,
			final StftFrameSynthesisStep synthStep,
			final boolean isFreqs )
	{
//		double speedScale = synthStep.getSpeedScale();

//		float[][] complexFrame = outputFrame.complexFrame;
//		float[][] amps = outputFrame.amps;
		final float[][] phases = outputFrame.phases;
		final float[][] freqs = outputFrame.freqs;

		// Debug the conversion of one freq to phase
		phaseFrequencyConverter.allFreqToPhaseArr( synthStep.getRoundedStepSize(),
				twoPiSynthStepSizeOverSampleRate,
				oldSynthPhases,
				phases,
				freqs );

		lastDataFrame = outputFrame;
	}

	@Override
	public abstract boolean isPeakProcessor();

	private void complexToPolarUnwrapPhase( final StftDataFrame frame,
			final float[][] complexFrameArr,
			final float[][] ampsArr,
			final float[][] phasesArr,
			final float[][] freqsArr,
			final float[][] oldAnalPhasesArr,
			final float inputScal,
			final float inputFac )
	{
		complexPolarConverter.complexToPolar( frame );

		// Use the peak finder to remove anything that isn't a peak
		//		peakFinder.identifyPeaks( amps, phases, peakBuffer, binToPeakBuffer );
		//		peakFinder.quickHackBinZeroing( binToPeakBuffer, amps );
		phaseFrequencyConverter.allPhaseToFreqArr( phasesArr,
				oldAnalPhasesArr,
				freqsArr );
	}

	@Override
	public StftDataFrame getLastDataFrame()
	{
		return lastDataFrame;
	}
}
