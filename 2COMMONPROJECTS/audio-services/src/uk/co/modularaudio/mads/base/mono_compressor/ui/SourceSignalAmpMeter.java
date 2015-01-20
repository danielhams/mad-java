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

import uk.co.modularaudio.util.audio.gui.paccontrols.PacPanel;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.exception.DatastoreException;

public class SourceSignalAmpMeter extends PacPanel
{
	public static final int PREFERRED_WIDTH = 10;
	public static final int PREFERRED_METER_WIDTH = PREFERRED_WIDTH - 2;
	
	private static final Color UNDER_THRESHOLD_COLOR = new Color( 0.4f, 0.4f, 0.4f );
	private static final Color OVER_THRESHOLD_COLOR = new Color( 0.8f, 0.8f, 0.8f );
	
	private static final Color MARK_THRESHOLD_COLOR = new Color( 0.8f, 0.4f, 0.4f );
	
	private float underThresholdLevel = 0.0f;

	private static final long serialVersionUID = -7723883774839586874L;

	private static Log log = LogFactory.getLog( SourceSignalAmpMeter.class.getName() );
	
	private boolean showClipBox = false;
	
	private float currentThresholdValueDb = Float.NEGATIVE_INFINITY;
	private float currentMeterValueDb = Float.NEGATIVE_INFINITY;
	private float previouslyPaintedMeterValueDb = Float.NEGATIVE_INFINITY;

	private long maxValueTimestamp = 0;
	private float currentMaxValueDb = Float.NEGATIVE_INFINITY;
	private float previouslyPaintedMaxValueDb = Float.NEGATIVE_INFINITY;
	
	private final MonoCompressorMadUiInstance uiInstance;
	
	private DbToLevelComputer dbToLevelComputer = null;
	
	private BufferedImageAllocator bufferedImageAllocator = null;
	private TiledBufferedImage tiledBufferedImage = null;
	private BufferedImage outBufferedImage = null;
	private Graphics outBufferedImageGraphics = null;	
	
	private int componentWidth = -1;
	private int componentHeight = -1;
	public SourceSignalAmpMeter( MonoCompressorMadUiInstance uiInstance,
			DbToLevelComputer dbToLevelComputer, BufferedImageAllocator bia, boolean showClipBox )
	{
		setOpaque( true );
		this.uiInstance = uiInstance;
		this.dbToLevelComputer = dbToLevelComputer;
		this.bufferedImageAllocator = bia;
		
		setBackground( Color.black );
		Dimension myPreferredSize = new Dimension(PREFERRED_WIDTH,100);
		this.setPreferredSize( myPreferredSize );
		
		this.showClipBox = showClipBox;
	}
	
	private Color getColorForDb( float dbValue )
	{
		if( dbValue == Float.NEGATIVE_INFINITY )
		{
			return UNDER_THRESHOLD_COLOR;
		}
		else if( dbValue <= currentThresholdValueDb )
		{
			return UNDER_THRESHOLD_COLOR;
		}
		else if( dbValue > currentThresholdValueDb )
		{
			return OVER_THRESHOLD_COLOR;
		}
		else
		{
			return Color.ORANGE;
		}
	}
	
	public void paint( Graphics g )
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
			
			int meterWidth = PREFERRED_METER_WIDTH;
			int totalMeterHeight = componentHeight - 2;
			
			int meterHeight = (showClipBox ? totalMeterHeight - meterWidth : totalMeterHeight );
			int meterHeightOffset = ( showClipBox ? meterWidth : 0 );
	
			underThresholdLevel = calcLevelValueFromDb( currentThresholdValueDb );
			
			int yReverser = meterHeight + 1;
			
			// Draw the two little marks indicating where the current threshold is
			int thresholdHeightInPixels = (int)(underThresholdLevel * meterHeight);
			thresholdHeightInPixels = (thresholdHeightInPixels > (meterHeight) ? (meterHeight) : (thresholdHeightInPixels < 0 ? 0 : thresholdHeightInPixels ));
			int thresholdStartY = yReverser - thresholdHeightInPixels + meterHeightOffset;
			outBufferedImageGraphics.setColor( MARK_THRESHOLD_COLOR );
			outBufferedImageGraphics.drawLine( 0, thresholdStartY, meterWidth + 2, thresholdStartY );
			
			float levelValue = 0.0f;
			if( currentMeterValueDb != Float.NEGATIVE_INFINITY )
			{
				levelValue = calcLevelValueFromDb( currentMeterValueDb );
			}
	
