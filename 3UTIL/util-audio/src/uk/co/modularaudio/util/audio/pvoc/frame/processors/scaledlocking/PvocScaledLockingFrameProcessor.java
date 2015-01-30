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

package uk.co.modularaudio.util.audio.pvoc.frame.processors.scaledlocking;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.pvoc.PvocParameters;
import uk.co.modularaudio.util.audio.pvoc.frame.PvocFrameProcessor;
import uk.co.modularaudio.util.audio.pvoc.frame.processors.PvocPeakFinder;
import uk.co.modularaudio.util.audio.pvoc.support.PvocComplexPolarConverter;
import uk.co.modularaudio.util.audio.pvoc.support.PvocDataFrame;
import uk.co.modularaudio.util.audio.pvoc.support.PvocFrameSynthesisStep;
import uk.co.modularaudio.util.audio.pvoc.support.PvocPhaseFrequencyConverter;
import uk.co.modularaudio.util.math.MathDefines;

@SuppressWarnings("unused")
public strictfp class PvocScaledLockingFrameProcessor extends PvocFrameProcessor
{
	private static Log log = LogFactory.getLog( PvocScaledLockingFrameProcessor.class.getName() );

	private int numChannels = -1;
	private int sampleRate = -1;
	private int numBins = -1;
	private int lastBinIndex = -1;

	private float[][] previousSynthPhases = null;
//	private float[][] previousSynthAmps = null;

	// Working data
	private final PvocPeaksWorkingBuffer workingBuffer;

	private PvocPeakFinder peakFinder = null;

	// Good for 2048 fft size - will need to scale it with fft
	private static final int MAX_BIN_CROSSING_TOLERANCE = 3;

	private final PvocComplexPolarConverter complexPolarConverter;

	private final PvocPhaseFrequencyConverter phaseFrequencyConverter;

	private final float[] phasorStorage = new float[2];

	public PvocScaledLockingFrameProcessor( final PvocParameters parameters )
	{
		super( parameters );
		numChannels = parameters.getNumChannels();

		sampleRate = parameters.getSampleRate();
		numBins = parameters.getNumBins();
		lastBinIndex = numBins - 1;

		previousSynthPhases = new float[numChannels][ numBins ];
//		previousSynthAmps = new float[numChannels][ numBins ];

		workingBuffer = new PvocPeaksWorkingBuffer( parameters );

		peakFinder = new PvocPeakFinder( parameters );

		complexPolarConverter = parameters.getComplexPolarConverter();

		phaseFrequencyConverter = parameters.getPhaseFrequencyConverter();
	}

	@Override
	public int getNumFramesNeeded()
	{
		return 2;
	}

	@Override
	public int processIncomingFrame( final PvocDataFrame outputFrame,
			final ArrayList<PvocDataFrame> lookaheadFrames,
			final PvocFrameSynthesisStep synthStep )
	{
		final PvocDataFrame curAnalFrame = lookaheadFrames.get( 0 );
		final PvocDataFrame oldAnalFrame = lookaheadFrames.get( 1 );

		for( int channelNum = 0 ; channelNum < numChannels ; channelNum++ )
		{
			// Compute amps
			peakFinder.computeAmpsSquared( curAnalFrame, channelNum );

			// Now eliminate other bins so we can debug one
			final int numPeaksInPeaksBuffer = peakFinder.identifyPeaks( curAnalFrame, channelNum );

//			// Hack to zero bins around peaks
//			peakFinder.quickZeroingLeaveBins( curAnalFrame, channelNum, 0, false, false, false );
//			peakFinder.identifyPeaks( curAnalFrame, channelNum );

			// Compute some things we will use when calculating freq/phase conversions
			final int synthesisStepSize = synthStep.getRoundedStepSize();

			final float twoPiSynthStepSizeOverSampleRate = (MathDefines.TWO_PI_F * synthesisStepSize) / sampleRate;

			if( numPeaksInPeaksBuffer > 0 )
			{
				// Now correlate peaks with previous peaks
				correlatePeaks( curAnalFrame,
						oldAnalFrame,
						channelNum );

				// Now for "starter" peaks, we initialise with the phase from analysis directly
				if( workingBuffer.numStarterPeaks > 0 )
				{
					fillInStarterPeaks( curAnalFrame,
							outputFrame,
							channelNum );
				}

				// For "running" peaks we are adding on the necessary phase rotations appropriate for the synthesis step size
				if( workingBuffer.numRunningPeaks > 0 )
				{
					fillInRunningPeaksPhasor( curAnalFrame,
							oldAnalFrame,
							outputFrame,
							channelNum,
							synthesisStepSize,
							twoPiSynthStepSizeOverSampleRate );
				}

				// For bin crossing peaks, we use the previous phase from the identified peak and add on how much is appropriate
				// for this step
				if( workingBuffer.numBinCrossingPeaks > 0 )
				{
					fillInBinCrossingPeaksPhasor( curAnalFrame,
							oldAnalFrame,
							outputFrame,
							channelNum,
							synthesisStepSize,
							twoPiSynthStepSizeOverSampleRate );
				}

				// And copy over the dc and nyquist amps into the complex frame
				outputFrame.complexFrame[channelNum][ 0 ] = curAnalFrame.amps[channelNum][ 0 ] * curAnalFrame.dcSign[channelNum];
				outputFrame.complexFrame[channelNum][ 1 ] = 0.0f;
				outputFrame.complexFrame[channelNum][ lastBinIndex * 2 ] = curAnalFrame.amps[channelNum][ lastBinIndex ] * curAnalFrame.nySign[channelNum];
				outputFrame.complexFrame[channelNum][ (lastBinIndex * 2) + 1 ] = 0.0f;
			}
			else
			{
				Arrays.fill( outputFrame.complexFrame[channelNum], 0.0f );
			}

			 // Now copy into old buffers values we need
//			System.arraycopy( outputFrame.amps[channelNum], 0, previousSynthAmps[channelNum], 0, numBins );
			System.arraycopy( outputFrame.phases[channelNum], 0, previousSynthPhases[channelNum], 0, numBins );
		}

//		log.debug( "ComplexFrame[0] contents are: " + MathFormatter.floatArrayPrint( outputFrame.complexFrame[0], 7 ) );

		return 0;
	}

	@Override
	public boolean isPeakProcessor()
	{
		return true;
	}

	@Override
	public boolean isSynthesisingProcessor()
	{
		return true;
	}

	@Override
	public void reset()
	{
	}

	private void correlatePeaks( final PvocDataFrame curAnalFrame,
			final PvocDataFrame oldAnalFrame,
			final int c )
	{
		int runningPeaksIndex = 0;
		int starterPeaksIndex = 0;
		int binCrossingPeaksIndex = 0;

		for( int i = 0 ; i < curAnalFrame.numPeaksInPeaksBuffer[c] ; i++ )
		{
			final int peakBinIndex = curAnalFrame.peaksBuffer[c][ i ];
			// Lookup this peak in the previous binToPeakBuffer and see if there is a correlation
			final int previousPeakForIndex = oldAnalFrame.binToPeakBuffer[c][ peakBinIndex ];
			final int absBinDiff = (previousPeakForIndex > peakBinIndex ? previousPeakForIndex - peakBinIndex : peakBinIndex - previousPeakForIndex );
			if( previousPeakForIndex != -1 &&
					previousPeakForIndex != 0 &&
					absBinDiff <= MAX_BIN_CROSSING_TOLERANCE )
			{
				// Matched peak
				if( previousPeakForIndex == peakBinIndex )
				{
					// Matched running peak
					workingBuffer.outputRunningPeaks[ runningPeaksIndex++ ] = peakBinIndex;
				}
				else
				{
					workingBuffer.outputBinCrossingPeaks[ binCrossingPeaksIndex++ ] = peakBinIndex;
					workingBuffer.outputBinCrossingPeaks[ binCrossingPeaksIndex++ ] = previousPeakForIndex;
				}
			}
			else
			{
				// Didn't match, add it to the "starter" peaks list
				workingBuffer.outputStarterPeaks[ starterPeaksIndex++ ] = peakBinIndex;
			}
		}
		workingBuffer.outputRunningPeaks[ runningPeaksIndex ] = -1;
		workingBuffer.numRunningPeaks = runningPeaksIndex;
		workingBuffer.outputStarterPeaks[ starterPeaksIndex ] = -1;
		workingBuffer.numStarterPeaks = starterPeaksIndex;
		workingBuffer.outputBinCrossingPeaks[ binCrossingPeaksIndex ] = -1;
		workingBuffer.numBinCrossingPeaks = binCrossingPeaksIndex;
	}

	private void fillInRunningPeaksPhasor( final PvocDataFrame curAnalFrame,
			final PvocDataFrame oldAnalFrame,
			final PvocDataFrame outputFrame,
			final int c,
			final int synthesisStepSize,
			final float twoPiSynthStepSizeOverSampleRate )
	{
		for( int r = 0 ; r < workingBuffer.numRunningPeaks ; r++ )
		{
			final int runningBinIndex = workingBuffer.outputRunningPeaks[ r ];
			final int runningPeakLowerBound = curAnalFrame.peakBoundariesBuffer[c][ (runningBinIndex*2) ];
			final int runningPeakUpperBound = curAnalFrame.peakBoundariesBuffer[c][ (runningBinIndex*2)+1 ];

			final float runningBinPhase = complexPolarConverter.oneComplexToPolarPhaseOnly( curAnalFrame.complexFrame[c], runningBinIndex );
			curAnalFrame.phases[c][ runningBinIndex ] = runningBinPhase;
			curAnalFrame.amps[c][ runningBinIndex ] = (float)Math.sqrt( curAnalFrame.ampsSquared[c][ runningBinIndex] );

			final float runningBinFreq = phaseFrequencyConverter.phaseToFreq( runningBinPhase, oldAnalFrame.phases[c][runningBinIndex], runningBinIndex );

			// Recompute new phase
			final float myComputedPeakPhase = phaseFrequencyConverter.freqToPhase(
					twoPiSynthStepSizeOverSampleRate,
					previousSynthPhases[c][ runningBinIndex ],
					runningBinFreq );

			if( PvocPeakFinder.DEBUG_PEAKS && log.isDebugEnabled() )
			{
				log.debug("Found running peak in bin " + runningBinIndex + " at freq " + runningBinFreq + " with amp " + curAnalFrame.amps[c][runningBinIndex] );
				log.debug("Analysis phases: old( "+ oldAnalFrame.phases[c][ runningBinIndex ] + ") new( " + curAnalFrame.phases[c][ runningBinIndex ] + ")");
				log.debug("Synthesis phases: old( "+ previousSynthPhases[c][ runningBinIndex ] + ") new( " + myComputedPeakPhase + ")");
			}

			outputFrame.phases[c][ runningBinIndex ] = myComputedPeakPhase;
			outputFrame.amps[c][ runningBinIndex ] = curAnalFrame.amps[c][ runningBinIndex ];
			// Populate the complex vector using this new phase
			complexPolarConverter.onePolarToComplex( outputFrame.amps[c], outputFrame.phases[c], outputFrame.complexFrame[c], runningBinIndex );

			// Compute the phasor (rotator) from this new peak phase
			final float theta = computePhaseDiff( myComputedPeakPhase, runningBinPhase );

			generatePhasor( theta );
			if( PvocPeakFinder.DEBUG_PEAKS && log.isDebugEnabled() )
			{
				log.debug( "Generated theta " + theta + " and thus a phasor: " + phasorStorage[0] + " " + phasorStorage[1] );
				log.debug( "Will rotate from " + runningPeakLowerBound + " to " + runningPeakUpperBound );
			}

			// Now loop up and down around the peak multiplying the anal complex vecs by this phasor
			for( int i = runningPeakLowerBound ; i < runningBinIndex ; i++ )
			{
				complexMultiply( phasorStorage[0],
						phasorStorage[1],
						curAnalFrame.complexFrame[c][ (2*i) ],
						curAnalFrame.complexFrame[c][ (2*i) + 1 ],
						outputFrame.complexFrame[c],
						2*i );
			}

			for( int i = runningBinIndex + 1 ; i < runningPeakUpperBound ; i++ )
			{
				complexMultiply( phasorStorage[0],
						phasorStorage[1],
						curAnalFrame.complexFrame[c][ (2*i) ],
						curAnalFrame.complexFrame[c][ (2*i) + 1 ],
						outputFrame.complexFrame[c],
						2*i );
			}
		}
	}

	private void fillInStarterPeaks( final PvocDataFrame curAnalFrame, final PvocDataFrame outputFrame, final int c )
	{
		for( int i = 0 ; i < workingBuffer.numStarterPeaks ; i++ )
		{
			final int starterBinIndex = workingBuffer.outputStarterPeaks[ i ];
			final int peakLowerBound = curAnalFrame.peakBoundariesBuffer[c][ (starterBinIndex*2) ];
			final int peakUpperBound = curAnalFrame.peakBoundariesBuffer[c][ (starterBinIndex*2)+1 ];

			final float curAnalPhase = complexPolarConverter.oneComplexToPolarPhaseOnly( curAnalFrame.complexFrame[c], starterBinIndex );
			curAnalFrame.phases[c][ starterBinIndex ] = curAnalPhase;

			if( PvocPeakFinder.DEBUG_PEAKS )
			{
				log.debug("Starter peak( "+ starterBinIndex + ") copying phase from analysis " + curAnalPhase );
				log.debug("Complex contents were (" + curAnalFrame.complexFrame[c][ starterBinIndex * 2] + ", " +
						curAnalFrame.complexFrame[c][ (starterBinIndex * 2) + 1 ] + ")");
			}

			outputFrame.phases[c][ starterBinIndex ] = curAnalPhase;

			// Straight array copy of the complex data from the analysis frame for the boundaries we worked out
			final int arrayCopyStart = (peakLowerBound * 2);
			final int arrayCopyEnd = (peakUpperBound * 2);
			final int lengthToCopy = arrayCopyEnd - arrayCopyStart;
			System.arraycopy( curAnalFrame.complexFrame[c], arrayCopyStart, outputFrame.complexFrame[c], arrayCopyStart, lengthToCopy );

			// Here's what the copy is actually doing....
//			outputFrame.complexFrame[c][ starterBinIndex * 2 ] = curAnalFrame.complexFrame[c][ starterBinIndex * 2 ];
//			outputFrame.complexFrame[c][ (starterBinIndex * 2) + 1 ] = curAnalFrame.complexFrame[c][ (starterBinIndex * 2) + 1 ];
//			// Now loop around going down and then up filling in the phases of the surrounding bins
//			// until we are not dealing with this peak any longer
//
//			for( int dd = starterBinIndex - 1; dd > 0 ; dd-- )
//			{
//				int curBinPeakNum = curAnalFrame.binToPeakBuffer[c][ dd ];
//				if( curBinPeakNum == starterBinIndex )
//				{
//					// Copy over the complex pair from the analysis
//					outputFrame.complexFrame[c][ (dd*2) ] = curAnalFrame.complexFrame[c][ (dd*2) ];
//					outputFrame.complexFrame[c][ (dd*2)+1 ] = curAnalFrame.complexFrame[c][ (dd*2)+1 ];
//				}
//				else
//				{
//					break;
//				}
//			}
//
//			for( int du = starterBinIndex + 1 ; du < lastBinIndex ; du++ )
//			{
//				int curBinPeakNum = curAnalFrame.binToPeakBuffer[c][ du ];
//				if( curBinPeakNum == starterBinIndex )
//				{
//					// Copy over the complex pair from the analysis
//					outputFrame.complexFrame[c][ (du*2) ] = curAnalFrame.complexFrame[c][ (du*2) ];
//					outputFrame.complexFrame[c][ (du*2)+1 ] = curAnalFrame.complexFrame[c][ (du*2)+1 ];
//				}
//				else
//				{
//					break;
//				}
//			}
		}
	}

	private void fillInBinCrossingPeaksPhasor( final PvocDataFrame curAnalFrame,
			final PvocDataFrame oldAnalFrame,
			final PvocDataFrame outputFrame,
			final int c,
			final int synthesisStepSize,
			final float twoPiSynthStepsOverSampleRate )
	{
		for( int b = 0 ; b < workingBuffer.numBinCrossingPeaks; b = b + 2 )
		{
			final int newPeakBinIndex = workingBuffer.outputBinCrossingPeaks[ b ];
			final int newPeakLowerBound = curAnalFrame.peakBoundariesBuffer[c][ (newPeakBinIndex*2) ];
			final int newPeakUpperBound = curAnalFrame.peakBoundariesBuffer[c][ (newPeakBinIndex*2)+1 ];

			final int prevPeakBinIndex = workingBuffer.outputBinCrossingPeaks[ b + 1 ];

			// Compute phase of new peak
			final float newPeakAnalPhase = complexPolarConverter.oneComplexToPolarPhaseOnly( curAnalFrame.complexFrame[c], newPeakBinIndex );
			curAnalFrame.phases[c][ newPeakBinIndex ] = newPeakAnalPhase;
			curAnalFrame.amps[c][ newPeakBinIndex ] = (float)Math.sqrt( curAnalFrame.ampsSquared[c][ newPeakBinIndex ] );

			final float newPeakFreq = phaseFrequencyConverter.crossBinPhaseToFreq( newPeakAnalPhase,
					oldAnalFrame.phases[c][ prevPeakBinIndex ],
					newPeakBinIndex,
					prevPeakBinIndex );

			final float newDanPeakPhase = phaseFrequencyConverter.crossBinFreqToPhase( twoPiSynthStepsOverSampleRate,
					previousSynthPhases[c][ prevPeakBinIndex ],
					newPeakFreq );


			if( PvocPeakFinder.DEBUG_PEAKS && log.isDebugEnabled() )
			{
				log.debug("So for bin " + prevPeakBinIndex + " old analysis phase is " + oldAnalFrame.phases[c][ prevPeakBinIndex ] +
						" and old synth phase is " + previousSynthPhases[c][ prevPeakBinIndex ] );
				log.debug("And for bin " + newPeakBinIndex + " analysis phase is " + curAnalFrame.phases[c][ newPeakBinIndex ] );
				log.debug("New Synth cross bin phase is " + newDanPeakPhase );
			}

			// Now re-create the complex data for this
			outputFrame.phases[c][ newPeakBinIndex ] = newDanPeakPhase;
			outputFrame.amps[c][ newPeakBinIndex ] = curAnalFrame.amps[c][ newPeakBinIndex ];

			complexPolarConverter.onePolarToComplex( outputFrame.amps[c],
					outputFrame.phases[c],
					outputFrame.complexFrame[c],
					newPeakBinIndex );

			// Convert the peak phase and amp back into complex form so we can compute the phasor from it.
			final float theta = computePhaseDiff( newDanPeakPhase, curAnalFrame.phases[c][ newPeakBinIndex ] );

			generatePhasor( theta );

//			log.debug( "Generated a phasor: " + phasorStorage[0] + " " + phasorStorage[1] );

			// Now loop up and down around the peak multiplying the anal complex vecs by this phasor

			for( int i = newPeakLowerBound ; i < newPeakBinIndex ; i++ )
			{
				complexMultiply( phasorStorage[0],
						phasorStorage[1],
						curAnalFrame.complexFrame[c][ (2*i) ],
						curAnalFrame.complexFrame[c][ (2*i) + 1 ],
						outputFrame.complexFrame[c],
						2*i );
			}

			for( int i = newPeakBinIndex + 1; i < newPeakUpperBound ; i++ )
			{
				complexMultiply( phasorStorage[0],
						phasorStorage[1],
						curAnalFrame.complexFrame[c][ (2*i) ],
						curAnalFrame.complexFrame[c][ (2*i) + 1 ],
						outputFrame.complexFrame[c],
						2*i );
			}
		}
	}

	private static final float computePhaseDiff( final float a, final float b )
	{
		final float rawDiff = a-b;
		boolean positive = a > b;
		float f = ( positive ? rawDiff : -rawDiff );
		if( f > MathDefines.ONE_PI_F )
		{
			f = MathDefines.TWO_PI_F - f;
			positive = !positive;
		}
		if( positive )
		{
			return f;
		}
		else
		{
			return -f;
		}
	}

	private final void generatePhasor(final float diffBetweenPeakAnalysisPhaseAndSynthesisPhase )
	{
		phasorStorage[0] = (float)(Math.cos( diffBetweenPeakAnalysisPhaseAndSynthesisPhase ));
		phasorStorage[1] = (float)(Math.sin( diffBetweenPeakAnalysisPhaseAndSynthesisPhase ));
	}

	private final static void complexMultiply(final float aReal, final float aImag, final float bReal, final float bImag, final float[] outputComplexFrame, final int outputOffset )
	{
		outputComplexFrame[ outputOffset ] = (aReal * bReal) - (aImag * bImag);
		outputComplexFrame[ outputOffset + 1 ] = (aReal * bImag) + (bReal * aImag);
//		log.debug("ComplexMultiply(" + aReal + ", " + aImag + "i)*(" + bReal + "," + bImag + "i)->(" + outputComplexFrame[ outputOffset ] + ", " + outputComplexFrame[ outputOffset + 1 ] + ")");
	}

}
