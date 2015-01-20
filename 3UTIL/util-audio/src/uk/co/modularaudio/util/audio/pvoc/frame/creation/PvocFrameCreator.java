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

package uk.co.modularaudio.util.audio.pvoc.frame.creation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.pvoc.PvocDebugger;
import uk.co.modularaudio.util.audio.pvoc.PvocFftComputer;
import uk.co.modularaudio.util.audio.pvoc.PvocParameters;
import uk.co.modularaudio.util.audio.pvoc.frame.PvocFrameRotatorPaddedTimeDomain;
import uk.co.modularaudio.util.audio.pvoc.support.PvocDataFrame;
import uk.co.modularaudio.util.audio.pvoc.support.PvocPlayPosition;

public strictfp class PvocFrameCreator
{
	public static Log log = LogFactory.getLog( PvocFrameCreator.class.getName() );
	
	// Useful values
	private int numChannels;
	private int windowLength;
	private int stepSize;
	private int fftSize;
	private int fftComplexArraySize;
	private PvocPlayPosition curPos;
	
	private FftWindow fftWindow;
	
	private PvocFrameRotatorPaddedTimeDomain frameRotator;
	
	private PvocFftComputer fftComputer;
	
	private float[] internalComplexBuffer;
	
	private UnsafeFloatRingBuffer[] cumulativeWindowRingBuffers;
	
	private PvocDebugger debugger;
	
	public PvocFrameCreator( PvocParameters params )
	{
		this.numChannels = params.getNumChannels();
		this.fftWindow = params.getFftWindow();
		this.windowLength = params.getWindowLength();
		this.stepSize = params.getStepSize();
		this.fftSize = params.getFftSize();
		this.fftComplexArraySize = params.getFftComplexArraySize();
		
		frameRotator = params.getFrameRotator();
		
		fftComputer = params.getFftComputer();
		
		debugger = params.getDebugger();

		cumulativeWindowRingBuffers = new UnsafeFloatRingBuffer[ numChannels ];
		for( int i = 0 ; i < numChannels ; i++ )
		{
			cumulativeWindowRingBuffers[i] = new UnsafeFloatRingBuffer( windowLength + stepSize );
		}
		
		internalComplexBuffer = new float[ fftComplexArraySize ];

		reset();
	}
	
	public int makeFrameFromNextStep( float[][] inputStep,
			PvocDataFrame frameToFill )
	{
		if( debugger != null )
		{
			debugger.receiveCreatorPosition( curPos );
		}
		
		for( int i = 0 ; i < numChannels ; i++ )
		{
			float[] complexFrame = frameToFill.complexFrame[i];

			cumulativeWindowRingBuffers[i].write( inputStep[i], 0, stepSize );
			
			// Now read out (and move) stepSize, then read out windowLength - stepSize without moving
			int stepRead = cumulativeWindowRingBuffers[i].read( complexFrame, 0, stepSize );
			assert( stepRead == stepSize );
			int restWindowRead = cumulativeWindowRingBuffers[i].readNoMove( complexFrame, stepSize, windowLength - stepSize );
			assert( restWindowRead == windowLength - stepSize );
			
			if( debugger != null )
			{
				debugger.receiveCreatorInputSegment( complexFrame, windowLength );
			}
			
			fftWindow.apply( complexFrame );
			
			frameRotator.inRotate( complexFrame, internalComplexBuffer );
			
			if( debugger != null )
			{
				debugger.receiveCreatorWindowedRotatedSegment( internalComplexBuffer, fftSize );
			}

			// The FFT is really costly when values are really small or zero
			// we check first here and skip the FFT if it isn't necessary
//			boolean allZeros = true;
//			for( int s = 0 ; s < windowLength ; s++ )
//			{
//				if( internalComplexBuffer[s] != 0.0f )
//				{
//					allZeros = false;
//					break;
//				}
//			}
//			
//			if( allZeros )
//			{
//				Arrays.fill( internalComplexBuffer, 0.0f );
//			}
//			else
//			{
				fftComputer.realForward( internalComplexBuffer );
//			}
			
			// Make the complex frame available to the frame processor (for phase unwrapping)
			System.arraycopy( internalComplexBuffer, 0, complexFrame, 0, fftComplexArraySize );
		}
		
		// Move us along.
		curPos.pos += stepSize;
		
		return 0;
	}

	public void reset()
	{
		curPos = new PvocPlayPosition();
		
		for( int i = 0 ; i < numChannels ; i++ )
		{
			cumulativeWindowRingBuffers[i].clear();
			// Now we need to prime the window ring buffer
			float[] primer = new float[ windowLength - stepSize ];
			cumulativeWindowRingBuffers[i].write( primer, 0, primer.length );
		}
	}
}
