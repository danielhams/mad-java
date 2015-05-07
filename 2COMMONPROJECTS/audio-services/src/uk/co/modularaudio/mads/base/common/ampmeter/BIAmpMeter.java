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

package uk.co.modularaudio.mads.base.common.ampmeter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownMeterIntToFloatConverter;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownMeterModel;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.exception.DatastoreException;

public class BIAmpMeter	extends PacPanel implements AmpMeter
{
	private static final long serialVersionUID = -7723883774839586874L;

	private static Log log = LogFactory.getLog( BIAmpMeter.class.getName() );

	private final boolean showClipBox;

	public static final MixdownMeterModel METER_MODEL = new MixdownMeterModel();
	public static final MixdownMeterIntToFloatConverter INT_TO_FLOAT_CONVERTER = new MixdownMeterIntToFloatConverter();

	private final BufferedImageAllocator bufferedImageAllocator;
	private TiledBufferedImage tiledBufferedImage;
	private BufferedImage outBufferedImage;
	private Graphics outBufferedImageGraphics;

	private int componentWidth;
	private int componentHeight;

	private final float numTotalSteps;
	private final float greenThresholdLevel;
	private final float orangeThresholdLevel;
	private int numPixelsInMeter;
	private int meterHeightOffset;
	private int numGreenPixels;
	private int numOrangePixels;
	private int numRedPixels;

	private float currentMeterValueDb = Float.NEGATIVE_INFINITY;
	private float previouslyPaintedMeterValueDb = Float.POSITIVE_INFINITY;

	private long maxValueTimestamp = 0;
	private float currentMaxValueDb = Float.NEGATIVE_INFINITY;
	private float previouslyPaintedMaxValueDb = Float.POSITIVE_INFINITY;

	private int framesBetweenPeakReset = DataRate.SR_44100.getValue();

	public BIAmpMeter( final BufferedImageAllocator bia,
			final boolean showClipBox )
	{
		this.setOpaque( true );

		this.bufferedImageAllocator = bia;

		numTotalSteps = INT_TO_FLOAT_CONVERTER.getNumTotalSteps();

		greenThresholdLevel = INT_TO_FLOAT_CONVERTER.toSliderIntFromDb( AmpMeter.GREEN_THRESHOLD_DB )
				/ numTotalSteps;
		orangeThresholdLevel = INT_TO_FLOAT_CONVERTER.toSliderIntFromDb( AmpMeter.ORANGE_THRESHOLD_DB )
				/ numTotalSteps;

		setBackground( Color.black );
		final Dimension myPreferredSize = new Dimension(AmpMeter.PREFERRED_WIDTH,100);
		this.setPreferredSize( myPreferredSize );
		this.setMinimumSize( myPreferredSize );

		this.showClipBox = showClipBox;
	}

	private Color getColorForDb( final float dbValue )
	{
		if( dbValue == Float.NEGATIVE_INFINITY )
		{
			return Color.green;
		}
		else if( dbValue > AmpMeter.ORANGE_THRESHOLD_DB )
		{
			return Color.RED;
		}
		else if( dbValue > AmpMeter.GREEN_THRESHOLD_DB )
		{
			return Color.orange;
		}
		else
		{
			return Color.green;
		}
	}

	@Override
	public void paint( final Graphics g )
	{
		if( outBufferedImage != null )
		{
			g.drawImage( outBufferedImage, 0,  0, null );
		}
	}

