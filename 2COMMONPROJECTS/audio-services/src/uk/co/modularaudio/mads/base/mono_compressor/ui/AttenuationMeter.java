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

package uk.co.modularaudio.mads.base.mono_compressor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.exception.DatastoreException;

public class AttenuationMeter extends PacPanel
{
	public static final int PREFERRED_WIDTH = 10;
	public static final int PREFERRED_METER_WIDTH = PREFERRED_WIDTH - 2;

	private static final Color OVER_THRESHOLD_COLOR = new Color( 0.4f, 0.8f, 0.4f );

	private static final long serialVersionUID = -7723883774839586874L;

	private static Log log = LogFactory.getLog( AttenuationMeter.class.getName() );

	private float currentMeterValueDb = 0.0f;
	private float previouslyPaintedMeterValueDb = 0.0f;

	private long maxValueTimestamp = 0;
	private float currentMinValueDb = 0.0f;
	private float previouslyPaintedMinValueDb = 0.0f;

	private final MonoCompressorMadUiInstance uiInstance;

	private final DbToLevelComputer dbToLevelComputer;

	private final BufferedImageAllocator bufferedImageAllocator;
	private TiledBufferedImage tiledBufferedImage;
	private BufferedImage outBufferedImage;
	private Graphics outBufferedImageGraphics;

	private int componentWidth = -1;
	private int componentHeight = -1;

	public AttenuationMeter( final MonoCompressorMadUiInstance uiInstance,
			final DbToLevelComputer dbToLevelComputer, final BufferedImageAllocator bia )
	{
		setOpaque( true );
		this.uiInstance = uiInstance;
		this.dbToLevelComputer = dbToLevelComputer;
		this.bufferedImageAllocator = bia;

		setBackground( Color.black );
		final Dimension myPreferredSize = new Dimension(PREFERRED_WIDTH,100);
		this.setPreferredSize( myPreferredSize );
	}

	@Override
	public void paint( final Graphics g )
	{
		checkBufferedImage();
		if( outBufferedImage != null )
		{
			g.drawImage( outBufferedImage, 0,  0, null );
		}
	}

	private void refillMeterImage()
	{
//		log.debug("Repainting it.");
		checkBufferedImage();

		if( outBufferedImageGraphics != null )
		{
			outBufferedImageGraphics.setColor( Color.BLACK );
			outBufferedImageGraphics.fillRect( 0,  0, componentWidth, componentHeight );

			final int meterWidth = PREFERRED_METER_WIDTH;
			final int totalMeterHeight = componentHeight - 2;

			final int meterHeight = totalMeterHeight;
			final int meterHeightOffset = 0;

			final int yReverser = meterHeight + 1;

			float levelValue = 0.0f;
			if( currentMeterValueDb != Float.NEGATIVE_INFINITY )
			{
				levelValue = calcLevelValueFromDb( currentMeterValueDb );
			}

			outBufferedImageGraphics.setColor( OVER_THRESHOLD_COLOR );
			final float underVal = levelValue;
			int underBarHeightInPixels = (int)(underVal * meterHeight );
			underBarHeightInPixels = (underBarHeightInPixels > (meterHeight) ? (meterHeight) : (underBarHeightInPixels < 0 ? 0 : underBarHeightInPixels ));
//			int underStartY = meterHeight - underBarHeightInPixels + 1 + meterHeightOffset;
//			outBufferedImageGraphics.fillRect( 3, underStartY, meterWidth - 4, underBarHeightInPixels );
			final int underStartY = underBarHeightInPixels + 1 + meterHeightOffset;
			outBufferedImageGraphics.fillRect( 3, 1, meterWidth - 4, meterHeight - underStartY );

			float minLevelValue = 0.0f;
			final Color maxDbColor = OVER_THRESHOLD_COLOR;
			if( currentMinValueDb != Float.NEGATIVE_INFINITY )
			{
				minLevelValue = calcLevelValueFromDb( currentMinValueDb );
			}
			outBufferedImageGraphics.setColor( maxDbColor );

			int minValueHeightInPixels = (int)(minLevelValue * meterHeight);
			minValueHeightInPixels = (minValueHeightInPixels > (meterHeight) ? (meterHeight) : (minValueHeightInPixels < 0 ? 0 : minValueHeightInPixels ));
			final int minStartY = yReverser - minValueHeightInPixels + meterHeightOffset;
			outBufferedImageGraphics.drawLine( 1, minStartY, meterWidth, minStartY );

	//		outBufferedImage.flush();
		}
	}

	private void checkBufferedImage()
	{
		componentWidth = getWidth();
		componentHeight = getHeight();
		if( componentWidth > 0 && componentHeight > 0 )
		{
			if( outBufferedImage == null || (outBufferedImage.getWidth() != componentWidth || outBufferedImage.getHeight() != componentHeight ) )
			{
				try
				{
					final AllocationMatch myAllocationMatch = new AllocationMatch();
					tiledBufferedImage = bufferedImageAllocator.allocateBufferedImage( "AmpMeter",
							myAllocationMatch,
							AllocationLifetime.SHORT,
							AllocationBufferType.TYPE_INT_RGB,
							componentWidth,
							componentHeight );
					outBufferedImage = tiledBufferedImage.getUnderlyingBufferedImage();
					outBufferedImageGraphics = outBufferedImage.createGraphics();
					outBufferedImageGraphics.setColor( Color.BLACK );
					outBufferedImageGraphics.fillRect( 0, 0, componentWidth, componentHeight );
				}
				catch( final DatastoreException e)
				{
					final String msg = "Unable to allocation image for meter: " + e.toString();
					log.error( msg, e );
				}
			}
		}
	}

	public void receiveDisplayTick( final long currentTime )
	{
		final boolean showing = isShowing();
		if( currentMeterValueDb < currentMinValueDb )
		{
			currentMinValueDb = currentMeterValueDb;
			maxValueTimestamp = currentTime;
		}
		else if( maxValueTimestamp + uiInstance.framesBetweenPeakReset < currentTime )
		{
			currentMinValueDb = currentMeterValueDb;
			maxValueTimestamp = currentTime;
		}

		if( showing )
		{
			if( currentMeterValueDb != previouslyPaintedMeterValueDb ||
					currentMinValueDb != previouslyPaintedMinValueDb )
			{
				refillMeterImage();
				repaint();
				previouslyPaintedMeterValueDb = currentMeterValueDb;
				previouslyPaintedMinValueDb = currentMinValueDb;
			}
		}
	}

	public void receiveMeterReadingInDb( final long currentFrameTime, final float meterReadingDb )
	{
		currentMeterValueDb = meterReadingDb;
	}

	public float calcLevelValueFromDb( final float dbIn )
	{
//		return (dbIn + FirstDbToLevelComputer.LOWEST_NEGATIVE_DB ) / FirstDbToLevelComputer.LOWEST_NEGATIVE_DB;
//		return (dbIn + -DbToLevelComputer.LOWEST_DIGITAL_DB_RATING ) / -DbToLevelComputer.LOWEST_DIGITAL_DB_RATING;
		return dbToLevelComputer.toNormalisedSliderLevelFromDb( dbIn );
	}

	public void destroy()
	{
		if( tiledBufferedImage != null )
		{
			try
			{
				bufferedImageAllocator.freeBufferedImage( tiledBufferedImage );
			}
			catch( final Exception e )
			{
				final String msg = "Failed to free up allocated image: " + e.toString();
				log.error( msg );
			}
			tiledBufferedImage = null;
			outBufferedImage = null;
			outBufferedImageGraphics = null;
		}
	}
}
