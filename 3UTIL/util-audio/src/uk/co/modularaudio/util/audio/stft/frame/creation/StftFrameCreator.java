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

package uk.co.modularaudio.util.audio.stft.frame.creation;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtransforms.fft.FloatFFT_1D;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.fft.FftWindow;
import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftException;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.StftPlayPosition;
import uk.co.modularaudio.util.audio.stft.tools.FrameRotatorPaddedTimeDomain;
import uk.co.modularaudio.util.math.MathDefines;

public class StftFrameCreator
{
	public static Log log = LogFactory.getLog( StftFrameCreator.class.getName() );

	private StftParameters params = null;

	// Useful values
	private int numChannels = -1;
	private int windowLength = -1;
	private int stepSize = -1;
	private int numReals = -1;
	private int complexArraySize = -1;

	private float inputScal = 0.0f;
	private float inputFac = 0.0f;
	private int sampleRate = 0;

	private StftPlayPosition curPos = null;

	private FftWindow fftWindow = null;

	private StftFrameCreatorBuffers buffers = null;

	private FrameRotatorPaddedTimeDomain frameRotator = null;

	private FloatFFT_1D fftComputer = null;

	private float[] internalComplexBuffer = null;

	private UnsafeFloatRingBuffer[] cumulativeWindowRingBuffer = null;

	private StftDataFrame lastAnalFrame = null;

	public StftFrameCreator( final StftParameters params )
	{
		this.params = params;
		this.numChannels = params.getNumChannels();
		this.fftWindow = params.getFftWindow();
		this.windowLength = params.getWindowLength();
		this.stepSize = params.getStepSize();
		this.numReals = params.getNumReals();
		this.complexArraySize = params.getComplexArraySize();

		sampleRate = params.getSampleRate();
		inputFac = (sampleRate / (stepSize * MathDefines.TWO_PI_F));
		inputScal = (MathDefines.TWO_PI_F * stepSize / numReals );

		frameRotator = new FrameRotatorPaddedTimeDomain( params );

		fftComputer = new FloatFFT_1D( numReals );

		cumulativeWindowRingBuffer = new UnsafeFloatRingBuffer[ numChannels ];
		for( int i = 0 ; i < numChannels ; i++ )
		{
			cumulativeWindowRingBuffer[i] = new UnsafeFloatRingBuffer( windowLength + stepSize );
		}

		internalComplexBuffer = new float[ complexArraySize ];

		setupBuffers();

		reset();
	}

	private final void setupBuffers()
	{
		buffers = new StftFrameCreatorBuffers();
		buffers.selectedWindowBuffer = new float[ windowLength ];
		buffers.weightedWindowBuffer = new float[ complexArraySize ];
	}

	public StftDataFrame makeFrameFromNextStep( final float[][] inputStep, final StftDataFrame latestFrame )
	{
		StftDataFrame retVal = null;
		try
		{
			retVal = latestFrame;
			retVal.position =  curPos.pos;
			for( int i = 0 ; i < numChannels ; i++ )
			{
				final float[] complexFrame = retVal.complexFrame[i];

				// These will be filled in by the frame processor itself
				retVal.inputScal = inputScal;
				retVal.inputFac = inputFac;

				cumulativeWindowRingBuffer[i].write( inputStep[i], 0, stepSize );

				// Now read out (and move) stepSize, then read out windowLength - stepSize without moving
				cumulativeWindowRingBuffer[i].read( buffers.selectedWindowBuffer, 0, stepSize );
				cumulativeWindowRingBuffer[i].readNoMove( buffers.selectedWindowBuffer, stepSize, windowLength - stepSize );

				applyWindowing();

				rotateWindowForAlignment( (int)curPos.pos );

				// The FFT is really costly when values are really small or zero
				// we check first here and skip the FFT if it isn't necessary
				boolean allZeros = true;
				for( int s = 0 ; s < windowLength ; s++ )
				{
					if( buffers.weightedWindowBuffer[s] != 0.0f )
					{
						allZeros = false;
						break;
					}
				}

				if( allZeros )
				{
					Arrays.fill( internalComplexBuffer, 0.0f );
//					log.debug("Filling with zeros");
				}
				else
				{
					forwardFft();
				}

				// Make the complex frame available to the frame processor (for phase unwrapping)
				System.arraycopy( internalComplexBuffer, 0, complexFrame, 0, complexArraySize );
			}

			// Move us along.
			curPos.pos += stepSize;

			lastAnalFrame = retVal;
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught building frame in PvFrameCreator: " + e.toString();
			log.error( msg );
			return null;
		}

		return retVal;
	}

	private void applyWindowing()
		throws StftException
	{
		final float[] selectedWindowBuffer = buffers.selectedWindowBuffer;
		final float[] outputWindowBuffer = buffers.weightedWindowBuffer;
		final int windowLength = params.getWindowLength();
		System.arraycopy( selectedWindowBuffer, 0, outputWindowBuffer, 0, windowLength );
		fftWindow.apply( outputWindowBuffer );
	}

	private void rotateWindowForAlignment( final int curPosition )
			throws StftException
	{
		final float[] inWindow = buffers.weightedWindowBuffer;
		final float[] fftBuffer = internalComplexBuffer;
//			log.debug("InRotating window at position " + curPosition);
		frameRotator.inRotate( inWindow, curPosition, fftBuffer );

		// Copy it back
		System.arraycopy( fftBuffer, 0, inWindow, 0, complexArraySize );
	}

	private void forwardFft()
	{
		fftComputer.realForward( internalComplexBuffer );
	}

	public float[] getSelectedWindowBuffer()
	{
		return buffers.selectedWindowBuffer;
	}

	public float[] getWeightedWindowBuffer()
	{
		return buffers.weightedWindowBuffer;
	}

	public StftDataFrame getLastAnalFrame()
	{
		return lastAnalFrame;
	}

	public final void reset()
	{
		curPos = new StftPlayPosition();

		for( int i = 0 ; i < numChannels ; i++ )
		{
			cumulativeWindowRingBuffer[i].clear();
			// Now we need to prime the window ring buffer
			final float[] primer = new float[ windowLength - stepSize ];
			cumulativeWindowRingBuffer[i].write( primer, 0, primer.length );
		}
	}
}
