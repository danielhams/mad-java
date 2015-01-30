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

package uk.co.modularaudio.util.audio.stft.frame.synthesis;

import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.StftPlayPosition;
import uk.co.modularaudio.util.audio.stft.tools.FrameRotatorPaddedTimeDomain;
import uk.co.modularaudio.util.audio.stft.tools.OverlapAdder;

public class StftFrameSynthesiser
{
//	private static Log log = LogFactory.getLog( StftFrameSynthesiser.class.getName() );

//	private StftParameters params = null;

	private final int numChannels;
	private final int windowLength;
	private final int numReals;
	private final int complexArraySize;
	private final int numBins;
	private final int analysisStepSize;
	private int lastOutputStepSize;
	private final int numOverlaps;

	private StftFrameSynthesiserBuffers buffers;

	private StftPlayPosition outputPosition;

	private final FftWindow fftWindow;
	private final FloatFFT_1D fftComputer;

	private final FrameRotatorPaddedTimeDomain frameRotator;

	private final OverlapAdder[] overlapAdders;
	private final float[] resampleArray;

	private final StftStreamResampler[] linearResamplers;
	private final StftStreamResampler[] cubicResamplers;

	private float fftWindowValSquaredSum;

	public StftFrameSynthesiser( final StftParameters params )
	{
//		this.params = params;
		numChannels = params.getNumChannels();
		windowLength = params.getWindowLength();
		numReals = params.getNumReals();
		complexArraySize = params.getComplexArraySize();
		analysisStepSize = params.getStepSize();
		numBins = params.getNumBins();
		numOverlaps = params.getNumOverlaps();
		setupBuffers();

		fftWindow = params.getFftWindow();
		final float[] fftWindowAmps = fftWindow.getAmps();
		final int fftWindowLength = fftWindowAmps.length;
		for( int i = 0 ; i < fftWindowLength ; i++ )
		{
			final float amp = fftWindowAmps[ i ];
			fftWindowValSquaredSum += (amp * amp);
		}

		fftComputer = new FloatFFT_1D( numReals );

		frameRotator = new FrameRotatorPaddedTimeDomain( params );

		overlapAdders = new OverlapAdder[ numChannels ];
		linearResamplers = new StftStreamResampler[ numChannels ];
		cubicResamplers = new StftStreamResampler[ numChannels ];
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			overlapAdders[chan] = new OverlapAdder( analysisStepSize, numOverlaps );
			linearResamplers[chan] = new StftStreamingLinearInterpolationResampler();
			cubicResamplers[chan] = new StftStreamingCubicInterpolationResampler();
		}

		resampleArray = new float[ analysisStepSize * 10 ];

