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

package uk.co.modularaudio.service.audioanalysis.impl.analysers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.service.hashedstorage.HashedStorageService;
import uk.co.modularaudio.service.hashedstorage.HashedWarehouse;
import uk.co.modularaudio.util.audio.format.DataRate;

public class StaticThumbnailGeneratorListener implements AnalysisListener
{
	private static final int BORDER_WIDTH = 0;

	private static Log log = LogFactory.getLog( StaticThumbnailGeneratorListener.class.getName() );

	private final int requiredWidth;
	private final int requiredHeight;

	private final Color minMaxColor;
	private final Color rmsColor;

	private final HashedStorageService hashedStorageService;
	private final HashedWarehouse hashedWarehouse;

	private int outIndex;
	private float[] thumbnailValues;
	private int numChannels;
	private float framesPerPixel;

	private double currentIndex;

	private final BufferedImage bufferedImage;
	private final Graphics2D og2d;

	private float minValue = 0.0f;
	private float maxValue = 0.0f;
	private float sumSq = 0.0f;

	public StaticThumbnailGeneratorListener( final int requiredWidth,
			final int requiredHeight,
			final Color minMaxColor,
			final Color rmsColor,
			final HashedStorageService hashedStorageService,
			final HashedWarehouse warehouse )
	{
		this.requiredWidth = requiredWidth;
		this.requiredHeight = requiredHeight;
		this.minMaxColor = minMaxColor;
		this.rmsColor = rmsColor;

		this.hashedStorageService = hashedStorageService;
		this.hashedWarehouse = warehouse;

		bufferedImage = new BufferedImage( requiredWidth, requiredHeight, BufferedImage.TYPE_INT_ARGB );
		og2d = bufferedImage.createGraphics();
	}

	@Override
	public void start(final DataRate dataRate, final int numChannels, final long totalFrames )
	{
		this.numChannels = numChannels;

		// 3 floats per sample - min and max and rms
		final int numPixels = requiredWidth;
		thumbnailValues = new float[ numPixels * 3 ];
		this.framesPerPixel = (totalFrames + 0.0f) / (numPixels + 0.0f);
		outIndex = 0;
		currentIndex = 0;
		minValue = 0.0f;
		maxValue = 0.0f;
		sumSq = 0.0f;
	}

	@Override
	public void receiveFrames( final float[] data, final int numFrames )
	{
		for( int f = 0 ; f < numFrames ; ++f )
		{
			final float curSample = data[f*numChannels];
			if( curSample > maxValue )
			{
				maxValue = curSample;
			}
			if( curSample < minValue )
			{
				minValue = curSample;
			}
			// The times two just makes the rms more "visible"
			sumSq = sumSq + ((curSample * curSample) * 2);
			currentIndex++;
			if( currentIndex >= framesPerPixel )
			{
				thumbnailValues[ outIndex++ ] = minValue;
				thumbnailValues[ outIndex++ ] = maxValue;
				thumbnailValues[ outIndex++ ] = (float)Math.sqrt( sumSq / framesPerPixel );
				minValue = maxValue = sumSq = 0.0f;
				currentIndex -= framesPerPixel;
			}
		}
	}

	@Override
	public void end()
	{
		log.debug("End called. Should generate buffered image.");

		final int width = requiredWidth - (2 * BORDER_WIDTH);
		final int height = requiredHeight - (2 * BORDER_WIDTH );

		final Graphics2D g2d = (Graphics2D)og2d.create( BORDER_WIDTH, BORDER_WIDTH, width, height );

//		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.setColor( Color.BLACK );
		g2d.fillRect( 0, 0, width, height );

		g2d.setColor( minMaxColor );

		int curDataCount = 0;

		for( int i = 0 ; i < width ; i++ )
		{
			final float minSample = thumbnailValues[ curDataCount++ ];
			final float maxSample = thumbnailValues[ curDataCount++ ];
			curDataCount++;
//			float rms = thumbnailValues[ curDataCount++ ];

			// lSample goes from +1.0 to -1.0
			// We need it to go from 0 to height
			final int curX = i;
			final int startY = (int)( ((minSample + 1.0) / 2.0) * height);
			final int endY = (int)( ((maxSample + 1.0)  / 2.0) * height);
			g2d.drawLine( curX, startY, curX, endY );
		}

		g2d.setColor( rmsColor );

		curDataCount = 0;
		for( int i = 0 ; i < width ; i++ )
		{
//			float minSample = thumbnailValues[ curDataCount++ ];
//			float maxSample = thumbnailValues[ curDataCount++ ];
			curDataCount+=2;
			final float rms = thumbnailValues[ curDataCount++ ];

			// lSample goes from +1.0 to -1.0
			// We need it to go from 0 to height
			final int curX = i;
			final int startY = (int)( ((rms + 1.0) / 2.0) * height);
			final int endY = (int)( ((-rms + 1.0)  / 2.0) * height);
			g2d.drawLine( curX, startY, curX, endY );
		}
	}

	@Override
	public void updateAnalysedData(final AnalysedData analysedData, final HashedRef hashedRef )
	{
		try
		{
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final ImageOutputStream ios = ImageIO.createImageOutputStream( os );

			ImageIO.write( bufferedImage, "png", ios );
			final InputStream contents = new ByteArrayInputStream( os.toByteArray() );
			// Now save this generated thumb nail onto disk then pass the path to this in the analysed data
			hashedStorageService.storeContentsInWarehouse( hashedWarehouse, hashedRef, contents );

			analysedData.setPathToStaticThumbnail( hashedStorageService.getPathToHashedRef( hashedWarehouse, hashedRef ) );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught serialising static thumb nail: " + e.toString();
			log.error( msg, e );
		}
	}
}
