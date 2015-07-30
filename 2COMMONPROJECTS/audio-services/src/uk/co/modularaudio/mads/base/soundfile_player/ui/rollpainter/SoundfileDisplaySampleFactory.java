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

package uk.co.modularaudio.mads.base.soundfile_player.ui.rollpainter;

import java.awt.Graphics2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.soundfile_player.ui.SoundfilePlayerColorDefines;
import uk.co.modularaudio.mads.base.soundfile_player.ui.SoundfilePlayerMadUiInstance;
import uk.co.modularaudio.mads.base.soundfile_player.ui.SoundfilePlayerZoomToggleGroupUiJComponent.ZoomLevel;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintDefaultUpdateStructure;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintDirection;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdate;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdateType;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterSampleFactory;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SoundfileDisplaySampleFactory implements
		RollPainterSampleFactory<SoundfileDisplayBuffer, SoundfileDisplayBufferClearer>
{
	private static Log log = LogFactory.getLog( SoundfileDisplaySampleFactory.class.getName() );

	private final SoundfileDisplayBufferClearer bufferClearer;

	private final SampleCachingService sampleCachingService;
	private final BufferedImageAllocator bia;
	private final int displayWidth;
	private final int displayWidthMinusOneOverTwo;
	private final int displayHeight;
//	private final RPSoundfilePlayerMadUiInstance uiInstance;

	private final RollPaintDefaultUpdateStructure updateStructure = new RollPaintDefaultUpdateStructure();

	private final AllocationMatch allocationMatch = new AllocationMatch();

	private long receivedBufferPos = 0;

	private long lastBufferPos;
	private float captureLengthMillis = ZoomLevel.ZOOMED_DEFAULT.getMillisForLevel();
	private int numSamplesPerPixel;

	private float displayMultiplier;

	private boolean needsFullUpdate;

	private float minValue;
	private float maxValue;
	private float previousMinValue;
	private float previousMaxValue;

	private SampleCacheClient scc;

	private final MinMaxSampleAcceptor minMaxSampleAcceptor = new MinMaxSampleAcceptor();

	private final static int DEFAULT_SAMPLES_PER_PIXEL = 371;

	public SoundfileDisplaySampleFactory(final SampleCachingService sampleCachingService,
		final BufferedImageAllocator bia,
		final int displayWidth,
		final int displayHeight,
		final SoundfilePlayerMadUiInstance uiInstance)
	{
		this.sampleCachingService = sampleCachingService;
		this.bia =bia;
		this.displayWidth = displayWidth;
		this.displayWidthMinusOneOverTwo = (int)((displayWidth-1.0f) / 2.0f);
		this.displayHeight = displayHeight;
//		this.uiInstance = uiInstance;

		lastBufferPos = 0;
		numSamplesPerPixel = DEFAULT_SAMPLES_PER_PIXEL;
		needsFullUpdate = true;
		maxValue = -Float.MAX_VALUE;
		minValue = -maxValue;
		previousMaxValue = 0.0f;
		previousMinValue = 0.0f;

		bufferClearer = new SoundfileDisplayBufferClearer( displayWidth, displayHeight );
	}

	private int getNumSamplesAvailable()
	{
		final long numReadable = getNumReadable();
		long numPixelsCanOutput = numReadable / numSamplesPerPixel;
		numPixelsCanOutput = (numPixelsCanOutput > displayWidth ? displayWidth : numPixelsCanOutput );
		final int numPixelsCanOutputInt = (int)numPixelsCanOutput;
//		if( numPixelsCanOutputInt != 0 )
//		{
//			log.debug("Returning " + numPixelsCanOutputInt + " pixels available for output");
//		}
		return numPixelsCanOutputInt;
	}

	@Override
	public void fullFillSamples( final RollPaintUpdate update, final SoundfileDisplayBuffer buffer )
	{
		if( scc == null )
		{
			return;
		}
		final Graphics2D g = buffer.g;

		g.setColor( SoundfilePlayerColorDefines.WAVE_DISPLAY_WAVE_FG_COLOR );

		final long bufferIndex = receivedBufferPos;
		int bufferIndexRemainder = -(numSamplesPerPixel * (displayWidthMinusOneOverTwo));

//		int numSamplesForDisplay = (numSamplesPerPixel * displayWidth);
//		long startBufferPos = bufferIndex;
//		long endBufferPos = bufferIndex + numSamplesForDisplay;
//		log.debug("Full fill from index(" + startBufferPos + ") to (" + endBufferPos +")");

		int numPixelsDone = 0;

		// Set up min and max
		final long preIndex = bufferIndex + bufferIndexRemainder - numSamplesPerPixel;
		calcMinMaxForSamples( preIndex );
		previousMinValue = minValue;
		previousMaxValue = maxValue;
		minValue = 0.0f;
		maxValue = 0.0f;

		for( ; numPixelsDone < displayWidth ; bufferIndexRemainder += numSamplesPerPixel, ++numPixelsDone )
		{
			final long indexInt = bufferIndex + bufferIndexRemainder;
//			log.debug("FullFill pulling pixel " + numPixelsDone + " from index " + indexInt + " due to pos(" + bufferIndex + ")-(" +
//					bufferIndexRemainder + ")" );

			calcMinMaxForSamples( indexInt );
			extendMinMaxWithPrevious();

			fillInMinMaxLine( g, numPixelsDone, minValue, maxValue );

			previousMinValue = minValue;
			previousMaxValue = maxValue;
		}

		lastBufferPos = receivedBufferPos;
//		log.debug("Full fill setting last buffer pos to (" + lastBufferPos +") with zero remainder");

//		log.debug("Full fill");
	}

	@Override
	public void deltaFillSamples( final RollPaintUpdate update,
			final int displayOffset,
			final SoundfileDisplayBuffer buffer,
			final int bufferSampleOffset,
			final int numSamples,
			final SoundfileDisplayBuffer otherBuffer )
	{
//		log.debug("DeltaFill asked to paint(" + numToPaint + ") at displayOffset(" + displayOffset + ") paintOffset(" + paintOffset + ")");

		final Graphics2D g = buffer.g;

		g.setColor(SoundfilePlayerColorDefines.WAVE_DISPLAY_WAVE_FG_COLOR );

		int numPixelsDone = 0;
		switch( update.getDirection() )
		{
			case BACKWARDS:
			{
//				g.setColor(Color.RED );
				final int offsetToBackwardEdge = numSamplesPerPixel * (displayWidthMinusOneOverTwo + numSamples );

				final long bufferIndex = lastBufferPos;
				int bufferRemainder = -offsetToBackwardEdge;

//				log.debug("Delta backwards starting from bufferIndex(" + bufferIndex + ")");

				final long preIndexInt = bufferIndex + bufferRemainder - numSamplesPerPixel;
//				log.debug("So previous bottom value was as index " + preIndexInt + " due to pos(" + bufferIndex + ")-(" +
//						(bufferRemainder + numSamplesPerPixel ) + ")");
				calcMinMaxForSamples( preIndexInt );
				previousMinValue = minValue;
				previousMaxValue = maxValue;

				for( ; numPixelsDone < numSamples ; bufferRemainder += numSamplesPerPixel, ++numPixelsDone )
				{
					final long indexInt = bufferIndex + bufferRemainder;
//					log.debug("Backwards paint pixel (" + numPixelsDone + ") from index(" + indexInt +")");

					calcMinMaxForSamples( indexInt );
					extendMinMaxWithPrevious();

					fillInMinMaxLine(g, bufferSampleOffset + numPixelsDone, minValue, maxValue);

					previousMinValue = minValue;
					previousMaxValue = maxValue;
				}
				final int numSamplesToMove = numPixelsDone * numSamplesPerPixel;
//				log.debug("Moving position (" + lastBufferPos + ") backward " + numSamplesToMove + " samples");
				lastBufferPos -= numSamplesToMove;
				break;
			}
			default:
			{
//				g.setColor(Color.GREEN );

				final int offsetToForwardEdge = numSamplesPerPixel * (displayWidthMinusOneOverTwo + 1);

				final long bufferIndex = lastBufferPos;
				int bufferRemainder = offsetToForwardEdge;

//				log.debug("Delta forwards starting from bufferIndex(" + bufferIndex + ")");

				// Fill in previous min max from pixel before (rough is good enough)
				final long preIndexInt = bufferIndex + bufferRemainder - numSamplesPerPixel;
//				log.debug("So previous top value was as index " + preIndexInt + " due to pos(" + bufferIndex + ")-(" +
//						(bufferRemainder - numSamplesPerPixel ) + ")");
				calcMinMaxForSamples(preIndexInt);
				previousMinValue = minValue;
				previousMaxValue = maxValue;

				for( ; numPixelsDone < numSamples ; bufferRemainder += numSamplesPerPixel, ++numPixelsDone )
				{
					final long indexInt = bufferIndex + bufferRemainder;
//					log.debug("Forwards paint pixel (" + numPixelsDone + ") from index(" + indexInt +")");

					calcMinMaxForSamples( indexInt );
					extendMinMaxWithPrevious();

					final int xOffset = bufferSampleOffset + numPixelsDone;
					fillInMinMaxLine(g, xOffset, minValue, maxValue);

					previousMinValue = minValue;
					previousMaxValue = maxValue;
				}

				final int numSamplesToMove = numPixelsDone * numSamplesPerPixel;

//				log.debug("Moving position (" + lastBufferPos + ") forward " + numSamplesToMove + " samples");

				lastBufferPos += numSamplesToMove;

				break;
			}
		}

//		log.debug("Delta fill did " + numPixelsDone );
	}

	@Override
	public RollPaintUpdate getPaintUpdate()
	{
		final int numPixelsAvailableInt = getNumSamplesAvailable();
		updateStructure.setDirection( (numPixelsAvailableInt > 0 ? RollPaintDirection.FORWARDS : RollPaintDirection.BACKWARDS ) );
		final int absNpa = Math.abs( numPixelsAvailableInt );
		updateStructure.setNumSamplesInUpdate( numPixelsAvailableInt );

		if( absNpa == 0 && !needsFullUpdate )
		{
			updateStructure.setUpdateType( RollPaintUpdateType.NONE );
		}
		else if( absNpa > displayWidth ||
				absNpa > 0 && numSamplesPerPixel < 4.0f )
		{
			resetForFullRepaint();
		}

		if( needsFullUpdate )
		{
			needsFullUpdate = false;
			updateStructure.setUpdateType( RollPaintUpdateType.FULL );
		}
		else
		{
			if( absNpa > 0 )
			{
				updateStructure.setUpdateType( RollPaintUpdateType.DELTA );
			}
			else
			{
				updateStructure.setUpdateType( RollPaintUpdateType.NONE );
			}
		}

		return updateStructure;
	}

	@Override
	public SoundfileDisplayBufferClearer getBufferClearer()
	{
		return bufferClearer;
	}

	@Override
	public SoundfileDisplayBuffer createBuffer( final int bufNum )
			throws DatastoreException
	{
		return new SoundfileDisplayBuffer( bufNum, bia, displayWidth, displayHeight, allocationMatch );
	}

	@Override
	public void freeBuffer( final SoundfileDisplayBuffer bufferToFree )
			throws DatastoreException
	{
		bia.freeBufferedImage( bufferToFree.tbi );
	}

	public void setSampleCacheClient( final SampleCacheClient scc )
	{
		this.scc = scc;
		receivedBufferPos = scc.getCurrentFramePosition();
		computeSamplesPerPixel();
		resetForFullRepaint();
	}

	private void computeSamplesPerPixel()
	{
		if( scc == null )
		{
			numSamplesPerPixel = DEFAULT_SAMPLES_PER_PIXEL;
		}
		else
		{
			final int sccSampleRate = scc.getSampleRate();
			final int numTotalSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sccSampleRate, captureLengthMillis );
			this.numSamplesPerPixel = (int)(numTotalSamples / (float)displayWidth );
		}
//		log.debug("Reset num samples per pixel to " + numSamplesPerPixel );
	}

	public void setCaptureLengthMillis( final float captureLengthMillis )
	{
		this.captureLengthMillis = captureLengthMillis;
		computeSamplesPerPixel();
		resetForFullRepaint();
	}

	public void resetForFullRepaint()
	{
		lastBufferPos = receivedBufferPos;
		needsFullUpdate = true;
//		log.debug("Resetting for full repaint");
	}

	public void setCurrentPosition( final long newPosition )
	{
		receivedBufferPos = newPosition;
	}

	private long getNumReadable()
	{
		// Need the sign, too as that indicates direction
		return receivedBufferPos - lastBufferPos;
	}

	private void calcMinMaxForSamples( final long sampleStartIndex )
	{
		minMaxSampleAcceptor.reset();
		long endIndex = sampleStartIndex + numSamplesPerPixel;

		if( numSamplesPerPixel < 1.0f )
		{
			endIndex = sampleStartIndex + 1;
		}

		final RealtimeMethodReturnCodeEnum retCode = sampleCachingService.readSamplesInBlocksForCacheClient( scc,
				sampleStartIndex,
				(int)(endIndex - sampleStartIndex),
				minMaxSampleAcceptor );

		if( retCode != RealtimeMethodReturnCodeEnum.SUCCESS )
		{
			log.error("Failed during min max fetch of sample blocks using acceptor");
		}

		minValue = minMaxSampleAcceptor.minMaxValues[0];
		maxValue = minMaxSampleAcceptor.minMaxValues[1];
	}

	private void extendMinMaxWithPrevious()
	{
		if( previousMaxValue < minValue )
		{
			minValue = previousMaxValue;
		}
		if( previousMinValue > maxValue )
		{
			maxValue = previousMinValue;
		}
	}

	private void fillInMinMaxLine( final Graphics2D g, final int pixelX, final float minValue, final float maxValue )
	{
		final float multiplier = displayHeight / 2.0f;

		final int yMinVal = (int)(-(minValue * displayMultiplier) * multiplier + multiplier);
		final int yMaxVal = (int)(-(maxValue * displayMultiplier) * multiplier + multiplier);
		g.drawLine(pixelX, yMinVal, pixelX, yMaxVal);
	}

	public void setDisplayMultiplier( final float displayMultiplier )
	{
		this.displayMultiplier = displayMultiplier;
	}
}
