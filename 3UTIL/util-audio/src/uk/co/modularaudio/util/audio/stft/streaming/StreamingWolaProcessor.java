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

package uk.co.modularaudio.util.audio.stft.streaming;

import java.nio.BufferUnderflowException;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.stft.StftException;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.WolaProcessor;
import uk.co.modularaudio.util.audio.stft.frame.processing.StftFrameProcessor;

public class StreamingWolaProcessor
{
//	private static Log log = LogFactory.getLog( StreamingWolaProcessor.class.getName() );

	private final UnsafeFloatRingBuffer[] inputRingBuffers;
	private final UnsafeFloatRingBuffer[] outputRingBuffers;

	private final WolaProcessor wolaProcessor;

//	private StftParameters params;
	private final int numChannels;

	// Useful parameters
	private final int windowLength;
	private final int stepSize;

	// Place to store a step
	private float[][] inputStepArray = new float[1][];

	public StreamingWolaProcessor( final StftParameters params, final StftFrameProcessor frameProcessor )
			throws StftException
	{
//		this.params = params;
		this.numChannels = params.getNumChannels();
		stepSize = params.getStepSize();
		inputStepArray = new float[ numChannels ][];
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			inputStepArray[chan] = new float[ stepSize ];
		}
		windowLength = params.getWindowLength();

		// We instantiate the phase vocoder, get it to tell us how big the frames it likes are
		// and buffer using the input ring buffer until we have enough to do a "step"
		// Once a step is done, we start appending the output into the outputRingBuffer
		// (if there is any)
		wolaProcessor = new WolaProcessor( params, frameProcessor );

		inputRingBuffers = new UnsafeFloatRingBuffer[ numChannels ];
		outputRingBuffers = new UnsafeFloatRingBuffer[ numChannels ];
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			// Make sure the input ring buffer is long enough to contain 2 times the input window length
			inputRingBuffers[chan] = new UnsafeFloatRingBuffer( windowLength * 2 );

			// Make sure the output ring buffer is at least ten times the standard window length
			// since when really slowed down lots more samples get generated than are read in
			outputRingBuffers[chan] = new UnsafeFloatRingBuffer( windowLength * 10 );
		}
	}

	public void write( final float[][] inputData, final int offset, final int length, final double speed, final double pitch )
	{
		int numWritten = 0;
		int numLeft = length;
		while( numLeft > 0 )
		{
			final int numCanWriteInRing = Math.min(inputRingBuffers[0].getNumWriteable(), numLeft );
//			log.debug("Pushing " + numCanWriteInRing + " into wola input ring buffers");

			for( int chan = 0 ; chan < numChannels ; chan++ )
			{
				inputRingBuffers[chan].write( inputData[chan], numWritten + offset, numCanWriteInRing );
			}
			numWritten += numCanWriteInRing;
			numLeft -= numCanWriteInRing;

			int numInInput = inputRingBuffers[0].getNumReadable();

			while( numInInput >= stepSize )
			{
				for( int chan = 0 ; chan < numChannels ; chan++ )
				{
					inputRingBuffers[chan].read( inputStepArray[chan], 0, stepSize );
//					for( int s = 0 ; s < stepSize ; s++ )
//					{
//						if( inputStepArray[ chan ][ s ] != 0.0f && Math.abs(inputStepArray[ chan ][ s ]) < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
//						{
//							inputStepArray[ chan ][ s ] = 0.0f;
//						}
//					}
				}
//				log.debug("Doing a next step");
				wolaProcessor.doNextStep( inputStepArray, speed, pitch, outputRingBuffers );
				numInInput -= stepSize;
			}
		}
	}

	public int getNumReadable()
	{
		return outputRingBuffers[0].getNumReadable();
	}

	public void read( final float[][] outputData, final int offset, final int length ) throws BufferUnderflowException, InterruptedException
	{
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			outputRingBuffers[chan].read( outputData[chan], offset, length );
		}
	}

	public WolaProcessor getWolaProcessor()
	{
		return wolaProcessor;
	}

	public void reset()
	{
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			inputRingBuffers[chan].clear();
			outputRingBuffers[chan].clear();
		}
		wolaProcessor.reset();
	}

	public StftFrameProcessor getFrameProcessor()
	{
		return wolaProcessor.getFrameProcessor();
	}

	public int getNumFramesInputLatency()
	{
		return stepSize;
	}

}
