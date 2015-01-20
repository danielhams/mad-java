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

package uk.co.modularaudio.service.audioanalysis.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioanalysis.AudioAnalysisService;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.AnalysisListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.BeatDetectionListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.GainDetectionListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.ScrollingThumbnailGeneratorListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.StaticThumbnailGeneratorListener;
import uk.co.modularaudio.service.audioanalysis.vos.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.vos.AudioAnalysisException;
import uk.co.modularaudio.service.audiodatafetcher.AudioDataFetcherFactory;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.hashedstorage.HashedStorageService;
import uk.co.modularaudio.service.hashedstorage.vos.HashedRef;
import uk.co.modularaudio.service.hashedstorage.vos.HashedWarehouse;
import uk.co.modularaudio.util.audio.fileio.IAudioDataFetcher;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.format.UnknownDataRateException;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.unitOfWork.ProgressListener;

public class AudioAnalysisServiceImpl implements ComponentWithLifecycle, AudioAnalysisService
{

	private static Log log = LogFactory.getLog( AudioAnalysisServiceImpl.class.getName() );

	private static final String CONFIG_KEY_ANALYSIS_BUFFER_SIZE = "BufferSize";

	private static final String CONFIG_KEY_STATIC_THUMB_ROOT = "StaticThumbnailRootDir";
	private static final String CONFIG_KEY_STATIC_MINMAX_COLOR = "StaticThumbnailMinMaxColor";
	private static final String CONFIG_KEY_STATIC_RMS_COLOR = "StaticThumbnailRmsColor";
	private static final String CONFIG_KEY_STATIC_THUMB_WIDTH = "StaticThumbnailWidth";
	private static final String CONFIG_KEY_STATIC_THUMB_HEIGHT = "StaticThumbnailHeight";

	private static final String CONFIG_KEY_ZI_SCROLLING_THUMB_ROOT = "ZiScrollingThumbnailRootDir";
	private static final String CONFIG_KEY_ZO_SCROLLING_THUMB_ROOT = "ZoScrollingThumbnailRootDir";
	private static final String CONFIG_KEY_SCROLLING_THUMB_SPP_ZI = "ScrollingThumbnailSamplesPerPixelZoomedIn";
	private static final String CONFIG_KEY_SCROLLING_THUMB_SPP_ZO = "ScrollingThumbnailSamplesPerPixelZoomedOut";
	private static final String CONFIG_KEY_SCROLLING_THUMB_HEIGHT = "ScrollingThumbnailHeight";
	private static final String CONFIG_KEY_SCROLLING_MINMAX_COLOR = "ScrollingThumbnailMinMaxColor";
	private static final String CONFIG_KEY_SCROLLING_RMS_COLOR = "ScrollingThumbnailRmsColor";

	private ConfigurationService configurationService = null;
	private AudioDataFetcherFactory audioDataFetcherFactory = null;
	private HashedStorageService hashedStorageService = null;

	private String staticThumbnailRootDir = null;
	private int staticThumbnailWidth = -1;
	private int staticThumbnailHeight = -1;
	private Color staticMinMaxColor = null;
	private Color staticRmsColor = null;

	private String ziScrollingThumbnailRootDir = null;
	private String zoScrollingThumbnailRootDir = null;
	private int scrollingThumbnailSamplesPerPixelZoomedIn = -1;
	private int scrollingThumbnailSamplesPerPixelZoomedOut = -1;
	private int scrollingThumbnailHeight = -1;
	private Color scrollingMinMaxColor = null;
	private Color scrollingRmsColor = null;

	private HashedWarehouse staticThumbnailWarehouse = null;
	private HashedWarehouse ziScrollingThumbnailWarehouse = null;
	private HashedWarehouse zoScrollingThumbnailWarehouse = null;

	private int analysisBufferSize = -1;
	private float[] analysisBuffer = null;

	private ArrayList<AnalysisListener> analysisListeners = new ArrayList<AnalysisListener>( 10 );

