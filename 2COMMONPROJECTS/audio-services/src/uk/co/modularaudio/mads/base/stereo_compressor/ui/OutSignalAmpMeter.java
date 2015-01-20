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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

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

public class OutSignalAmpMeter extends PacPanel
{
	public static final int PREFERRED_WIDTH = 10;
	public static final int PREFERRED_METER_WIDTH = PREFERRED_WIDTH - 2;
	
	private static final float GREEN_THRESHOLD_DB = -6.0f;
	private static final float ORANGE_THRESHOLD_DB = -3.0f;

	private float greenThresholdLevel = 0.0f;
	private float orangeThreholdLevel = 0.0f;

	private static final long serialVersionUID = -7723883774839586874L;

	private static Log log = LogFactory.getLog( OutSignalAmpMeter.class.getName() );
	
	private boolean showClipBox = false;
	
	private float currentMeterValueDb = Float.NEGATIVE_INFINITY;
	private float previouslyPaintedMeterValueDb = Float.NEGATIVE_INFINITY;

	private long maxValueTimestamp = 0;
	private float currentMaxValueDb = Float.NEGATIVE_INFINITY;
	private float previouslyPaintedMaxValueDb = Float.NEGATIVE_INFINITY;
	
	private final StereoCompressorMadUiInstance uiInstance;
	
	private DbToLevelComputer dbToLevelComputer = null;
	
	private BufferedImageAllocator bufferedImageAllocator = null;
	private TiledBufferedImage tiledBufferedImage = null;
	private BufferedImage outBufferedImage = null;
	private Graphics outBufferedImageGraphics = null;	
	
	private int componentWidth = -1;
	private int componentHeight = -1;
	
	public OutSignalAmpMeter( StereoCompressorMadUiInstance uiInstance,
			DbToLevelComputer dbToLevelComputer, BufferedImageAllocator bia, boolean showClipBox )
	{
		setOpaque( true );
		this.uiInstance = uiInstance;
		this.dbToLevelComputer = dbToLevelComputer;
		this.bufferedImageAllocator = bia;
		
		greenThresholdLevel = dbToLevelComputer.toNormalisedSliderLevelFromDb( GREEN_THRESHOLD_DB );
		orangeThreholdLevel = dbToLevelComputer.toNormalisedSliderLevelFromDb( ORANGE_THRESHOLD_DB );
		
		setBackground( Color.black );
		Dimension myPreferredSize = new Dimension(PREFERRED_WIDTH,100);
		this.setPreferredSize( myPreferredSize );
		
		this.showClipBox = showClipBox;
	}
	
	private Color getColorForDb( float dbValue )
	{
		if( dbValue == Float.NEGATIVE_INFINITY )
		{
			return Color.green;
		}
		else if( dbValue > ORANGE_THRESHOLD_DB )
		{
			return Color.RED;
		}
		else if( dbValue > GREEN_THRESHOLD_DB )
		{
			return Color.orange;
		}
		else
		{
			return Color.green;
		}
	}
	
	public void paint( Graphics g )
	{
		if( outBufferedImage != null )
		{
			g.drawImage( outBufferedImage, 0,  0, null );
		}
	}

