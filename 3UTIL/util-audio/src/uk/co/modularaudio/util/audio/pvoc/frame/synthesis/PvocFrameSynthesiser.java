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

package uk.co.modularaudio.util.audio.pvoc.frame.synthesis;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.pvoc.PvocDebugger;
import uk.co.modularaudio.util.audio.pvoc.PvocFftComputer;
import uk.co.modularaudio.util.audio.pvoc.PvocParameters;
import uk.co.modularaudio.util.audio.pvoc.frame.PvocFrameRotatorPaddedTimeDomain;
import uk.co.modularaudio.util.audio.pvoc.support.PvocDataFrame;
import uk.co.modularaudio.util.audio.pvoc.support.PvocFrameSynthesisStep;
import uk.co.modularaudio.util.audio.pvoc.support.PvocPlayPosition;
import uk.co.modularaudio.util.audio.stft.tools.OverlapAdder;

public strictfp class PvocFrameSynthesiser
{
//	private static Log log = LogFactory.getLog( PvocFrameSynthesiser.class.getName() );

	private final int numChannels;
	private final int windowLength;
	private final int fftSize;
	private final int fftComplexArraySize;
	private final int analysisStepSize;
	private final int numOverlaps;

	private PvocPlayPosition outputPosition;

	private final FftWindow fftWindow;
	private final PvocFftComputer fftComputer;

	private final float[] internalComplexArray;

	private final PvocFrameRotatorPaddedTimeDomain frameRotator;

	private final OverlapAdder[] overlapAdders;
	private final float[] resampleArray;

	private final PvocStreamResampler[] resamplers;

	private float fftWindowValSquaredSum;

	private final PvocDebugger debugger;

	public PvocFrameSynthesiser( final PvocParameters params )
	{
		numChannels = params.getNumChannels();
		windowLength = params.getWindowLength();
		fftSize = params.getFftSize();
		fftComplexArraySize = params.getFftComplexArraySize();
		analysisStepSize = params.getStepSize();
		numOverlaps = params.getNumOverlaps();

		internalComplexArray = new float[ fftComplexArraySize ];

		fftWindow = params.getFftWindow();
		final float[] fftWindowAmps = fftWindow.getAmps();
		final int fftWindowLength = fftWindowAmps.length;
		for( int i = 0 ; i < fftWindowLength ; i++ )
		{
			final float amp = fftWindowAmps[ i ];
			fftWindowValSquaredSum += (amp * amp);
		}

		fftComputer = params.getFftComputer();

		frameRotator = params.getFrameRotator();

		overlapAdders = new OverlapAdder[ numChannels ];
		resamplers = new PvocStreamResampler[ numChannels ];
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			overlapAdders[chan] = new OverlapAdder( analysisStepSize, numOverlaps );
//			resamplers[chan] = new PvocStreamingCubicInterpolationResampler();
			resamplers[chan] = new PvocStreamingLinearInterpolationResampler();
		}

		resampleArray = new float[ analysisStepSize * 10 ];
		debugger = params.getDebugger();

		reset();
	}

	public int synthesiseFrame( final PvocDataFrame processedFrame,
			final double speed,
			final double pitch,
			final UnsafeFloatRingBuffer[] outputRingBuffers,
			final PvocFrameSynthesisStep synthStep )
	{
		// Work out how much we move by
		final double synthesisFrac = synthStep.getFrac();
		final int synthesisRoundedStepSize = synthStep.getRoundedStepSize();
		final double resampleRatio = synthStep.getInternalPitchRatio();

		final float outputStepGain = synthesisRoundedStepSize / fftWindowValSquaredSum;

		for( int chan = 0 ; chan < numChannels ; chan++ )
		{

			// Now do the processing
			System.arraycopy( processedFrame.complexFrame[chan], 0, internalComplexArray, 0, fftComplexArraySize );

			fftComputer.realInverse( internalComplexArray );

			if( debugger != null )
			{
				debugger.receiveProcessedWindowedRotatedSegment( internalComplexArray, fftSize );
			}

			frameRotator.outRotate( internalComplexArray, processedFrame.complexFrame[chan] );

			fftWindow.applyWithGain( processedFrame.complexFrame[chan], outputStepGain );

			if( debugger != null )
			{
				debugger.receiveProcessedOutputSegment( processedFrame.complexFrame[chan], windowLength );
			}

			overlapAdders[chan].addOverlap( processedFrame.complexFrame[chan], synthesisRoundedStepSize, windowLength );

			if( debugger != null )
			{
				debugger.receiveSynthesiserRingContents( overlapAdders[chan].getRingBuffer(), overlapAdders[chan].getRingBuffer().length );
			}

			overlapAdders[chan].readOutput( internalComplexArray, synthesisRoundedStepSize );

			// Resample and push into output ring
			resampleOutputSamples( chan,
					synthesisRoundedStepSize,
					resampleArray,
					(float)resampleRatio,
					outputRingBuffers[ chan ] );

		}

		// Update the position
		outputPosition.pos += synthesisRoundedStepSize;
		outputPosition.frac += synthesisFrac;

		return 0;
	}

	private int resampleOutputSamples( final int chan,
			final int numInputSamples,
			final float[] outputSamples,
			final float resampleRatio,
			final UnsafeFloatRingBuffer outputRingBuffer )
	{
		final int numSamples = resamplers[ chan ].streamResample( resampleRatio, internalComplexArray, numInputSamples, outputSamples );
		final int numWritten = outputRingBuffer.write( outputSamples, 0, numSamples );
		if( numWritten != numSamples )
		{
			// Failed to write into ring buffer
			return -1;
		}
		return numSamples;
	}

	public final void reset()
	{
		outputPosition = new PvocPlayPosition();
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			overlapAdders[chan].reset();
		}
	}
}
