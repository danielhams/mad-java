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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisService;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.AnalysisListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.BeatDetectionListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.GainDetectionListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.StaticThumbnailGeneratorListener;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.service.hashedstorage.HashedStorageService;
import uk.co.modularaudio.service.hashedstorage.HashedWarehouse;
import uk.co.modularaudio.util.audio.format.DataRate;
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

	private ConfigurationService configurationService;
	private AudioFileIORegistryService audioFileIORegistryService;
	private HashedStorageService hashedStorageService;

	private String staticThumbnailRootDir;
	private int staticThumbnailWidth;
	private int staticThumbnailHeight;
	private Color staticMinMaxColor;
	private Color staticRmsColor;

	private HashedWarehouse staticThumbnailWarehouse;

	private int analysisBufferSize;
	private float[] analysisBuffer;

	private final ArrayList<AnalysisListener> analysisListeners = new ArrayList<AnalysisListener>( 10 );

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				audioFileIORegistryService == null ||
				hashedStorageService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}

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

			// Our internal buffer size
			analysisBufferSize = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_ANALYSIS_BUFFER_SIZE );
		}
		catch (final RecordNotFoundException e)
		{
			final String msg = "Unable to find config key: " + e.toString();
			log.error( msg, e);
			throw new ComponentConfigurationException( msg, e );
		}

		// Make sure our directories are correctly initialised
		try
		{
			staticThumbnailWarehouse = hashedStorageService.initStorage( staticThumbnailRootDir );
		}
		catch (final IOException e)
		{
			final String msg = "Unable to initialise hashed storage: " + e.toString();
			log.error( msg, e);
			throw new ComponentConfigurationException( msg, e );
		}

		// Initialise our buffer
		analysisBuffer = new float[ analysisBufferSize ];

		// and then initialise the list of listeners.
		final BeatDetectionListener bdl = new BeatDetectionListener();
		analysisListeners.add( bdl );

		final GainDetectionListener gdl = new GainDetectionListener();
		analysisListeners.add( gdl );

		final StaticThumbnailGeneratorListener stgl = new StaticThumbnailGeneratorListener( staticThumbnailWidth,
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
	public AnalysedData analyseFile(final String pathToFile, final ProgressListener progressListener)
			throws DatastoreException, IOException, RecordNotFoundException, UnsupportedAudioFileException
	{
		final AnalysedData analysedData = new AnalysedData();

		// Create the hashed ref our listeners might be interested in
		final HashedRef fileHashedRef = hashedStorageService.getHashedRefForFilename( pathToFile );

		final AudioFileFormat fileFormat = audioFileIORegistryService.sniffFileFormatOfFile( pathToFile );

		final AudioFileIOService decoderService = audioFileIORegistryService.getAudioFileIOServiceForFormatAndDirection( fileFormat,
				AudioFileDirection.DECODE );

		AudioFileHandleAtom fha = null;

		try
		{
			fha = decoderService.openForRead( pathToFile );

			final StaticMetadata sm = fha.getStaticMetadata();
			final DataRate dataRate = sm.dataRate;
			final int numChannels = sm.numChannels;
			final long totalFloats = sm.numFloats;
			if( log.isDebugEnabled() )
			{
				log.debug("Beginning audio analysis on file " + pathToFile + " with " + totalFloats + " total floating point samples");
			}
			for( final AnalysisListener al : analysisListeners )
			{
				al.start( dataRate, numChannels, totalFloats );
			}
			int percentageComplete = 0;
			for( long curPos = 0 ; curPos < totalFloats ; curPos += analysisBufferSize )
			{
				final int numFramesRead = decoderService.readFrames( fha,
						analysisBuffer,
						0,
						analysisBufferSize / numChannels,
						curPos / numChannels );
				int numRead = numFramesRead * numChannels;

				if( numRead <= 0 )
				{
					// Pad with empty samples
					numRead = analysisBufferSize;
					Arrays.fill( analysisBuffer, 0, analysisBufferSize, 0.0f );
					curPos = totalFloats;
				}
				if( numRead > 0 )
				{
					for( final AnalysisListener al : analysisListeners )
					{
						al.receiveData( analysisBuffer, numRead );
					}
				}
				final int newPercentageComplete = (int)( ((float)curPos / (float)totalFloats) * 100.0);
				if( newPercentageComplete != percentageComplete )
				{
					percentageComplete = newPercentageComplete;
					progressListener.receivePercentageComplete( "Distance in file: ", percentageComplete );
				}
			}

			for( final AnalysisListener al : analysisListeners )
			{
				al.end();
				al.updateAnalysedData( analysedData, fileHashedRef );
			}
		}
		finally
		{
			if( fha != null )
			{
				decoderService.closeHandle( fha );
			}
		}

		return analysedData;
	}

	public void setAudioFileIORegistryService( final AudioFileIORegistryService audioFileIORegistryService )
	{
		this.audioFileIORegistryService = audioFileIORegistryService;
	}

	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public void setHashedStorageService(final HashedStorageService hashedStorageService)
	{
		this.hashedStorageService = hashedStorageService;
	}
}