		reset();
	}

	private final void setupBuffers()
	{
		buffers = new StftFrameSynthesiserBuffers( numChannels, windowLength, numReals, complexArraySize, numBins, analysisStepSize);
	}

	public void synthesiseFrame( final StftDataFrame processedFrame,
			final double speed,
			final double pitch,
			final UnsafeFloatRingBuffer[] outputRingBuffers,
			final StftFrameSynthesisStep synthStep )
	{
		// Work out how much we move by
		final double synthesisStepSize = synthStep.getSynthesisStepSize();
		final double synthesisFrac = synthStep.getFrac();
		final int synthesisRoundedStepSize = synthStep.getRoundedStepSize();
		final double resampleRatio = synthStep.getInternalPitchRatio();

		final long posOfFrame = processedFrame.position;

		for( int chan = 0 ; chan < numChannels ; chan++ )
		{

			// Now do the processing
			// First we copy the incoming frame amps and freqs into our internal ones
			System.arraycopy( processedFrame.amps[chan], 0, buffers.ampsBuffer[chan], 0, numBins );

			// Freqs shouldn't be produced by a frame processor, only phases
			System.arraycopy( processedFrame.freqs[chan], 0, buffers.freqsBuffer[chan], 0, numBins );
			System.arraycopy( processedFrame.phases[chan], 0, buffers.phasesBuffer[chan], 0, numBins );

			// Copy out the fft data so we don't destroy it.
			// This isn't strictly necessary but may make debugging easier
			System.arraycopy( processedFrame.complexFrame[chan], 0, buffers.fftBuffer[chan], 0, complexArraySize );

			inverseFftOneChannel( buffers.fftBuffer[chan] );

			unrotateWindowForAlignmentOneChannel( buffers.fftBuffer[chan], buffers.outputWindowBuffer[chan], outputPosition, posOfFrame );

			applyOutputWindowingOneChannel( buffers.outputWindowBuffer[ chan ]);

			overlapAddToOutputRingForChannel( buffers.outputWindowBuffer[chan],
					overlapAdders[chan],
					synthesisStepSize,
					synthesisRoundedStepSize );

			lastOutputStepSize = synthesisRoundedStepSize;

			removeSynthesisSamplesForChannel( overlapAdders[chan],
					synthesisRoundedStepSize,
					buffers.outputStepBuffer[chan] );

			// Resample and push into output ring
			resampleOutputSamples( chan,
					buffers.outputStepBuffer[ chan ],
					synthesisRoundedStepSize,
					resampleArray,
					(float)resampleRatio,
					outputRingBuffers[ chan ] );

		}

		// Update the position
		outputPosition.pos += synthesisRoundedStepSize;
		outputPosition.frac += synthesisFrac;
	}

	private void inverseFftOneChannel( final float[] bufferToFft )
	{
		fftComputer.realInverse( bufferToFft, true );
	}

	private void unrotateWindowForAlignmentOneChannel( final float[] inFftBuffer, final float[] outputWindow, final StftPlayPosition outputWavePosition, final long posOfFrame )
	{
		frameRotator.outRotate( inFftBuffer, (int)posOfFrame , outputWindow );
	}

	private void applyOutputWindowingOneChannel( final float[] outputWindowBuffer )
	{
		fftWindow.apply( outputWindowBuffer );
	}

	private void overlapAddToOutputRingForChannel( final float[] outputWindowBuffer,
			final OverlapAdder overlapAdderForChan,
			final double synthesisStepSize,
			final int synthesisRoundedStepSize)
	{
		final float gain = synthesisRoundedStepSize / fftWindowValSquaredSum;

		for( int i = 0 ; i < outputWindowBuffer.length ; i++ )
		{
			outputWindowBuffer[i] *= gain;
		}
		overlapAdderForChan.addOverlap( outputWindowBuffer, synthesisRoundedStepSize, windowLength );
	}

	private int resampleOutputSamples( final int chan,
			final float[] inputSamples,
			final int numInputSamples,
			final float[] outputSamples,
			final float resampleRatio,
			final UnsafeFloatRingBuffer outputRingBuffer )
	{
		final boolean cubic = true;
		int numSamples;
		if( cubic )
		{
			numSamples = cubicResamplers[ chan ].streamResample( resampleRatio, inputSamples, numInputSamples, outputSamples );
		}
		else
		{
			numSamples = linearResamplers[ chan ].streamResample( resampleRatio, inputSamples, numInputSamples, outputSamples );
		}
		final int numWritten = outputRingBuffer.write( outputSamples, 0, numSamples );
		if( numWritten != numSamples )
		{
			// Failed to write into ring buffer
			return -1;
		}
		return numSamples;
	}

	private void removeSynthesisSamplesForChannel( final OverlapAdder overlapAdder,
			final int numSamples,
			final float[] outputStepBuffer )
	{
		overlapAdder.readOutput( outputStepBuffer, numSamples );
	}

	public int getLastOutputStepSize()
	{
		return lastOutputStepSize;
	}

	public final void reset()
	{
		outputPosition = new StftPlayPosition();
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			overlapAdders[chan].reset();
		}
	}

	public float[][] getSynthAmpChannelBuffers()
	{
		return buffers.ampsBuffer;
	}

	public float[][] getSynthPhaseChannelBuffers()
	{
		return buffers.phasesBuffer;
	}

	public float[][] getSynthFreqChannelBuffers()
	{
		return buffers.freqsBuffer;
	}

	public float[][] getSynthFftBuffers()
	{
		return buffers.fftBuffer;
	}

	public float[][] getSynthOutputWindowBuffers()
	{
		return buffers.outputWindowBuffer;
	}

	public OverlapAdder[] getOutputRingBuffers()
	{
		return overlapAdders;
	}

}