	private void refillMeterImage()
	{
		if( outBufferedImage != null )
		{
			outBufferedImageGraphics.setColor( Color.BLACK );
			outBufferedImageGraphics.fillRect( 0,  0, componentWidth, componentHeight );

			final int meterWidth = AmpMeter.PREFERRED_METER_WIDTH;

			final int sliderIntValue = INT_TO_FLOAT_CONVERTER.floatValueToSliderIntValue( METER_MODEL, currentMeterValueDb );
			final float normalisedlevelValue = (sliderIntValue / numTotalSteps);
			final int numPixelsHigh = (int)(normalisedlevelValue * numPixelsInMeter);

			outBufferedImageGraphics.setColor( Color.GREEN );
			final int greenBarHeightInPixels = (numPixelsHigh > numGreenPixels ? numGreenPixels : numPixelsHigh );

			final int greenStartY = numPixelsInMeter - greenBarHeightInPixels + 1 + meterHeightOffset;
			outBufferedImageGraphics.fillRect( 3, greenStartY, meterWidth - 4, greenBarHeightInPixels );

			int pixelsLeft = numPixelsHigh - greenBarHeightInPixels;

			final int orangeBarHeightInPixels = (pixelsLeft > numOrangePixels ? numOrangePixels : pixelsLeft );

			final int orangeStartY = greenStartY - orangeBarHeightInPixels;
			if( orangeBarHeightInPixels > 0 )
			{
				outBufferedImageGraphics.setColor( Color.ORANGE );
				outBufferedImageGraphics.fillRect( 3, orangeStartY, meterWidth - 4, orangeBarHeightInPixels );
			}

			pixelsLeft -= orangeBarHeightInPixels;
			final int redBarHeightInPixels = (pixelsLeft > numRedPixels ? numRedPixels : pixelsLeft );

			if( redBarHeightInPixels > 0 )
			{
				outBufferedImageGraphics.setColor( Color.RED );
				final int redStartY = orangeStartY - redBarHeightInPixels;
				outBufferedImageGraphics.fillRect( 3, redStartY, meterWidth - 4, redBarHeightInPixels );
			}

			pixelsLeft -= redBarHeightInPixels;

			// Slightly larger bar across the top for the sticky max db
			final float maxNormalisedValue = ( currentMaxValueDb >= 0.0f ? 1.0f :
				INT_TO_FLOAT_CONVERTER.toSliderIntFromDb( currentMaxValueDb ) /	numTotalSteps );
			final int maxBarPixelsHigh = (int)(maxNormalisedValue * numPixelsInMeter);
			final Color maxDbColor = getColorForDb( currentMaxValueDb );
			outBufferedImageGraphics.setColor( maxDbColor );

			final int yReverser = numPixelsInMeter + 1;
			final int maxStartY = yReverser - maxBarPixelsHigh + meterHeightOffset;
			outBufferedImageGraphics.drawLine( 1, maxStartY, meterWidth, maxStartY );

			if( showClipBox )
			{
				if( currentMaxValueDb >= 0.0f )
				{
					// Should already be the right colour
					outBufferedImageGraphics.fillRect( 1, 1, meterWidth, meterWidth - 1 );
				}
			}
		}
	}

	@Override
	public void receiveDisplayTick( final long currentTime )
	{
		final boolean showing = isShowing();
		if( currentMeterValueDb > currentMaxValueDb )
		{
			currentMaxValueDb = currentMeterValueDb;
			maxValueTimestamp = currentTime;
		}
		else if( (maxValueTimestamp + framesBetweenPeakReset ) < currentTime )
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

	@Override
	public void receiveMeterReadingInDb( final long currentTimestamp, final float meterReadingDb )
	{
		currentMeterValueDb = meterReadingDb;
	}

	@Override
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

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		try
		{
			if( tiledBufferedImage != null )
			{
				bufferedImageAllocator.freeBufferedImage( tiledBufferedImage );
			}

			final AllocationMatch myAllocationMatch = new AllocationMatch();
			tiledBufferedImage = bufferedImageAllocator.allocateBufferedImage( "AmpMeter",
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
		catch (final DatastoreException e)
		{
			if( log.isErrorEnabled() )
			{
				log.error("DatastoreException caught allocating buffered image: " + e.toString(), e );
			}
		}
		componentWidth = width;
		componentHeight = height;

		numPixelsInMeter = (showClipBox ? componentHeight - 2 - AmpMeter.PREFERRED_METER_WIDTH :
			componentHeight - 2);
		meterHeightOffset = ( showClipBox ? AmpMeter.PREFERRED_METER_WIDTH : 0 );
		numGreenPixels = (int)(numPixelsInMeter * greenThresholdLevel);
		numOrangePixels = (int)((numPixelsInMeter * orangeThresholdLevel) - numGreenPixels);
		numRedPixels = numPixelsInMeter - numGreenPixels - numOrangePixels;
	}

	@Override
	public void setFramesBetweenPeakReset( final int framesBetweenPeakReset )
	{
		this.framesBetweenPeakReset = framesBetweenPeakReset;
	}
}
