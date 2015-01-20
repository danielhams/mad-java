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

package uk.co.modularaudio.mads.base.audioanalyser.ui;

import java.util.Arrays;

import uk.co.modularaudio.mads.base.audioanalyser.ui.modeprocessors.ModeProcessor;
import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;

public class AudioAnalyserDataBuffers extends UnsafeFloatRingBuffer
{
//	private static Log log = LogFactory.getLog( AudioAnalyserDataBuffers.class.getName() );

	public int sampleRate;
	
	public AdditionalDataBuffers additionalDataBuffers = null;
	
	public float[] additionalFloatBuffer1;
	public float[] additionalFloatBuffer2;
	public float[] additionalFloatBuffer3;
	public float[] additionalFloatBuffer4;
	public float[] additionalFloatBuffer5;
	public float[] additionalFloatBuffer6;
	
	private AudioAnalyserDisplayMode displayMode = AudioAnalyserDisplayMode.RAW_WAVE;
	private ModeProcessor displayModeProcessor = displayMode.getModeProcessor();
	
	public AudioAnalyserDataBuffers( int sampleRate, int dataBufferLength, boolean shouldFillWithZeros)
	{
		super( dataBufferLength, shouldFillWithZeros );
		this.sampleRate = sampleRate;
		
		additionalFloatBuffer1 = new float[ dataBufferLength ];
		additionalFloatBuffer2 = new float[ dataBufferLength ];
		additionalFloatBuffer3 = new float[ dataBufferLength ];
		additionalFloatBuffer4 = new float[ dataBufferLength ];
		additionalFloatBuffer5 = new float[ dataBufferLength ];
		additionalFloatBuffer6 = new float[ dataBufferLength ];

		setMode( AudioAnalyserDisplayMode.RAW_WAVE );
	}
	
	public void setAdditionalDataBuffers( AdditionalDataBuffers nadb )
	{
		this.additionalDataBuffers = nadb;
		if( additionalDataBuffers != null )
		{
			int numBefore = ( readPosition > writePosition ? bufferLength - readPosition : writePosition - readPosition );
			int numAfter = capacity - numBefore;
//			log.debug("AdditionalBuffers calculated populate from RP(" + readPosition + ") WP(" + writePosition + ") BL(" + bufferLength + ")");
//			log.debug("NB(" + numBefore + ") NA(" + numAfter + ")");
			
			additionalDataBuffers.write( buffer, readPosition, numBefore );
			additionalDataBuffers.write( buffer, 0, numAfter );
		}
	}
	
	@Override
	public int write(float[] source, int pos, int length)
	{
		int lrp = writePosition;
		int retVal = super.write(source, pos, length);
		
		if( additionalDataBuffers != null )
		{
			// And filter and rms it
			int numBefore = ( lrp + length < bufferLength ? length : bufferLength - lrp );
			int numAfter = length - numBefore;
			
			additionalDataBuffers.write( buffer, lrp, numBefore );
			if( numAfter > 0 )
			{
				additionalDataBuffers.write( buffer, 0, numAfter );
			}
			
			displayModeProcessor.deltaProcess( this, lrp, numBefore );
			if( numAfter > 0 )
			{
				displayModeProcessor.deltaProcess( this, 0, numAfter );
			}
		}
		
		return retVal;
	}

	public void resetIfNeeded( int sampleRate, int capacity, boolean fillWithZeros)
	{
		boolean needsReset = false;
		if( this.sampleRate != sampleRate )
		{
			needsReset = true;
		}
		if( this.capacity != capacity )
		{
			needsReset = true;
		}
		if( fillWithZeros )
		{
			needsReset = true;
		}
		
		if( needsReset )
		{
			if( this.sampleRate != sampleRate )
			{
				this.sampleRate = sampleRate;
			}

			if( this.capacity != capacity )
			{
				this.capacity = capacity;
				this.bufferLength = capacity + 1;
				buffer = new float[ bufferLength ];
				readPosition = 0;
			}

			if( fillWithZeros )
			{
				Arrays.fill( buffer,  0.0f );
				writePosition = bufferLength - 1;
			}
			else
			{
				writePosition = 0;
			}
			
			displayModeProcessor.fullProcess( this );
		}
	}

	public void setMode( AudioAnalyserDisplayMode mode )
	{
		this.displayMode = mode;
		this.displayModeProcessor = displayMode.getModeProcessor();
		displayModeProcessor.fullProcess( this );
	}
}
