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

package uk.co.modularaudio.util.audio.pvoc;

import java.nio.BufferUnderflowException;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.pvoc.frame.PvocFrameProcessor;

public class StreamingPhaseVocoder
{
//	private static Log log = LogFactory.getLog( StreamingWolaProcessor.class.getName() );
	
//	private LocklessAudioRingBuffer[] inputRingBuffers = null;
//	private LocklessAudioRingBuffer[] outputRingBuffers = null;
	private UnsafeFloatRingBuffer[] inputRingBuffers = null;
	private UnsafeFloatRingBuffer[] outputRingBuffers = null;
	
	private PhaseVocoder pvoc = null;
	
	private int numChannels = -1;
	
	// Useful parameters 
	private int windowLength = -1;
	private int stepSize = -1;
	
	// Place to store a step
	private float[][] inputStepArray = new float[1][];
	
	public StreamingPhaseVocoder( PvocParameters params, PvocFrameProcessor frameProcessor )
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
		pvoc = new PhaseVocoder( params, frameProcessor );
		
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
	
	public void write( float[][] inputData, int offset, int length, double speed, double pitch )
		throws InterruptedException
	{
		int numWritten = 0;
		while( numWritten < length )
		{
			int numCanWriteInRing = Math.min(inputRingBuffers[0].getNumWriteable(), length );
			for( int chan = 0 ; chan < numChannels ; chan++ )
			{
				inputRingBuffers[chan].write( inputData[chan], numWritten + offset, numCanWriteInRing );
			}
			numWritten += numCanWriteInRing;
			
			while( inputRingBuffers[0].getNumReadable() >= stepSize )
			{
				for( int chan = 0 ; chan < numChannels ; chan++ )
				{
					inputRingBuffers[chan].read( inputStepArray[chan], 0, stepSize );
					for( int s = 0 ; s < stepSize ; s++ )
					{
						if( inputStepArray[ chan ][ s ] != 0.0f && Math.abs(inputStepArray[ chan ][ s ]) < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
						{
							inputStepArray[ chan ][ s ] = 0.0f;
						}
					}
				}
//				log.debug("Doing a next step");
				pvoc.doNextStep( inputStepArray, speed, pitch, outputRingBuffers );
			}
		}
	}
	
	public int getNumReadable()
	{
		return outputRingBuffers[0].getNumReadable();
	}
	
	public void read( float[][] outputData, int offset, int length ) throws BufferUnderflowException, InterruptedException
	{
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			outputRingBuffers[chan].read( outputData[chan], offset, length );
		}
	}

	public PhaseVocoder getPhaseVocoder()
	{
		return pvoc;
	}

	public void reset()
	{
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			inputRingBuffers[chan].clear();
			outputRingBuffers[chan].clear();
		}
		pvoc.reset();		
	}

	public int getNumFramesInputLatency()
	{
		return stepSize;
	}
	
}
