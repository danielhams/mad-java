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

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadInstance;
import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class AudioAnalyserUiBufferState
{
	private static Log log = LogFactory.getLog( AudioAnalyserUiBufferState.class.getName() );
	
	public enum ZoomDirection
	{
		IN,
		OUT
	};
	
	public enum PanDirection
	{
		BACK,
		FORWARD
	};

	private final AudioAnalyserMadInstance instance;
	private final float bufferLengthMilliseconds;
	private int minSamplesToDisplay = -1;
	public int maxSamplesToDisplay = -1;
	
	private AudioAnalyserDataRingBuffer instanceRingBuffer = null;
	
	private AudioAnalyserDataBuffers dataBuffers = null;
	
	private int sampleRate = -1;
	private int lastBufferWriteIndex = -1;
	
	private ArrayList<BufferStateListener> bufferStateListeners = new ArrayList<BufferStateListener>();
	private ArrayList<BufferFreezeListener> bufferFreezeListeners = new ArrayList<BufferFreezeListener>();
	private ArrayList<BufferZoomAndPositionListener> bufferZoomAndPositionListeners = new ArrayList<BufferZoomAndPositionListener>();
	
	public final UiBufferPositions bufferPositions;
	
	private int samplesDeltaPerZoomChange;

	public AudioAnalyserUiBufferState( AudioAnalyserMadInstance auInstance, float bufferLengthMilliseconds )
	{
		this.instance = auInstance;
		this.bufferLengthMilliseconds = bufferLengthMilliseconds;
		
		int sampleRate = 44100;
		int numSamplesToDisplay = sampleRate * 4;
		minSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 80.0f );
		maxSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 5000.0f );
		bufferPositions = new UiBufferPositions( false, numSamplesToDisplay, 0, 0, maxSamplesToDisplay - numSamplesToDisplay, maxSamplesToDisplay );

		// Create a "default" buffer set and size - we'll resize if need be on startup
		int dataBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(sampleRate, bufferLengthMilliseconds);
		// And fill with empties
		dataBuffers = new AudioAnalyserDataBuffers( sampleRate, dataBufferLength, true );
	}

	public void receiveStartup( HardwareIOChannelSettings ratesAndLatency,
			MadTimingParameters timingParameters)
	{
		sampleRate = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();
		
		int dataBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(sampleRate, bufferLengthMilliseconds);
		dataBuffers.resetIfNeeded( sampleRate, dataBufferLength, true );
		
		minSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 320.0f );
		maxSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 5000.0f );
		samplesDeltaPerZoomChange = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 100.0f );
		
		instanceRingBuffer = instance.getDataRingBuffer();
		lastBufferWriteIndex = instanceRingBuffer.getWritePosition();

		// We reset the positions anyway since the buffer is getting reset (due to possible sample rate change)
		int newEndBufferPos = dataBuffers.writePosition;
		int newStartBufferPos = newEndBufferPos - maxSamplesToDisplay;
		if( newStartBufferPos < 0 )
		{
			newStartBufferPos += dataBuffers.bufferLength;
		}
		
		int newStartWindowOffset = maxSamplesToDisplay - bufferPositions.numSamplesToDisplay;
		int newEndWindowOffset = maxSamplesToDisplay;
		bufferPositions.resetBufferPositions( newStartBufferPos, newEndBufferPos, newStartWindowOffset, newEndWindowOffset );
		
		for( BufferStateListener bsl : bufferStateListeners )
		{
			bsl.receiveStartup( ratesAndLatency,
					timingParameters );
		}
	}

	public void receiveStop()
	{
		for( BufferStateListener bsl : bufferStateListeners )
		{
			bsl.receiveStop();
		}
	}

	public void destroy()
	{
		for( BufferStateListener bsl : bufferStateListeners )
		{
			bsl.receiveDestroy();
		}
	}

	public void receiveBufferIndexUpdate( long updateTimestamp, int bufferWriteIndex )
	{
		if( bufferWriteIndex != lastBufferWriteIndex )
		{
			int numReadable = instanceRingBuffer.getNumReadableWithWriteIndex( bufferWriteIndex );

			if( bufferPositions.frozen )
			{
				instanceRingBuffer.moveForward( numReadable );
			}
			else
			{
				int spaceAvailable = dataBuffers.getNumWriteable();
				if( spaceAvailable < numReadable )
				{
					int spaceToFree = numReadable - spaceAvailable;
					dataBuffers.moveForward( spaceToFree );
				}
	
				// Add on the new data
				int numRead = instanceRingBuffer.readToRingWithWriteIndex( bufferWriteIndex, dataBuffers,  numReadable );
				if( numRead != numReadable )
				{
					log.error("Failed reading from data ring buffer - expected " + numReadable + " and received " + numRead);
					// Zero buffer and set to full
					Arrays.fill( dataBuffers.buffer, 0.0f );
					dataBuffers.readPosition = 0;
					dataBuffers.writePosition = dataBuffers.bufferLength - 1;
				}
				// Filter into relevant freq buffers
				
				int newEndBufferPos = dataBuffers.writePosition;
				int newStartBufferPos = newEndBufferPos - maxSamplesToDisplay;
				if( newStartBufferPos < 0 )
				{
					newStartBufferPos += dataBuffers.bufferLength;
				}
				bufferPositions.resetBufferPositions( newStartBufferPos, newEndBufferPos, bufferPositions.startWindowOffset, bufferPositions.endWindowOffset );
			}
		}
		lastBufferWriteIndex = bufferWriteIndex;
	}
	
	public void addBufferStateListener( BufferStateListener bsl )
	{
		bufferStateListeners.add( bsl );
	}
	
	public void addBufferFreezeListener( BufferFreezeListener bfl )
	{
		bufferFreezeListeners.add( bfl );
		bfl.receiveFreezeStateChange(bufferPositions.frozen);
	}
	
	public void addBufferZoomAndPositionListener( BufferZoomAndPositionListener bzapl )
	{
		bufferZoomAndPositionListeners.add( bzapl );
	}
	
	public void setFrozen( boolean frozen )
	{
		boolean shouldNotify = (bufferPositions.frozen != frozen );
		if( shouldNotify )
		{
			this.bufferPositions.frozen = frozen;
			if( !frozen )
			{
				// Reset buffer window pos to be the end of the buffer
				bufferPositions.endWindowOffset = maxSamplesToDisplay;
				bufferPositions.startWindowOffset = maxSamplesToDisplay - bufferPositions.numSamplesToDisplay;
				for( BufferZoomAndPositionListener bzpl : bufferZoomAndPositionListeners )
				{
					bzpl.receiveZoomAndPositionUpdate();
				}
			}
			
			for( BufferFreezeListener bfl : bufferFreezeListeners )
			{
				bfl.receiveFreezeStateChange( frozen );
			}
		}
	}
	
	public boolean isFrozen()
	{
		return bufferPositions.frozen;
	}
	
	public AudioAnalyserDataBuffers getDataBuffers()
	{
		return dataBuffers;
	}

	public void zoom( ZoomDirection zoomDirection )
	{
		int previousNumSamplesToDisplay = bufferPositions.numSamplesToDisplay;
		int newNumSamplesToDisplay = bufferPositions.numSamplesToDisplay;
		switch( zoomDirection )
		{
			case IN:
			{
				newNumSamplesToDisplay -= samplesDeltaPerZoomChange;
				if( newNumSamplesToDisplay < minSamplesToDisplay )
				{
					newNumSamplesToDisplay = minSamplesToDisplay;
				}
				break;
			}
			default:
			{
				newNumSamplesToDisplay += samplesDeltaPerZoomChange;
				if( newNumSamplesToDisplay > maxSamplesToDisplay )
				{
					newNumSamplesToDisplay = maxSamplesToDisplay;
				}
				break;
			}
		}
//		log.debug("Will change num samples to display to " + newNumSamplesToDisplay + " samples");

		// Now adjust the start and end positions
		int newStartBufferPos = bufferPositions.startBufferPos;
		int newEndBufferPos = bufferPositions.endBufferPos;
		int newEndWindowOffset;
		int newStartWindowOffset;
//		log.debug("Adjusting origStartOffset(" + bufferPositions.startWindowOffset + ") origEndOffset(" + bufferPositions.endWindowOffset + ")");
		if( bufferPositions.frozen )
		{
			if( newNumSamplesToDisplay >= maxSamplesToDisplay )
			{
				newStartWindowOffset = 0;
				newEndWindowOffset = maxSamplesToDisplay;
			}
			else
			{
				// Adjust around the center
				int centerBufferPos = bufferPositions.startWindowOffset + ((bufferPositions.endWindowOffset - bufferPositions.startWindowOffset) / 2);
				int halfSamplesToDisplay = newNumSamplesToDisplay / 2;
				newStartWindowOffset = centerBufferPos - halfSamplesToDisplay;
				newEndWindowOffset = centerBufferPos + halfSamplesToDisplay;
				if( newStartWindowOffset < 0 )
				{
					newStartWindowOffset = 0;
					newEndWindowOffset = newNumSamplesToDisplay;
				}
				else if( newEndWindowOffset >= maxSamplesToDisplay )
				{
					newEndWindowOffset = maxSamplesToDisplay;
					newStartWindowOffset = newEndWindowOffset - newNumSamplesToDisplay;
				}
			}
//			log.debug("Will zoom adjust frozen with newStart(" + newStartWindowOffset + ") newEnd(" + newEndWindowOffset + ")");
		}
		else
		{
			// Adjust around the end
			newEndWindowOffset = maxSamplesToDisplay;
			newStartWindowOffset = maxSamplesToDisplay - newNumSamplesToDisplay;
		}
//		log.debug("Will zoom adjust to newWindowStart(" + newStartWindowOffset + ") newWindowEnd(" + newEndWindowOffset + ") with numSam("
//				+ newNumSamplesToDisplay +")" );
		
		bufferPositions.setNumSamplesToDisplay( newNumSamplesToDisplay );
		bufferPositions.resetBufferPositions( newStartBufferPos, newEndBufferPos, newStartWindowOffset, newEndWindowOffset );
		
		if( newNumSamplesToDisplay != previousNumSamplesToDisplay )
		{
			for( BufferZoomAndPositionListener bzapl : bufferZoomAndPositionListeners )
			{
				bzapl.receiveZoomAndPositionUpdate();
			}
		}
	}
	
	public void pan( PanDirection direction )
	{
		// Always move by a scaled amount
		int samplesDeltaForPanChange = bufferPositions.numSamplesToDisplay / 20;
		
		int numSamplesChangeForStartAndEnd;
		
		switch( direction )
		{
			case BACK:
			{
				int newStartWindowOffset = bufferPositions.startWindowOffset - samplesDeltaForPanChange;
				if( newStartWindowOffset < 0 )
				{
					numSamplesChangeForStartAndEnd = (-bufferPositions.startWindowOffset);
				}
				else
				{
					numSamplesChangeForStartAndEnd = (-samplesDeltaForPanChange);
				}
				break;
			}
			case FORWARD:
			default:
			{
				int newEndWindowOffset = bufferPositions.endWindowOffset + samplesDeltaForPanChange;
				int diffToEnd = newEndWindowOffset - maxSamplesToDisplay;
				if( diffToEnd > 0 )
				{
					numSamplesChangeForStartAndEnd = maxSamplesToDisplay - bufferPositions.endWindowOffset;
				}
				else
				{
					numSamplesChangeForStartAndEnd = samplesDeltaForPanChange;
				}
				break;
			}
		}
		
//		log.debug( "Pan delta is " + numSamplesChangeForStartAndEnd );
		
		bufferPositions.startWindowOffset += numSamplesChangeForStartAndEnd;
		bufferPositions.endWindowOffset += numSamplesChangeForStartAndEnd;
		
//		log.debug("Will pan adjust to newWindowStart(" + bufferPositions.startWindowOffset + ") newWindowEnd(" + bufferPositions.endWindowOffset + ") with " +
//				" numSam(" + bufferPositions.numSamplesToDisplay + ")" );
		
		for( BufferZoomAndPositionListener bzpl : bufferZoomAndPositionListeners )
		{
			bzpl.receiveZoomAndPositionUpdate();
		}
	}
}
