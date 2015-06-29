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

package uk.co.modularaudio.mads.base.waveroller.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintDefaultUpdateStructure;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdate;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdateType;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterSampleFactory;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.math.MinMaxComputer;

public class WaveRollerBufferSampleFactory
	implements RollPainterSampleFactory<WaveRollerBuffer,WaveRollerBufferCleaner>,
	ScaleLimitChangeListener
{
	private static Log log = LogFactory.getLog( WaveRollerBufferSampleFactory.class.getName() );

	private final BufferedImageAllocator bufferImageAllocator;
	private final AllocationMatch localAllocationMatch = new AllocationMatch();
	private final RollPaintDefaultUpdateStructure rpUpdateType = new RollPaintDefaultUpdateStructure();
	private final WaveRollerBufferCleaner bufferClearer;

	private final UnsafeFloatRingBuffer displayRingBuffer;

	private final Rectangle displayBounds;
	private final float valueScaleForMargins;
	private float maxDbScaleMultiplier = 1.0f;

	private int lastBufferPos;
	private int numSamplesPerPixel;

	private int captureRenderLength;
	private boolean needsFullUpdate;

	private final float[] minMaxValues = new float[2];
	private final float[] previousMinMaxValues = new float[2];

	public WaveRollerBufferSampleFactory( final WaveRollerMadUiInstance uiInstance,
			final BufferedImageAllocator bufferImageAllocator,
			final UnsafeFloatRingBuffer displayRingBuffer,
			final Rectangle displayBounds,
			final float valueScaleForMargins )
	{
		this.bufferImageAllocator = bufferImageAllocator;
		bufferClearer = new WaveRollerBufferCleaner( displayBounds );
		this.displayRingBuffer = displayRingBuffer;
		this.displayBounds = displayBounds;
		this.valueScaleForMargins = valueScaleForMargins;
		lastBufferPos = displayRingBuffer.readPosition;

		// Not perfect, but needs a value
		numSamplesPerPixel = 1;

		captureRenderLength = 1;
		needsFullUpdate = true;

		uiInstance.addScaleChangeListener( this );
	}

	public void resetForFullRepaint()
	{
		// And reset to perform a full render next time around
		this.lastBufferPos = displayRingBuffer.writePosition - (captureRenderLength + 1);
		if( lastBufferPos < 0 )
		{
			lastBufferPos += displayRingBuffer.bufferLength;
		}
		needsFullUpdate = true;
	}

	public void setCaptureRenderLength( final int captureRenderLength )
	{
		this.captureRenderLength = captureRenderLength;
		this.numSamplesPerPixel = (int)(captureRenderLength / (float)displayBounds.width);
		if( numSamplesPerPixel <= 0 )
		{
			numSamplesPerPixel = 1;
		}
//		log.debug("Setting capture render length to " + captureRenderLength + " which is " + numSamplesPerPixel + " samples per pixel" );

		resetForFullRepaint();
	}

	@Override
	public WaveRollerBufferCleaner getBufferClearer()
	{
		return bufferClearer;
	}

	@Override
	public WaveRollerBuffer createBuffer( final int bufNum ) throws DatastoreException
	{
		final TiledBufferedImage tbi = bufferImageAllocator.allocateBufferedImage( this.getClass().getSimpleName(),
				localAllocationMatch, AllocationLifetime.SHORT, AllocationBufferType.TYPE_INT_RGB, displayBounds.width, displayBounds.height );
		return new WaveRollerBuffer( tbi );
	}

	@Override
	public void freeBuffer( final WaveRollerBuffer bufferToFree ) throws DatastoreException
	{
		bufferImageAllocator.freeBufferedImage( bufferToFree.tbi );
	}

	private int getNumReadable()
	{
		final int curWritePos = displayRingBuffer.writePosition;
		final int numReadable = ( lastBufferPos > curWritePos ? (displayRingBuffer.bufferLength - lastBufferPos) + curWritePos : curWritePos - lastBufferPos );
		return numReadable;
	}

	private void calcMinMaxForSamples( final int sampleStartIndex )
	{
		minMaxValues[0] = Float.MAX_VALUE;
		minMaxValues[1] = -minMaxValues[0];

		if( numSamplesPerPixel < 1.0f )
		{
			minMaxValues[0] = displayRingBuffer.buffer[sampleStartIndex];
			minMaxValues[1] = displayRingBuffer.buffer[sampleStartIndex];
			return;
		}

		final int linearEndIndex = sampleStartIndex + numSamplesPerPixel;
		final int endIndex = ( linearEndIndex >= displayRingBuffer.bufferLength ? displayRingBuffer.bufferLength : linearEndIndex );

		final int wrappedEndIndex = linearEndIndex - endIndex;

		MinMaxComputer.calcMinMaxForFloats( displayRingBuffer.buffer, sampleStartIndex, endIndex - sampleStartIndex, minMaxValues );
		if( wrappedEndIndex > 0 )
		{
			MinMaxComputer.calcMinMaxForFloats( displayRingBuffer.buffer, 0, wrappedEndIndex, minMaxValues );
		}
	}

	private void extendMinMaxWithPrevious()
	{
		// Make sure we meet up with the adjoining line
		if( previousMinMaxValues[1] < minMaxValues[0] )
		{
			minMaxValues[0] = previousMinMaxValues[1];
		}
		if( previousMinMaxValues[0] > minMaxValues[1] )
		{
			minMaxValues[1] = previousMinMaxValues[0];
		}
	}

	private void fillInMinMaxLine( final Graphics2D g, final int pixelX, final float minValue, final float maxValue )
	{
		final float multiplier = (displayBounds.height / 2.0f);

		g.setColor( WaveRollerColours.DISPLAY_VALUE_COLOUR );
		int yMinVal =  (int)(-minValue * multiplier * maxDbScaleMultiplier);
		int yMaxVal = (int)(-maxValue * multiplier * maxDbScaleMultiplier);

		yMinVal = (int)((yMinVal * valueScaleForMargins) + multiplier);
		yMaxVal = (int)((yMaxVal * valueScaleForMargins) + multiplier);
		g.drawLine(pixelX, yMinVal, pixelX, yMaxVal );
	}

	private int getNumSamplesAvailable()
	{
		final int numReadable = getNumReadable();

		int numPixelsCanOutput = numReadable / numSamplesPerPixel;
		numPixelsCanOutput = (numPixelsCanOutput > displayBounds.width ? displayBounds.width : numPixelsCanOutput );

//		log.debug("Return num pixels as " + numPixelsCanOutput );

		return numPixelsCanOutput;
	}

	@Override
	public RollPaintUpdate getPaintUpdate()
	{
		if( numSamplesPerPixel < 4.0f )
		{
			resetForFullRepaint();
		}
		if( needsFullUpdate )
		{
//			log.debug("Is forced full update");
			needsFullUpdate = false;
			rpUpdateType.setUpdateType( RollPaintUpdateType.FULL );
		}
		else
		{
			if( displayRingBuffer.writePosition != lastBufferPos )
			{
				final int numSamplesAvailable = getNumSamplesAvailable();
				if( numSamplesAvailable != 0 )
				{
//					log.debug("Is delta update");
					rpUpdateType.setNumSamplesInUpdate( numSamplesAvailable );
					rpUpdateType.setUpdateType( RollPaintUpdateType.DELTA );
				}
				else
				{
					rpUpdateType.setUpdateType( RollPaintUpdateType.NONE );
				}
			}
			else
			{
				rpUpdateType.setUpdateType( RollPaintUpdateType.NONE );
			}
		}
		return rpUpdateType;
	}

	@Override
	public void fullFillSamples( final RollPaintUpdate update, final WaveRollerBuffer buffer )
	{
		final Graphics2D g = buffer.graphics;
		previousMinMaxValues[0] = 0.0f;
		previousMinMaxValues[1] = 0.0f;

//		log.debug( "Full repaint " + displayBounds );
		g.setColor( Color.red );

		lastBufferPos = displayRingBuffer.writePosition;
		final int numReadable = displayRingBuffer.getNumReadable();
		int numPixelsFromRing = numReadable / numSamplesPerPixel;
		numPixelsFromRing = (numPixelsFromRing > displayBounds.width ? displayBounds.width : numPixelsFromRing );
		int numZeros = 0;
		if( numPixelsFromRing < displayBounds.width )
		{
			numZeros = displayBounds.width - numPixelsFromRing;
			if( log.isDebugEnabled() )
			{
				log.debug("Using " + numZeros + " zeros");
			}
		}

		int numPixelsDone = 0;

		int bufferIndex;

		if( numZeros > 0 )
		{
			previousMinMaxValues[0] = 0.0f;
			previousMinMaxValues[1] = 0.0f;
			for( int z = 0 ; z < numZeros ; ++z, ++numPixelsDone )
			{
				minMaxValues[0] = 0.0f;
				minMaxValues[1] = 0.0f;
				fillInMinMaxLine( g, numPixelsDone, minMaxValues[0], minMaxValues[1] );
			}

			bufferIndex = lastBufferPos - (numPixelsFromRing * numSamplesPerPixel);
			if( bufferIndex < 0 )
			{
				bufferIndex += displayRingBuffer.bufferLength;
			}
		}
		else
		{
			bufferIndex = lastBufferPos - ((numPixelsFromRing + 1) * numSamplesPerPixel);
			if( bufferIndex < 0 )
			{
				bufferIndex += displayRingBuffer.bufferLength;
			}
			// First one out of the loop so we can reset previousMinMax
			calcMinMaxForSamples( bufferIndex );
			previousMinMaxValues[0] = minMaxValues[0];
			previousMinMaxValues[1] = minMaxValues[1];

			bufferIndex += numSamplesPerPixel;
			numPixelsDone++;
		}

		for( ; bufferIndex < displayRingBuffer.bufferLength && numPixelsDone < displayBounds.width ; bufferIndex += numSamplesPerPixel, ++numPixelsDone )
		{
			final int indexInt = bufferIndex;

//			log.debug("Pixel " + numPixelsDone + " reading from index " + indexInt );
			calcMinMaxForSamples( indexInt );
			extendMinMaxWithPrevious();

			fillInMinMaxLine( g, numPixelsDone, minMaxValues[0], minMaxValues[1] );

			previousMinMaxValues[0] = minMaxValues[0];
			previousMinMaxValues[1] = minMaxValues[1];
		}
		if( numPixelsDone < displayBounds.width )
		{
//			bufferIndex = bufferIndex % displayRingBuffer.bufferLength;
			bufferIndex = bufferIndex - displayRingBuffer.bufferLength;
			for( ; bufferIndex < displayRingBuffer.writePosition && numPixelsDone < displayBounds.width ; bufferIndex += numSamplesPerPixel, ++numPixelsDone )
			{
				final int indexInt = bufferIndex;
//				log.debug("Pixel " + numPixelsDone + " reading from index " + indexInt );
				calcMinMaxForSamples( indexInt );

				fillInMinMaxLine( g, numPixelsDone, minMaxValues[0], minMaxValues[1] );

				previousMinMaxValues[0] = minMaxValues[0];
				previousMinMaxValues[1] = minMaxValues[1];
			}
		}

		lastBufferPos = bufferIndex;
	}

	@Override
	public void deltaFillSamples( final RollPaintUpdate update, final int displayOffset, final WaveRollerBuffer buffer,
			final int bufferSampleOffset, final int numSamples, final WaveRollerBuffer otherBuffer )
	{
		final Graphics2D g = buffer.graphics;
		// Nasty assumptions:
		// (1) the previous min/max values are correct.
		// (2) we are going forwards
//			log.debug( "Delta paint " + numToPaint + " pixels at offset " + paintOffset );

		final int numReadable = getNumReadable();

		int numPixelsCanOutput = numReadable / numSamplesPerPixel;
		numPixelsCanOutput = (numPixelsCanOutput > numSamples ? numSamples : numPixelsCanOutput );

//			log.debug("NumPixelsCanOutputInt is " + numPixelsCanOutputInt);

		int bufferIndex = lastBufferPos;
		int numPixelsDone = 0;

		for( ; bufferIndex < displayRingBuffer.bufferLength && numPixelsDone < numPixelsCanOutput ; bufferIndex += numSamplesPerPixel, ++numPixelsDone )
		{
			final int indexInt = bufferIndex;

			calcMinMaxForSamples( indexInt );
			extendMinMaxWithPrevious();

			final int xOffset = bufferSampleOffset + numPixelsDone;
			fillInMinMaxLine( g,  xOffset, minMaxValues[0], minMaxValues[1] );
//				log.debug("Filled in line at " + xOffset + " from " + minValue + " to " + maxValue );

			previousMinMaxValues[0] = minMaxValues[0];
			previousMinMaxValues[1] = minMaxValues[1];
		}
		if( numPixelsDone < numPixelsCanOutput )
		{
			bufferIndex = bufferIndex - displayRingBuffer.bufferLength;
			for( ; bufferIndex < displayRingBuffer.writePosition && numPixelsDone < numPixelsCanOutput ; bufferIndex += numSamplesPerPixel, ++numPixelsDone )
			{
				final int indexInt = bufferIndex;
				calcMinMaxForSamples( indexInt );
				extendMinMaxWithPrevious();

				final int xOffset = bufferSampleOffset + numPixelsDone;
				fillInMinMaxLine( g,  xOffset, minMaxValues[0], minMaxValues[1] );
//					log.debug("Filled in line at " + xOffset + " from " + minValue + " to " + maxValue );

				previousMinMaxValues[0] = minMaxValues[0];
				previousMinMaxValues[1] = minMaxValues[1];
			}
		}
//			log.debug("Did " + numPixelsDone + " pixels");
		lastBufferPos = bufferIndex;
	}

	@Override
	public void receiveScaleLimitChange( final float newMaxDB )
	{
		final float negatedDb = -newMaxDB;
		final float asLevel = AudioMath.dbToLevelF( negatedDb );
		maxDbScaleMultiplier = asLevel;
		resetForFullRepaint();
	}
}
