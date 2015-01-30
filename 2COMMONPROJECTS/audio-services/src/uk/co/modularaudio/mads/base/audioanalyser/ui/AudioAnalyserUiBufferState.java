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
	private int minSamplesToDisplay;
	public int maxSamplesToDisplay;

	private AudioAnalyserDataRingBuffer instanceRingBuffer;

	private final AudioAnalyserDataBuffers dataBuffers;

	private int sampleRate;
	private int lastBufferWriteIndex;

	private final ArrayList<BufferStateListener> bufferStateListeners = new ArrayList<BufferStateListener>();
	private final ArrayList<BufferFreezeListener> bufferFreezeListeners = new ArrayList<BufferFreezeListener>();
	private final ArrayList<BufferZoomAndPositionListener> bufferZoomAndPositionListeners = new ArrayList<BufferZoomAndPositionListener>();

	public final UiBufferPositions bufferPositions;

	private int samplesDeltaPerZoomChange;

	public AudioAnalyserUiBufferState( final AudioAnalyserMadInstance auInstance, final float bufferLengthMilliseconds )
	{
		this.instance = auInstance;
		this.bufferLengthMilliseconds = bufferLengthMilliseconds;

		final int sampleRate = 44100;
		final int numSamplesToDisplay = sampleRate * 4;
		minSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 80.0f );
		maxSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 5000.0f );
		bufferPositions = new UiBufferPositions( false, numSamplesToDisplay, 0, 0, maxSamplesToDisplay - numSamplesToDisplay, maxSamplesToDisplay );

		// Create a "default" buffer set and size - we'll resize if need be on startup
		final int dataBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(sampleRate, bufferLengthMilliseconds);
		// And fill with empties
		dataBuffers = new AudioAnalyserDataBuffers( sampleRate, dataBufferLength, true );
	}

	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters)
	{
		sampleRate = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();

		final int dataBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(sampleRate, bufferLengthMilliseconds);
		dataBuffers.resetIfNeeded( sampleRate, dataBufferLength, true );

		minSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 320.0f );
		maxSamplesToDisplay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 5000.0f );
		samplesDeltaPerZoomChange = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 100.0f );

		instanceRingBuffer = instance.getDataRingBuffer();
		lastBufferWriteIndex = instanceRingBuffer.getWritePosition();

		// We reset the positions anyway since the buffer is getting reset (due to possible sample rate change)
		final int newEndBufferPos = dataBuffers.writePosition;
		int newStartBufferPos = newEndBufferPos - maxSamplesToDisplay;
		if( newStartBufferPos < 0 )
		{
			newStartBufferPos += dataBuffers.bufferLength;
		}

		final int newStartWindowOffset = maxSamplesToDisplay - bufferPositions.numSamplesToDisplay;
		final int newEndWindowOffset = maxSamplesToDisplay;
		bufferPositions.resetBufferPositions( newStartBufferPos, newEndBufferPos, newStartWindowOffset, newEndWindowOffset );

		for( final BufferStateListener bsl : bufferStateListeners )
		{
			bsl.receiveStartup( ratesAndLatency,
					timingParameters );
		}
	}

	public void receiveStop()
	{
		for( final BufferStateListener bsl : bufferStateListeners )
		{
			bsl.receiveStop();
		}
	}

	public void destroy()
	{
		for( final BufferStateListener bsl : bufferStateListeners )
		{
			bsl.receiveDestroy();
		}
	}

	public void receiveBufferIndexUpdate( final long updateTimestamp, final int bufferWriteIndex )
	{
		if( bufferWriteIndex != lastBufferWriteIndex )
		{
			final int numReadable = instanceRingBuffer.getNumReadableWithWriteIndex( bufferWriteIndex );

			if( bufferPositions.frozen )
			{
				instanceRingBuffer.moveForward( numReadable );
			}
			else
			{
				final int spaceAvailable = dataBuffers.getNumWriteable();
				if( spaceAvailable < numReadable )
				{
					final int spaceToFree = numReadable - spaceAvailable;
					dataBuffers.moveForward( spaceToFree );
				}

				// Add on the new data
				final int numRead = instanceRingBuffer.readToRingWithWriteIndex( bufferWriteIndex, dataBuffers,  numReadable );
				if( numRead != numReadable )
				{
					if( log.isErrorEnabled() )
					{
						log.error("Failed reading from data ring buffer - expected " + numReadable + " and received " + numRead);
					}
					// Zero buffer and set to full
					Arrays.fill( dataBuffers.buffer, 0.0f );
					dataBuffers.readPosition = 0;
					dataBuffers.writePosition = dataBuffers.bufferLength - 1;
				}
				// Filter into relevant freq buffers

				final int newEndBufferPos = dataBuffers.writePosition;
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

	public void addBufferStateListener( final BufferStateListener bsl )
	{
		bufferStateListeners.add( bsl );
	}

	public void addBufferFreezeListener( final BufferFreezeListener bfl )
	{
		bufferFreezeListeners.add( bfl );
		bfl.receiveFreezeStateChange(bufferPositions.frozen);
	}

	public void addBufferZoomAndPositionListener( final BufferZoomAndPositionListener bzapl )
	{
		bufferZoomAndPositionListeners.add( bzapl );
	}

	public void setFrozen( final boolean frozen )
	{
		final boolean shouldNotify = (bufferPositions.frozen != frozen );
		if( shouldNotify )
		{
			this.bufferPositions.frozen = frozen;
			if( !frozen )
			{
				// Reset buffer window pos to be the end of the buffer
				bufferPositions.endWindowOffset = maxSamplesToDisplay;
				bufferPositions.startWindowOffset = maxSamplesToDisplay - bufferPositions.numSamplesToDisplay;
				for( final BufferZoomAndPositionListener bzpl : bufferZoomAndPositionListeners )
				{
					bzpl.receiveZoomAndPositionUpdate();
				}
			}

			for( final BufferFreezeListener bfl : bufferFreezeListeners )
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

	public void zoom( final ZoomDirection zoomDirection )
	{
		final int previousNumSamplesToDisplay = bufferPositions.numSamplesToDisplay;
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
		final int newStartBufferPos = bufferPositions.startBufferPos;
		final int newEndBufferPos = bufferPositions.endBufferPos;
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
				final int centerBufferPos = bufferPositions.startWindowOffset + ((bufferPositions.endWindowOffset - bufferPositions.startWindowOffset) / 2);
				final int halfSamplesToDisplay = newNumSamplesToDisplay / 2;
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
			for( final BufferZoomAndPositionListener bzapl : bufferZoomAndPositionListeners )
			{
				bzapl.receiveZoomAndPositionUpdate();
			}
		}
	}

	public void pan( final PanDirection direction )
	{
		// Always move by a scaled amount
		final int samplesDeltaForPanChange = bufferPositions.numSamplesToDisplay / 20;

		int numSamplesChangeForStartAndEnd;

		switch( direction )
		{
			case BACK:
			{
				final int newStartWindowOffset = bufferPositions.startWindowOffset - samplesDeltaForPanChange;
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
				final int newEndWindowOffset = bufferPositions.endWindowOffset + samplesDeltaForPanChange;
				final int diffToEnd = newEndWindowOffset - maxSamplesToDisplay;
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

		for( final BufferZoomAndPositionListener bzpl : bufferZoomAndPositionListeners )
		{
			bzpl.receiveZoomAndPositionUpdate();
		}
	}
}