			outBufferedImageGraphics.setColor( UNDER_THRESHOLD_COLOR );
			float underVal = (levelValue >= underThresholdLevel ? underThresholdLevel : levelValue );
			int underBarHeightInPixels = (int)(underVal * meterHeight );
			underBarHeightInPixels = (underBarHeightInPixels > (meterHeight) ? (meterHeight) : (underBarHeightInPixels < 0 ? 0 : underBarHeightInPixels ));
			int underStartY = meterHeight - underBarHeightInPixels + 1 + meterHeightOffset;
			outBufferedImageGraphics.fillRect( 3, underStartY, meterWidth - 4, underBarHeightInPixels );
			
			if( currentMeterValueDb > currentThresholdValueDb )
			{
				outBufferedImageGraphics.setColor( OVER_THRESHOLD_COLOR );
				float overVal = levelValue;
				int overBarHeightInPixels = (int)(overVal * meterHeight );
				overBarHeightInPixels = (overBarHeightInPixels > (meterHeight) ? (meterHeight) : (overBarHeightInPixels < 0 ? 0 : overBarHeightInPixels ));
				overBarHeightInPixels = overBarHeightInPixels - underBarHeightInPixels;
				int overStartY = underStartY - overBarHeightInPixels;
				outBufferedImageGraphics.fillRect( 3, overStartY, meterWidth - 4, overBarHeightInPixels );			
			}
			
			float maxLevelValue = 0.0f;
			Color maxDbColor = getColorForDb( currentMaxValueDb );
			if( currentMaxValueDb != Float.NEGATIVE_INFINITY )
			{
				maxLevelValue = calcLevelValueFromDb( currentMaxValueDb );
			}
			outBufferedImageGraphics.setColor( maxDbColor );
			
			int maxValueHeightInPixels = (int)(maxLevelValue * meterHeight);
			maxValueHeightInPixels = (maxValueHeightInPixels > (meterHeight) ? (meterHeight) : (maxValueHeightInPixels < 0 ? 0 : maxValueHeightInPixels ));
			int maxStartY = yReverser - maxValueHeightInPixels + meterHeightOffset;
			outBufferedImageGraphics.drawLine( 1, maxStartY, meterWidth, maxStartY );
			
			if( showClipBox )
			{
				if( currentMaxValueDb >= 1.0f )
				{
					// Should already be the right colour
	//				g.setColor( getColorForDb( 0.0f ) );
					outBufferedImageGraphics.fillRect( 1, 1, meterWidth, meterWidth - 1 );
				}
			}
			else
			{
			}
			
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
					AllocationMatch myAllocationMatch = new AllocationMatch();
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
				catch( DatastoreException e)
				{
					String msg = "Unable to allocation image for meter: " + e.toString();
					log.error( msg, e );
				}
			}
		}
	}
	
	public void receiveDisplayTick( long currentTime )
	{
		boolean showing = isShowing();
		if( currentMeterValueDb > currentMaxValueDb )
		{
			currentMaxValueDb = currentMeterValueDb;
			maxValueTimestamp = currentTime;
		}
		else if( maxValueTimestamp + uiInstance.framesBetweenPeakReset < currentTime )
		{
			currentMaxValueDb = currentMeterValueDb;
			maxValueTimestamp = currentTime;
		}
		
		if( showing )
		{
			if( currentMeterValueDb != previouslyPaintedMeterValueDb ||
					currentMaxValueDb != previouslyPaintedMaxValueDb )
			{
				refillMeterImage();
				repaint();
				previouslyPaintedMeterValueDb = currentMeterValueDb;
				previouslyPaintedMaxValueDb = currentMaxValueDb;
			}
		}
	}

	public void receiveMeterReadingInDb( long currentFrameTime, float meterReadingDb )
	{
		currentMeterValueDb = meterReadingDb;
	}

	public float calcLevelValueFromDb( float dbIn )
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
			catch( Exception e )
			{
				String msg = "Failed to free up allocated image: " + e.toString();
				log.error( msg );
			}
			tiledBufferedImage = null;
			outBufferedImage = null;
			outBufferedImageGraphics = null;
		}
	}

	public void setThresholdDb( float newThresholdDb )
	{
//		log.debug("Amp meter received new threshold db: " + newThresholdDb );
		this.currentThresholdValueDb = newThresholdDb;
		this.refillMeterImage();
		this.repaint();
	}
}