	private void refillMeterImage()
	{
//		log.debug("Repainting it.");
		if( outBufferedImage != null )
		{
			
			outBufferedImageGraphics.setColor( Color.BLACK );
			outBufferedImageGraphics.fillRect( 0,  0, componentWidth, componentHeight );
			
			int meterWidth = PREFERRED_METER_WIDTH;
			int totalMeterHeight = componentHeight - 2;
			
			int meterHeight = (showClipBox ? totalMeterHeight - meterWidth : totalMeterHeight );
			int meterHeightOffset = ( showClipBox ? meterWidth : 0 );
			
			float levelValue = 0.0f;
			if( currentMeterValueDb != Float.NEGATIVE_INFINITY )
			{
				levelValue = dbToLevelComputer.toNormalisedSliderLevelFromDb( currentMeterValueDb );
			}
	
			outBufferedImageGraphics.setColor( Color.GREEN );
			float greenVal = (levelValue >= greenThresholdLevel ? greenThresholdLevel : levelValue );
			int greenBarHeightInPixels = (int)(greenVal * meterHeight );
			greenBarHeightInPixels = (greenBarHeightInPixels > (meterHeight) ? (meterHeight) : (greenBarHeightInPixels < 0 ? 0 : greenBarHeightInPixels ));
			int greenStartY = meterHeight - greenBarHeightInPixels + 1 + meterHeightOffset;
			outBufferedImageGraphics.fillRect( 3, greenStartY, meterWidth - 4, greenBarHeightInPixels );
			
			if( currentMeterValueDb > GREEN_THRESHOLD_DB )
			{
				outBufferedImageGraphics.setColor( Color.orange );
				float orangeVal = (levelValue >= orangeThreholdLevel ? orangeThreholdLevel : levelValue );
				int orangeBarHeightInPixels = (int)(orangeVal * meterHeight );
				orangeBarHeightInPixels = (orangeBarHeightInPixels > (meterHeight) ? (meterHeight) : (orangeBarHeightInPixels < 0 ? 0 : orangeBarHeightInPixels ));
				// Take off the green
				orangeBarHeightInPixels -= greenBarHeightInPixels;
				int orangeStartY = greenStartY - orangeBarHeightInPixels;
	//			int orangeEndY = greenStartY;
				outBufferedImageGraphics.fillRect( 3, orangeStartY, meterWidth - 4, orangeBarHeightInPixels );
	
				if( currentMeterValueDb > ORANGE_THRESHOLD_DB )
				{
					outBufferedImageGraphics.setColor( Color.RED );
					float redVal = levelValue;
					int redBarHeightInPixels = (int)(redVal * meterHeight );
					redBarHeightInPixels = (redBarHeightInPixels > (meterHeight) ? (meterHeight) : (redBarHeightInPixels < 0 ? 0 : redBarHeightInPixels ));
					// Take off the green and orange
					redBarHeightInPixels = redBarHeightInPixels - (greenBarHeightInPixels + orangeBarHeightInPixels );
					int redStartY = orangeStartY - redBarHeightInPixels;
	//				int redEndY = orangeStartY;
					outBufferedImageGraphics.fillRect( 3, redStartY, meterWidth - 4, redBarHeightInPixels );
					
				}
			}
			
			float maxLevelValue = 0.0f;
			Color maxDbColor = getColorForDb( currentMaxValueDb );
			if( currentMaxValueDb != Float.NEGATIVE_INFINITY )
			{
				maxLevelValue = dbToLevelComputer.toNormalisedSliderLevelFromDb( currentMaxValueDb );
			}
			outBufferedImageGraphics.setColor( maxDbColor );
			
			int maxValueHeightInPixels = (int)(maxLevelValue * meterHeight);
			maxValueHeightInPixels = (maxValueHeightInPixels > (meterHeight) ? (meterHeight) : (maxValueHeightInPixels < 0 ? 0 : maxValueHeightInPixels ));
			int yReverser = meterHeight + 1;
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

//	public float calcLevelValueFromDb( float dbIn )
//	{
//		return dbToLevelComputer.toNormalisedSliderLevelFromDb( dbIn );
//	}
	
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
	
	@Override
	public void setBounds( int x, int y, int width, int height )
	{
		super.setBounds( x, y, width, height );
		try
		{
			if( tiledBufferedImage != null )
			{
				bufferedImageAllocator.freeBufferedImage( tiledBufferedImage );
			}

			AllocationMatch myAllocationMatch = new AllocationMatch();
			tiledBufferedImage = bufferedImageAllocator.allocateBufferedImage( "OutSignalAmpMeter",
					myAllocationMatch,
					AllocationLifetime.SHORT,
					AllocationBufferType.TYPE_INT_RGB,
					width,
					height );
			outBufferedImage = tiledBufferedImage.getUnderlyingBufferedImage();
			outBufferedImageGraphics = outBufferedImage.createGraphics();
			outBufferedImageGraphics.setColor( Color.BLACK );
			outBufferedImageGraphics.fillRect( 0, 0, componentWidth, componentHeight );
		}
		catch (DatastoreException e)
		{
			log.error("DatastoreException caught allocating buffered image: " + e.toString(), e );
		}
		componentWidth = width;
		componentHeight = height;
	}
}
