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

package uk.co.modularaudio.controller.advancedcomponents.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.controller.samplecaching.SampleCachingController;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisService;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.library.LibraryEntry;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;

public class AdvancedComponentsFrontControllerImpl implements ComponentWithLifecycle,
		AdvancedComponentsFrontController
{
	private static Log log = LogFactory.getLog( AdvancedComponentsFrontControllerImpl.class.getName() );

	private static final String CONFIG_KEY_WAVETABLES_CACHE_ROOT = "AdvancedComponents.WavetablesCacheRoot";

	// Internally used service references
	private ConfigurationService configurationService;
	private HibernateSessionController hibernateSessionController;
	private SampleCachingController sampleCachingController;
	private UserPreferencesService userPreferencesService;

	// Exposed data and services
	private String wavetablesCachingRoot;
	private OscillatorFactory oscillatorFactory;
	private BlockResamplerService blockResamplerService;
	private SampleCachingService sampleCachingService;
	private AudioAnalysisService audioAnalysisService;
	private JobExecutorService jobExecutorService;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				hibernateSessionController == null ||
				sampleCachingController == null ||
				blockResamplerService == null ||
				sampleCachingService == null ||
				audioAnalysisService == null ||
				jobExecutorService == null ||
				userPreferencesService == null )
		{
			throw new ComponentConfigurationException( "Controller missing dependencies. Check configuration" );
		}

		// Now fetch our music root
		// Grab the music root from the config file
		final Map<String,String> errors = new HashMap<String,String>();
		wavetablesCachingRoot = ConfigurationServiceHelper.checkForSingleStringKey( configurationService, CONFIG_KEY_WAVETABLES_CACHE_ROOT, errors );
		ConfigurationServiceHelper.errorCheck( errors );

		try
		{
			oscillatorFactory = OscillatorFactory.getInstance( wavetablesCachingRoot );
		}
		catch (final IOException e)
		{
			final String msg = "IOException caught obtaining reference to oscillator factory: " + e.toString();
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}
}

	@Override
	public void destroy()
	{
	}

	public void setSampleCachingController( final SampleCachingController sampleCachingController )
	{
		this.sampleCachingController = sampleCachingController;
	}

	public void setHibernateSessionController( final HibernateSessionController hibernateSessionController )
	{
		this.hibernateSessionController = hibernateSessionController;
	}

	public void setBlockResamplerService(final BlockResamplerService blockResamplerService)
	{
		this.blockResamplerService = blockResamplerService;
	}

	public void setSampleCachingService(final SampleCachingService sampleCachingService)
	{
		this.sampleCachingService = sampleCachingService;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setJobExecutorService( final JobExecutorService jobExecutorService )
	{
		this.jobExecutorService = jobExecutorService;
	}

	@Override
	public SampleCacheClient registerCacheClientForFile( final String path )
		throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		// Hibernate session needed so added to internal library
		Session sessionResource = null;
		Transaction t = null;
		try
		{
			hibernateSessionController.getThreadSession();
			sessionResource = ThreadLocalSessionResource.getSessionResource();
			t = sessionResource.beginTransaction();
			final SampleCacheClient retVal = sampleCachingController.registerCacheClientForFile( path );
			t.commit();
			t = null;
			return retVal;
		}
		catch (final NoSuchHibernateSessionException e)
		{
			final String msg = "Error in using hibernate session: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		finally
		{
			if( t != null )
			{
				t.rollback();
			}
			try
			{
				if( sessionResource != null )
				{
					hibernateSessionController.releaseThreadSession();
				}
			}
			catch (final NoSuchHibernateSessionException e)
			{
				// Nothing to clean up
			}
		}
	}

	@Override
	public void registerForBufferFillCompletion( final SampleCacheClient client,
			final BufferFillCompletionListener completionListener )
	{
		sampleCachingController.registerForBufferFillCompletion( client, completionListener );
	}

	@Override
	public void unregisterCacheClientForFile( final SampleCacheClient client )
			throws DatastoreException, RecordNotFoundException, IOException
	{
		// No hibernate session needed
		sampleCachingController.unregisterCacheClientForFile( client );
	}

	@Override
	public String getSoundfileMusicRoot()
	{
		// Always return latest version
		return userPreferencesService.getUserMusicDir();
	}

	@Override
	public OscillatorFactory getOscillatorFactory()
	{
		return oscillatorFactory;
	}

	@Override
	public BlockResamplerService getBlockResamplerService()
	{
		return blockResamplerService;
	}

	@Override
	public SampleCachingService getSampleCachingService()
	{
		return sampleCachingService;
	}

	@Override
	public JobExecutorService getJobExecutorService()
	{
		return jobExecutorService;
	}

	@Override
	public BlockResamplingClient createResamplingClient( final String pathToFile,
			final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		// Hibernate session needed so added to internal library
		Session sessionResource = null;
		Transaction t = null;
		try
		{
			hibernateSessionController.getThreadSession();
			sessionResource = ThreadLocalSessionResource.getSessionResource();
			t = sessionResource.beginTransaction();
			final BlockResamplingClient retVal = sampleCachingController.createResamplingClient( pathToFile, resamplingMethod );
			t.commit();
			t = null;
			return retVal;
		}
		catch (final NoSuchHibernateSessionException e)
		{
			final String msg = "Error in using hibernate session: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		finally
		{
			if( t != null )
			{
				t.rollback();
			}
			try
			{
				if( sessionResource != null )
				{
					hibernateSessionController.releaseThreadSession();
				}
			}
			catch (final NoSuchHibernateSessionException e)
			{
				// Nothing to clean up
			}
		}
	}

	@Override
	public void destroyResamplingClient( final BlockResamplingClient resamplingClient )
			throws DatastoreException, RecordNotFoundException
	{
		// No hibernate session needed.
		sampleCachingController.destroyResamplingClient( resamplingClient );
	}

	@Override
	public BlockResamplingClient promoteSampleCacheClientToResamplingClient( final SampleCacheClient sampleCacheClient,
			final BlockResamplingMethod cubic )
	{
		// No hibernate session needed.
		return sampleCachingController.promoteSampleCacheClientToResamplingClient( sampleCacheClient, cubic );
	}

	@Override
	public AnalysedData registerForLibraryEntryAnalysis( final LibraryEntry libraryEntry,
			final AnalysisFillCompletionListener analysisListener ) throws DatastoreException
	{
		// Hibernate session needed so added to internal database table
		Session sessionResource = null;
		Transaction t = null;
		try
		{
			hibernateSessionController.getThreadSession();
			sessionResource = ThreadLocalSessionResource.getSessionResource();
			t = sessionResource.beginTransaction();

			final AnalysedData retVal = audioAnalysisService.analyseLibraryEntryFile( libraryEntry,
					analysisListener );

			t.commit();
			t = null;
			return retVal;
		}
		catch (final NoSuchHibernateSessionException e)
		{
			final String msg = "Error in using hibernate session: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		finally
		{
			if( t != null )
			{
				t.rollback();
			}
			try
			{
				if( sessionResource != null )
				{
					hibernateSessionController.releaseThreadSession();
				}
			}
			catch (final NoSuchHibernateSessionException e)
			{
				// Nothing to clean up
			}
		}
	}

	public void setAudioAnalysisService( final AudioAnalysisService audioAnalysisService )
	{
		this.audioAnalysisService = audioAnalysisService;
	}

	public void setUserPreferencesService( final UserPreferencesService userPreferencesService )
	{
		this.userPreferencesService = userPreferencesService;
	}
}
