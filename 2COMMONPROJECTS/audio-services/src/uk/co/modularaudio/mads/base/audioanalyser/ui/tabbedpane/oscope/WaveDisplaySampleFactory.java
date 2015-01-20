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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import java.awt.Graphics2D;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserDataBuffers;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.BufferStateListener;
import uk.co.modularaudio.mads.base.audioanalyser.ui.UiBufferPositions;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintDefaultUpdateStructure;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintDirection;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdate;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdateType;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterSampleFactory;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class WaveDisplaySampleFactory implements RollPainterSampleFactory<WaveDisplayBufferedImage, WaveDisplayBufferedImageClearer>, BufferStateListener
{
//	private final static Log log = LogFactory.getLog( WaveDisplaySampleFactory.class.getName() );

	private final BufferedImageAllocator bia;
	
	private final AudioAnalyserUiBufferState uiBufferState;

	private AudioAnalyserDataBuffers dataBuffers = null;
	
	private final WaveDisplayBufferedImageClearer bufferClearer;
	private final AllocationMatch localMatch = new AllocationMatch();
	private final RollPaintDefaultUpdateStructure update = new RollPaintDefaultUpdateStructure();
	
	private final int displayWidth;
	private final int displayHeight;

	private int samplesMovementThreshold = 44100;
	
	private boolean needsFullUpdate;
	
	private final UiBufferPositions previousBufferPositions;
	private int previousStartWindowBufferPos;
	
	private DisplayPresentationProcessor presentationProcessor;
	
	public WaveDisplaySampleFactory( BufferedImageAllocator bia, AudioAnalyserUiBufferState uiBufferState, int displayWidth, int displayHeight )
	{
		this.bia = bia;
		this.uiBufferState = uiBufferState;
		uiBufferState.addBufferStateListener( this );

		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;
		
		needsFullUpdate = true;
		
		bufferClearer = new WaveDisplayBufferedImageClearer( displayWidth, displayHeight );
		
		previousBufferPositions = new UiBufferPositions( uiBufferState.bufferPositions );
		// Use window offsets that will definitely cause a full repaint
		previousBufferPositions.startWindowOffset = 0;
		previousBufferPositions.endWindowOffset = 1;
		previousStartWindowBufferPos = 0;
		
	}
	
	@Override
	public WaveDisplayBufferedImage createBuffer(int bufNum) throws DatastoreException
	{
		WaveDisplayBufferedImage retVal = new WaveDisplayBufferedImage();
		retVal.tbi = bia.allocateBufferedImage(this.getClass().getSimpleName(), localMatch, AllocationLifetime.SHORT, AllocationBufferType.TYPE_INT_RGB, displayWidth, displayHeight );
		retVal.bi = retVal.tbi.getUnderlyingBufferedImage();
		retVal.g = retVal.bi.createGraphics();
		bufferClearer.clearBuffer(bufNum, retVal);
		return retVal;
	}

	@Override
	public void freeBuffer(WaveDisplayBufferedImage bufferToFree)
			throws DatastoreException
	{
		bia.freeBufferedImage(bufferToFree.tbi);
	}

	@Override
	public void receiveStartup(HardwareIOChannelSettings ratesAndLatency,
			MadTimingParameters timingParameters)
	{
		dataBuffers = uiBufferState.getDataBuffers();
		
		int sampleRate = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();
		// One second limit for wrap around tests.
		samplesMovementThreshold = sampleRate;
		
		setNeedsFullUpdate();
	}

	@Override
	public void receiveStop()
	{
	}

	@Override
	public void receiveDestroy()
	{
	}

	public void setNeedsFullUpdate()
	{
		needsFullUpdate = true;
	}

	@Override
	public WaveDisplayBufferedImageClearer getBufferClearer()
	{
		return bufferClearer;
	}

	@Override
	public RollPaintUpdate getPaintUpdate()
	{
		RollPaintUpdateType updateType;
		RollPaintDirection updateDirection;
		int numPixelsAvailable;
		
		int numSamplesPerPixel;
		numSamplesPerPixel = uiBufferState.bufferPositions.numSamplesToDisplay / displayWidth;
		int startWindowBufferPos;
		
		if( dataBuffers == null )
		{
			updateType = RollPaintUpdateType.NONE;
			updateDirection = RollPaintDirection.FORWARDS;
			numPixelsAvailable = 0;
			startWindowBufferPos = 0;
		}
		else if( needsFullUpdate )
		{
			needsFullUpdate = false;
			updateType = RollPaintUpdateType.FULL;
			updateDirection = RollPaintDirection.FORWARDS;
			numPixelsAvailable = 0;

			startWindowBufferPos = calcStartWindowBufferPos();
		}
		else
		{
			boolean numSamplesToDisplayDifference = (previousBufferPositions.numSamplesToDisplay != uiBufferState.bufferPositions.numSamplesToDisplay);
			
			startWindowBufferPos = calcStartWindowBufferPos();
			int startWindowBufferDelta = calcStartWindowBufferPositionDelta( startWindowBufferPos, previousStartWindowBufferPos );
			int absStartWindowBufferDelta = Math.abs( startWindowBufferDelta );
			
			if( !numSamplesToDisplayDifference )
			{
				updateDirection = (startWindowBufferDelta >= 0 ? RollPaintDirection.FORWARDS : RollPaintDirection.BACKWARDS );
				if( absStartWindowBufferDelta >= samplesMovementThreshold )
				{
					updateType = RollPaintUpdateType.FULL;
					numPixelsAvailable = previousBufferPositions.numSamplesToDisplay / numSamplesPerPixel;
				}
				else
				{
					numPixelsAvailable =  startWindowBufferDelta / numSamplesPerPixel;
					if( numPixelsAvailable > displayWidth )
					{
						updateType = RollPaintUpdateType.FULL;
						numPixelsAvailable = previousBufferPositions.numSamplesToDisplay / numSamplesPerPixel;
					}
					else if( numPixelsAvailable != 0 )
					{
						updateType = RollPaintUpdateType.DELTA;
					}
					else
					{
						updateType = RollPaintUpdateType.NONE;
					}
				}
			}
			else
			{
				// Difference in num samples to display
				updateType = RollPaintUpdateType.FULL;
				updateDirection = RollPaintDirection.FORWARDS;
				numPixelsAvailable = 0;
			}
		}
		
		switch( updateType )
		{
			case FULL:
			{
				previousStartWindowBufferPos = startWindowBufferPos;
				break;
			}
			case DELTA:
			{
				int numToMoveBy = numPixelsAvailable * numSamplesPerPixel;
				previousStartWindowBufferPos += numToMoveBy;
				if( previousStartWindowBufferPos >= dataBuffers.bufferLength )
				{
					previousStartWindowBufferPos -= dataBuffers.bufferLength;
				}
				else if( previousStartWindowBufferPos < 0 )
				{
					previousStartWindowBufferPos += dataBuffers.bufferLength;
				}
				break;
			}
			default:
			{
				break;
			}
		}

		update.setUpdateValues( updateType, updateDirection, numPixelsAvailable );
		if( updateType !=RollPaintUpdateType.NONE )
		{
			previousBufferPositions.assign(uiBufferState.bufferPositions);
//			log.debug("Returning an update of " + update.toString() );
		}
		else
		{
//			log.debug("Skipping an update as not enough delta");
		}
		return update;
	}

	@Override
	public void fullFillSamples( RollPaintUpdate update, WaveDisplayBufferedImage buffer)
	{
//		log.debug("Doing full fill");
		Graphics2D g = buffer.g;
		
		if( dataBuffers == null )
		{
			return;
		}
		
		int numSamplesToDisplay = uiBufferState.bufferPositions.numSamplesToDisplay;
		int numSamplesPerPixel = numSamplesToDisplay / displayWidth;

		int startWindowBufferPos = calcStartWindowBufferPos();
		
		int preIndexPos = startWindowBufferPos  - numSamplesPerPixel;
		if( preIndexPos < 0 )
		{
			preIndexPos += dataBuffers.bufferLength;
		}
		
//		log.debug("Beginning pre index from position " + preIndexPos + " buffer start pos is " + uiBufferState.bufferPositions.startBufferPos );
//		log.debug("So the calculated start window buffer pos is " + startWindowBufferPos );

		presentationProcessor.doPreIndex( preIndexPos, numSamplesPerPixel );
		
		int numPixelsDone = 0;
		
		int bufferIndexRemainder = 0;
		
		for( ; numPixelsDone < displayWidth ; bufferIndexRemainder += numSamplesPerPixel, ++numPixelsDone )
		{
			int indexInt = startWindowBufferPos + bufferIndexRemainder;
			if( indexInt >= dataBuffers.bufferLength )
			{
				indexInt -= dataBuffers.bufferLength;
			}
			
			presentationProcessor.presentPixel( indexInt, numSamplesPerPixel, g, numPixelsDone );
		}
	}

	@Override
	public void deltaFillSamples( RollPaintUpdate update,
		int displayOffset,
		WaveDisplayBufferedImage buffer,
		int paintOffset,
		int numToPaint,
		WaveDisplayBufferedImage otherBuffer )
	{
		int numSamplesToDisplay = uiBufferState.bufferPositions.numSamplesToDisplay;
		int numSamplesPerPixel = numSamplesToDisplay / displayWidth;

//		log.debug("Doing delta fill of " + numToPaint + " with " + numSamplesPerPixel + " samples per pixel at displayOffset " + displayOffset);
		Graphics2D g = buffer.g;
	
		if( dataBuffers == null )
		{
			return;
		}
		
		int startWindowBufferPos = previousStartWindowBufferPos;
		
		int numPixelsDone = 0;
		
		RollPaintDirection direction = update.getDirection();
		
		switch( direction )
		{
			case BACKWARDS:
			{
				int samplesOffsetFromStart = numSamplesPerPixel * displayOffset;
				int bufferIndex = startWindowBufferPos + samplesOffsetFromStart;
				if( bufferIndex >= dataBuffers.bufferLength )
				{
					bufferIndex -= dataBuffers.bufferLength;
				}
				int preIndexInt = bufferIndex - numSamplesPerPixel;
				
				if( preIndexInt < 0 )
				{
					preIndexInt += dataBuffers.bufferLength;
				}
//				log.debug("Beginning pre index from position " + preIndexInt + " buffer start pos is " + uiBufferState.bufferPositions.startBufferPos );
//				log.debug("So the calculated start window buffer pos is " + startWindowBufferPos );

				presentationProcessor.doPreIndex(preIndexInt, numSamplesPerPixel);
				
				int bufferRemainder = 0;
				
				for( ; numPixelsDone < numToPaint ; bufferRemainder += numSamplesPerPixel, ++numPixelsDone )
				{
					int indexInt = bufferIndex + bufferRemainder;
					if( indexInt >= dataBuffers.bufferLength )
					{
						indexInt -= dataBuffers.bufferLength;
					}
//					log.debug("Calculating min max for index " + indexInt );
					int pixel = paintOffset + numPixelsDone;
					presentationProcessor.presentPixel(indexInt, numSamplesPerPixel, g, pixel );
				}
				break;
			}
			default:
			{
				// Work out how many "pixels" from the start we should be
				int samplesOffsetFromStart = numSamplesPerPixel * displayOffset;

				int bufferIndex = startWindowBufferPos + samplesOffsetFromStart;
				if( bufferIndex >= dataBuffers.bufferLength )
				{
					bufferIndex -= dataBuffers.bufferLength;
				}

				int preIndexInt = bufferIndex - numSamplesPerPixel;
				
				if( preIndexInt < 0 )
				{
					preIndexInt += dataBuffers.bufferLength;
				}
//				log.debug("Beginning preindex from position " + preIndexInt );
				presentationProcessor.doPreIndex(preIndexInt, numSamplesPerPixel);

				int bufferRemainder = 0;
				
				for( ; numPixelsDone < numToPaint ; bufferRemainder += numSamplesPerPixel, ++numPixelsDone )
				{
					int indexInt = bufferIndex + bufferRemainder;
					if( indexInt >= dataBuffers.bufferLength )
					{
						indexInt -= dataBuffers.bufferLength;
					}
//					log.debug("Calculating min max for index " + indexInt );

					int pixel = paintOffset + numPixelsDone;
					presentationProcessor.presentPixel(indexInt, numSamplesPerPixel, g, pixel );
				}
				break;
			}
		}
	}

	private int calcStartWindowBufferPos()
	{
		int startPosition = uiBufferState.bufferPositions.startBufferPos + uiBufferState.bufferPositions.startWindowOffset;
		if( startPosition >= dataBuffers.bufferLength )
		{
			startPosition -= dataBuffers.bufferLength;
		}
		return startPosition;
	}
	
	private int calcStartWindowBufferPositionDelta( int curStartWindowBufferPos, int previousStartWindowBufferPos )
	{
		int numSamplesToDisplay = uiBufferState.bufferPositions.numSamplesToDisplay;
		
		int diffFromLastBufferPos = curStartWindowBufferPos - previousStartWindowBufferPos;
		
		int sigNum = (diffFromLastBufferPos < 0 ? -1 : 1 );
		int absDfldp = Math.abs(diffFromLastBufferPos);
		int aSamplesMoved;
		if( absDfldp >= samplesMovementThreshold )
		{
			// Either the start or the end position "wraps" and we've got a large value.
			if( curStartWindowBufferPos < previousStartWindowBufferPos )
			{
				diffFromLastBufferPos = (dataBuffers.bufferLength - previousStartWindowBufferPos) + curStartWindowBufferPos;
			}
			else
			{
				diffFromLastBufferPos = curStartWindowBufferPos - dataBuffers.bufferLength - previousStartWindowBufferPos;
			}
			absDfldp = Math.abs(diffFromLastBufferPos);
			
			if( absDfldp >= samplesMovementThreshold )
			{
				sigNum = diffFromLastBufferPos < 0 ? -1 : 1;
				aSamplesMoved = sigNum * numSamplesToDisplay;
			}
			else
			{
				aSamplesMoved = diffFromLastBufferPos;
			}
		}
		else
		{
			aSamplesMoved = diffFromLastBufferPos;
		}

//		log.debug("calcStartWindowBufferPositionDelta found " + aSamplesMoved + " samples moved");

		return aSamplesMoved;
	}

	public void setPresentationProcessor( DisplayPresentationProcessor dpp )
	{
		this.presentationProcessor = dpp;
	}

}
