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

	private final int scrollingThumbnailSamplesPerPixelZoomedIn;
	private final int scrollingThumbnailSamplesPerPixelZoomedOut;
	private final int scrollingThumbnailHeight;
	private final Color scrollingMinMaxColor;
	private final Color scrollingRmsColor;
	private final HashedStorageService hashedStorageService;
	private final HashedWarehouse ziScrollingThumbnailWarehouse;
	private final HashedWarehouse zoScrollingThumbnailWarehouse;

	private final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();
	private ThumbnailGenerationRT ziRt = null;
	private ThumbnailGenerationRT zoRt = null;

	public ScrollingThumbnailGeneratorListener( final int scrollingThumbnailSamplesPerPixelZoomedIn,
			final int scrollingThumbnailSamplesPerPixelZoomedOut,
			final int scrollingThumbnailHeight,
			final Color scrollingMinMaxColor,
			final Color scrollingRmsColor,
			final HashedStorageService hashedStorageService,
			final HashedWarehouse ziScrollingThumbnailWarehouse,
			final HashedWarehouse zoScrollingThumbnailWarehouse )
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
	public void start( final DataRate dataRate, final int numChannels, final long totalFloatsLength )
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
	public void receiveData(final float[] data, final int numRead )
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
	public void updateAnalysedData(final AnalysedData analysedData, final HashedRef hashedRef )
	{
		try
		{
			// Zoomed in
			final ByteArrayOutputStream ziOs = new ByteArrayOutputStream();
			final ImageOutputStream ziIos = ImageIO.createImageOutputStream( ziOs );

			ImageIO.write( ziRt.getBufferedImage(), "png", ziIos );
			final InputStream ziContents = new ByteArrayInputStream( ziOs.toByteArray() );
			// Now save this generated thumb nail onto disk then pass the path to this in the analysed data
			hashedStorageService.storeContentsInWarehouse( ziScrollingThumbnailWarehouse, hashedRef, ziContents );

			analysedData.setPathToZiScrollingThumbnail( hashedStorageService.getPathToHashedRef( ziScrollingThumbnailWarehouse,
					hashedRef ) );

			// Zoomed out
			final ByteArrayOutputStream zoOs = new ByteArrayOutputStream();
			final ImageOutputStream zoIos = ImageIO.createImageOutputStream( zoOs );

			ImageIO.write( zoRt.getBufferedImage(), "png", zoIos );
			final InputStream zoContents = new ByteArrayInputStream( zoOs.toByteArray() );
			// Now save this generated thumb nail onto disk then pass the path to this in the analysed data
			hashedStorageService.storeContentsInWarehouse( zoScrollingThumbnailWarehouse, hashedRef, zoContents );

			analysedData.setPathToZoScrollingThumbnail( hashedStorageService.getPathToHashedRef( zoScrollingThumbnailWarehouse,
					hashedRef ) );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught serialising static thumb nail: " + e.toString();
			log.error( msg, e );
		}
	}

}
