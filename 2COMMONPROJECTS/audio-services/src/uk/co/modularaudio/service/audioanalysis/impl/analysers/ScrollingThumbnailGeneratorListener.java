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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioanalysis.impl.analysers.thumbnail.ThumbnailGenerationRT;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.thumbnail.ThumbnailGenerator;
import uk.co.modularaudio.service.audioanalysis.vos.AnalysedData;
import uk.co.modularaudio.service.hashedstorage.HashedStorageService;
import uk.co.modularaudio.service.hashedstorage.vos.HashedRef;
import uk.co.modularaudio.service.hashedstorage.vos.HashedWarehouse;
import uk.co.modularaudio.util.audio.format.DataRate;

public class ScrollingThumbnailGeneratorListener implements AnalysisListener
{
	private static Log log = LogFactory.getLog( ScrollingThumbnailGeneratorListener.class.getName() );
	
	private int scrollingThumbnailSamplesPerPixelZoomedIn = -1;
	private int scrollingThumbnailSamplesPerPixelZoomedOut = -1;
	private int scrollingThumbnailHeight = -1;
	private Color scrollingMinMaxColor = null;
	private Color scrollingRmsColor = null;
	private HashedStorageService hashedStorageService = null;
	private HashedWarehouse ziScrollingThumbnailWarehouse = null;
	private HashedWarehouse zoScrollingThumbnailWarehouse = null;

	private ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();
	private ThumbnailGenerationRT ziRt = null;
	private ThumbnailGenerationRT zoRt = null;

	public ScrollingThumbnailGeneratorListener( int scrollingThumbnailSamplesPerPixelZoomedIn,
			int scrollingThumbnailSamplesPerPixelZoomedOut,
			int scrollingThumbnailHeight,
			Color scrollingMinMaxColor,
			Color scrollingRmsColor,
			HashedStorageService hashedStorageService,
			HashedWarehouse ziScrollingThumbnailWarehouse,
			HashedWarehouse zoScrollingThumbnailWarehouse )
	{
		this.scrollingThumbnailSamplesPerPixelZoomedIn = scrollingThumbnailSamplesPerPixelZoomedIn;
		this.scrollingThumbnailSamplesPerPixelZoomedOut = scrollingThumbnailSamplesPerPixelZoomedOut;
		this.scrollingThumbnailHeight  = scrollingThumbnailHeight;
		this.scrollingMinMaxColor = scrollingMinMaxColor;
		this.scrollingRmsColor = scrollingRmsColor;
		this.hashedStorageService = hashedStorageService;
		this.ziScrollingThumbnailWarehouse = ziScrollingThumbnailWarehouse;
		this.zoScrollingThumbnailWarehouse = zoScrollingThumbnailWarehouse;
	}
	
	@Override
	public void start( DataRate dataRate, int numChannels, long totalFloatsLength )
	{
		// Create the two thumbnail runtimes for the image
		ziRt = thumbnailGenerator.start( dataRate,
				numChannels,
				totalFloatsLength,
				scrollingThumbnailSamplesPerPixelZoomedIn,
				scrollingThumbnailHeight,
				scrollingMinMaxColor,
				scrollingRmsColor );

		zoRt = thumbnailGenerator.start( dataRate,
				numChannels,
				totalFloatsLength,
				scrollingThumbnailSamplesPerPixelZoomedOut,
				scrollingThumbnailHeight,
				scrollingMinMaxColor,
				scrollingRmsColor );
	}

	@Override
	public void receiveData(float[] data, int numRead )
	{
		thumbnailGenerator.receiveData( ziRt, data, numRead );
		thumbnailGenerator.receiveData( zoRt, data, numRead );
	}

	@Override
	public void end()
	{
		thumbnailGenerator.end( ziRt );
		thumbnailGenerator.end( zoRt );
	}

	@Override
	public void updateAnalysedData(AnalysedData analysedData, HashedRef hashedRef )
	{
		try
		{
			// Zoomed in
			ByteArrayOutputStream ziOs = new ByteArrayOutputStream();
			ImageOutputStream ziIos = ImageIO.createImageOutputStream( ziOs );

			ImageIO.write( ziRt.getBufferedImage(), "png", ziIos );
			InputStream ziContents = new ByteArrayInputStream( ziOs.toByteArray() );
			// Now save this generated thumb nail onto disk then pass the path to this in the analysed data
			hashedStorageService.storeContentsInWarehouse( ziScrollingThumbnailWarehouse, hashedRef, ziContents );
			
			analysedData.setPathToZiScrollingThumbnail( hashedStorageService.getPathToHashedRef( ziScrollingThumbnailWarehouse,
					hashedRef ) );

			// Zoomed out
			ByteArrayOutputStream zoOs = new ByteArrayOutputStream();
			ImageOutputStream zoIos = ImageIO.createImageOutputStream( zoOs );

			ImageIO.write( zoRt.getBufferedImage(), "png", zoIos );
			InputStream zoContents = new ByteArrayInputStream( zoOs.toByteArray() );
			// Now save this generated thumb nail onto disk then pass the path to this in the analysed data
			hashedStorageService.storeContentsInWarehouse( zoScrollingThumbnailWarehouse, hashedRef, zoContents );
			
			analysedData.setPathToZoScrollingThumbnail( hashedStorageService.getPathToHashedRef( zoScrollingThumbnailWarehouse,
					hashedRef ) );
		}
		catch (Exception e)
		{
			String msg = "Exception caught serialising static thumb nail: " + e.toString();
			log.error( msg, e );
		}
	}

}
