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
import uk.co.modularaudio.util.audio.stft.StftException;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesisStep;
import uk.co.modularaudio.util.audio.stft.tools.ComplexPolarConverter;
import uk.co.modularaudio.util.audio.stft.tools.PhaseFrequencyConverter;

public abstract class StftAbstractPhaseUnwrappingWrappingFrameProcessor implements StftFrameProcessor
{
//	private static Log log = LogFactory.getLog( StftAbstractPhaseUnwrappingWrappingFrameProcessor.class.getName() );
	
	protected StftParameters params = null;
	protected int numReals = -1;
	protected int complexArraySize = -1;
	protected int numBins = -1;
	
	protected int analysisStepSize = -1;
	protected int sampleRate = 0;
	protected int numChannels = -1;
	protected float totalFrequencyHeadroom = 0.0f;
	protected float freqPerBin = 0.0f;
	protected float[][] oldAnalPhases = null;
	protected float[][] oldSynthPhases = null;
	
	protected ComplexPolarConverter complexPolarConverter = null;
	protected PhaseFrequencyConverter phaseFrequencyConverter = null;
	
	protected StftDataFrame lastDataFrame = null;
	
	@Override
	public abstract int getNumFramesNeeded();

	@Override
	public void setParams( StftParameters params )
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
	public int processIncomingFrame( StftDataFrame outputFrame,
			ArrayList<StftDataFrame> lookaheadFrames,
			StftFrameSynthesisStep synthStep )
	{
		StftDataFrame curFrame = lookaheadFrames.get( 0 );
		
		float[][] complexFrame = curFrame.complexFrame;
		float[][] amps = curFrame.amps;
		float[][] phases = curFrame.phases;
		float[][] freqs = curFrame.freqs;
		float inputScal = curFrame.inputScal;
		float inputFac = curFrame.inputFac;
		
		complexToPolarUnwrapPhase( curFrame, complexFrame, amps, phases, freqs, oldAnalPhases, inputScal, inputFac );
		
		processUnwrappedIncomingFrame( outputFrame,
				lookaheadFrames,
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
	
	/**
	 * Intended to be used as helper by subclasses
	 * @param outputFrame
	 * @param outputScal
	 * @param outputFac
	 * @param synthStep
	 * @param isFreqs
	 * @throws StftException
	 */
	public void processWrappedOutgoingFrame( StftDataFrame outputFrame,
			float twoPiSynthStepSizeOverSampleRate,
			StftFrameSynthesisStep synthStep,
			boolean isFreqs )
	{
//		double speedScale = synthStep.getSpeedScale();

//		float[][] complexFrame = outputFrame.complexFrame;
//		float[][] amps = outputFrame.amps;
		float[][] phases = outputFrame.phases;
		float[][] freqs = outputFrame.freqs;
		
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

	private void complexToPolarUnwrapPhase( StftDataFrame frame,
			float[][] complexFrameArr,
			float[][] ampsArr,
			float[][] phasesArr,
			float[][] freqsArr,
			float[][] oldAnalPhasesArr,
			float inputScal,
			float inputFac )
	{
		complexPolarConverter.complexToPolar( frame );
			
		// Use the peak finder to remove anything that isn't a peak
		//		peakFinder.identifyPeaks( amps, phases, peakBuffer, binToPeakBuffer );
		//		peakFinder.quickHackBinZeroing( binToPeakBuffer, amps );
		phaseFrequencyConverter.allPhaseToFreqArr( phasesArr,
				oldAnalPhasesArr,
				freqsArr );
	}

	public StftDataFrame getLastDataFrame()
	{
		return lastDataFrame;
	}
}