	@Override
	public void init() throws ComponentConfigurationException
	{
		// Fetch our configuration data
		// (1) Static thumb nail storage directory
		// (2) Scrolling thumb nail storage directory
		try
		{
			// Static thumbnail config
			staticThumbnailRootDir = configurationService.getSingleStringValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_THUMB_ROOT );
			staticThumbnailWidth = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_THUMB_WIDTH );
			staticThumbnailHeight = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_THUMB_HEIGHT );
			staticMinMaxColor = configurationService.getSingleColorValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_MINMAX_COLOR );
			staticRmsColor = configurationService.getSingleColorValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_RMS_COLOR );

			// Scrolling thumbnail config
			ziScrollingThumbnailRootDir = configurationService.getSingleStringValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_ZI_SCROLLING_THUMB_ROOT );
			zoScrollingThumbnailRootDir = configurationService.getSingleStringValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_ZO_SCROLLING_THUMB_ROOT );
			scrollingThumbnailSamplesPerPixelZoomedIn = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_SCROLLING_THUMB_SPP_ZI );
			scrollingThumbnailSamplesPerPixelZoomedOut = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_SCROLLING_THUMB_SPP_ZO );
			scrollingThumbnailHeight = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_SCROLLING_THUMB_HEIGHT );
			scrollingMinMaxColor = configurationService.getSingleColorValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_SCROLLING_MINMAX_COLOR );
			scrollingRmsColor = configurationService.getSingleColorValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_SCROLLING_RMS_COLOR );

			// Our internal buffer size
			analysisBufferSize = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_ANALYSIS_BUFFER_SIZE );
		}
		catch (RecordNotFoundException e)
		{
			String msg = "Unable to find config key: " + e.toString();
			log.error( msg, e);
			throw new ComponentConfigurationException( msg, e );
		}

		// Make sure our directories are correctly initialised
		try
		{
			staticThumbnailWarehouse = hashedStorageService.initStorage( staticThumbnailRootDir );
			ziScrollingThumbnailWarehouse = hashedStorageService.initStorage( ziScrollingThumbnailRootDir );
			zoScrollingThumbnailWarehouse = hashedStorageService.initStorage( zoScrollingThumbnailRootDir );
		}
		catch (IOException e)
		{
			String msg = "Unable to initialise hashed storage: " + e.toString();
			log.error( msg, e);
			throw new ComponentConfigurationException( msg, e );
		}

		// Initialise our buffer
		analysisBuffer = new float[ analysisBufferSize ];

		// and then initialise the list of listeners.
		BeatDetectionListener bdl = new BeatDetectionListener();
		analysisListeners.add( bdl );
		GainDetectionListener gdl = new GainDetectionListener();
		analysisListeners.add( gdl );
		ScrollingThumbnailGeneratorListener scgl = new ScrollingThumbnailGeneratorListener(
				scrollingThumbnailSamplesPerPixelZoomedIn,
				scrollingThumbnailSamplesPerPixelZoomedOut,
				scrollingThumbnailHeight,
				scrollingMinMaxColor,
				scrollingRmsColor,
				hashedStorageService,
				ziScrollingThumbnailWarehouse,
				zoScrollingThumbnailWarehouse );

		analysisListeners.add( scgl );
		StaticThumbnailGeneratorListener stgl = new StaticThumbnailGeneratorListener( staticThumbnailWidth,
				staticThumbnailHeight,
				staticMinMaxColor,
				staticRmsColor,
				hashedStorageService,
				staticThumbnailWarehouse );
		analysisListeners.add( stgl );
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public AnalysedData analyseFile(String pathToFile, ProgressListener progressListener)
		throws IOException, AudioAnalysisException, UnsupportedAudioFileException, UnknownDataRateException, DatastoreException
	{
		AnalysedData analysedData = new AnalysedData();

		// Create the hashed ref our listeners might be interested in
		HashedRef fileHashedRef = hashedStorageService.getHashedRefForFilename( pathToFile );

		// Get the data fetcher for the file and then loop around fetching chunks of the file and feeding it to the analysers
		// Then at the end
		File file = new File( pathToFile );
		DataRate dataRate = DataRate.SR_44100;
		int numChannels = 2;
		IAudioDataFetcher dataFetcher = audioDataFetcherFactory.getFetcherForFile( file );
		AudioFormat desiredAudioFormat = new AudioFormat( dataRate.getValue(), 16, numChannels, true, true );

		dataFetcher.open( desiredAudioFormat,  file );

		long totalFloats = dataFetcher.getNumTotalFloats();
		log.debug("Beginning audio analysis on file " + file.getName() + " with " + totalFloats + " total floating point samples");

		for( AnalysisListener al : analysisListeners )
		{
			al.start( dataRate, numChannels, totalFloats );
		}
		int percentageComplete = 0;
		for( long curPos = 0 ; curPos < totalFloats ; curPos += analysisBufferSize )
		{
			int numRead = dataFetcher.read( analysisBuffer, 0, curPos, analysisBufferSize );

			if( numRead <= 0 )
			{
				// Pad with empty samples
				numRead = analysisBufferSize;
				Arrays.fill( analysisBuffer, 0, analysisBufferSize, 0.0f );
				curPos = totalFloats;
			}
			if( numRead > 0 )
			{
				for( AnalysisListener al : analysisListeners )
				{
					al.receiveData( analysisBuffer, numRead );
				}
			}
			int newPercentageComplete = (int)( ((float)curPos / (float)totalFloats) * 100.0);
			if( newPercentageComplete != percentageComplete )
			{
				percentageComplete = newPercentageComplete;
				progressListener.receivePercentageComplete( "Distance in file: ", percentageComplete );
			}
		}

		for( AnalysisListener al : analysisListeners )
		{
			al.end();
			al.updateAnalysedData( analysedData, fileHashedRef );
		}

		return analysedData;
	}

	public AudioDataFetcherFactory getAudioDataFetcherFactory()
	{
		return audioDataFetcherFactory;
	}

	public void setAudioDataFetcherFactory(AudioDataFetcherFactory audioDataFetcherFactory)
	{
		this.audioDataFetcherFactory = audioDataFetcherFactory;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public HashedStorageService getHashedStorageService()
	{
		return hashedStorageService;
	}

	public void setHashedStorageService(HashedStorageService hashedStorageService)
	{
		this.hashedStorageService = hashedStorageService;
	}
}
