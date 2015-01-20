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
	
	private int numChannels = -1;
	private int windowLength = -1;
	private int numReals = -1;
	private int complexArraySize = -1;
	private int numBins = -1;
	private int analysisStepSize = -1;
	private int lastOutputStepSize = -1;
	private int numOverlaps = -1;
	
	private StftFrameSynthesiserBuffers buffers = null;
	
	private StftPlayPosition outputPosition = null;
	
	private FftWindow fftWindow = null;
	private FloatFFT_1D fftComputer = null;
	
	private FrameRotatorPaddedTimeDomain frameRotator = null;
	
	private OverlapAdder[] overlapAdders = null;
	private float[] resampleArray = null;

	private StftStreamResampler[] linearResamplers = null;
	private StftStreamResampler[] cubicResamplers = null;
	
	private float fftWindowValSquaredSum = 0.0f;
	
	public StftFrameSynthesiser( StftParameters params )
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
		float[] fftWindowAmps = fftWindow.getAmps();
		int fftWindowLength = fftWindowAmps.length;
		for( int i = 0 ; i < fftWindowLength ; i++ )
		{
			float amp = fftWindowAmps[ i ];
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
	
	private void setupBuffers()
	{
		buffers = new StftFrameSynthesiserBuffers( numChannels, windowLength, numReals, complexArraySize, numBins, analysisStepSize);
	}

	public void synthesiseFrame( StftDataFrame processedFrame,
			double speed,
			double pitch,
			UnsafeFloatRingBuffer[] outputRingBuffers,
			StftFrameSynthesisStep synthStep )
	{
		// Work out how much we move by
		double synthesisStepSize = synthStep.getSynthesisStepSize();
		double synthesisFrac = synthStep.getFrac();
		int synthesisRoundedStepSize = synthStep.getRoundedStepSize();
		double resampleRatio = synthStep.getInternalPitchRatio();
		
		long posOfFrame = processedFrame.position;
		
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

	private void inverseFftOneChannel( float[] bufferToFft )
	{
		fftComputer.realInverse( bufferToFft, true );
	}

	private void unrotateWindowForAlignmentOneChannel( float[] inFftBuffer, float[] outputWindow, StftPlayPosition outputWavePosition, long posOfFrame )
	{
		frameRotator.outRotate( inFftBuffer, (int)posOfFrame , outputWindow );
	}

	private void applyOutputWindowingOneChannel( float[] outputWindowBuffer )
	{
		fftWindow.apply( outputWindowBuffer );
	}

	private void overlapAddToOutputRingForChannel( float[] outputWindowBuffer,
			OverlapAdder overlapAdderForChan,
			double synthesisStepSize,
			int synthesisRoundedStepSize)
	{		
		float gain = synthesisRoundedStepSize / (float)fftWindowValSquaredSum;

		for( int i = 0 ; i < outputWindowBuffer.length ; i++ )
		{
			outputWindowBuffer[i] *= gain;
		}
		overlapAdderForChan.addOverlap( outputWindowBuffer, synthesisRoundedStepSize, windowLength );
	}
	
	private int resampleOutputSamples( int chan,
			float[] inputSamples,
			int numInputSamples,
			float[] outputSamples,
			float resampleRatio,
			UnsafeFloatRingBuffer outputRingBuffer )
	{
		boolean cubic = true;
		int numSamples;
		if( cubic )
		{
			numSamples = cubicResamplers[ chan ].streamResample( resampleRatio, inputSamples, numInputSamples, outputSamples );
		}
		else
		{
			numSamples = linearResamplers[ chan ].streamResample( resampleRatio, inputSamples, numInputSamples, outputSamples );
		}
		int numWritten = outputRingBuffer.write( outputSamples, 0, numSamples );
		if( numWritten != numSamples )
		{
			// Failed to write into ring buffer
			return -1;
		}
		return numSamples;
	}

	private void removeSynthesisSamplesForChannel( OverlapAdder overlapAdder,
			int numSamples,
			float[] outputStepBuffer )
	{
		overlapAdder.readOutput( outputStepBuffer, numSamples );
	}

	public int getLastOutputStepSize()
	{
		return lastOutputStepSize;
	}

	public void reset()
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
