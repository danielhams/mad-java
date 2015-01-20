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
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorFactory;
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

	private static final String CONFIG_KEY_SAMPLER_MUSIC_ROOT = "AdvancedComponents.SamplerMusicRoot";
	private static final String CONFIG_KEY_WAVETABLES_CACHE_ROOT = "AdvancedComponents.WavetablesCacheRoot";

	private ConfigurationService configurationService = null;
	private HibernateSessionController hibernateSessionController = null;
	private SampleCachingController sampleCachingController = null;
	private String samplePlayerSelectionRoot = null;

	private String wavetablesCachingRoot = null;

	private OscillatorFactory oscillatorFactory = null;

	private BlockResamplerService blockResamplerService = null;
	private SampleCachingService sampleCachingService = null;

	@Override
	public void init() throws ComponentConfigurationException
	{
		// Now fetch our music root
		// Grab the music root from the config file
		Map<String,String> errors = new HashMap<String,String>();
		samplePlayerSelectionRoot = ConfigurationServiceHelper.checkForSingleStringKey( configurationService, CONFIG_KEY_SAMPLER_MUSIC_ROOT, errors );
		wavetablesCachingRoot = ConfigurationServiceHelper.checkForSingleStringKey( configurationService, CONFIG_KEY_WAVETABLES_CACHE_ROOT, errors );
		ConfigurationServiceHelper.errorCheck( errors );

		try
		{
			oscillatorFactory = OscillatorFactory.getInstance( wavetablesCachingRoot );
		}
		catch (IOException e)
		{
			String msg = "IOException caught obtaining reference to oscillator factory: " + e.toString();
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}
}

	@Override
	public void destroy()
	{
	}

	public void setSampleCachingController( SampleCachingController sampleCachingController )
	{
		this.sampleCachingController = sampleCachingController;
	}

	public void setHibernateSessionController( HibernateSessionController hibernateSessionController )
	{
		this.hibernateSessionController = hibernateSessionController;
	}

	public void setBlockResamplerService(BlockResamplerService blockResamplerService)
	{
		this.blockResamplerService = blockResamplerService;
	}

	public void setSampleCachingService(SampleCachingService sampleCachingService)
	{
		this.sampleCachingService = sampleCachingService;
	}

	@Override
	public SampleCacheClient registerCacheClientForFile( String path ) throws DatastoreException, UnsupportedAudioFileException
	{
		SampleCacheClient retVal = null;
		Session sessionResource = null;
		Transaction t = null;
		try
		{
			hibernateSessionController.getThreadSession();
			sessionResource = ThreadLocalSessionResource.getSessionResource();
			t = sessionResource.beginTransaction();
			retVal = sampleCachingController.registerCacheClientForFile( path );
			t.commit();
			t = null;
			return retVal;
		}
		catch (NoSuchHibernateSessionException e)
		{
			String msg = "Error in using hibernate session: " + e.toString();
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
			catch (NoSuchHibernateSessionException e)
			{
				// Nothing to clean up
			}
		}
	}

	@Override
	public void unregisterCacheClientForFile( SampleCacheClient client )
			throws DatastoreException, RecordNotFoundException, IOException
	{
		sampleCachingController.unregisterCacheClientForFile( client );
	}

//	@Override
//	public RealtimeMethodReturnCodeEnum sampleClientFetchFrames( SampleCacheClient client,
//			float[] outputInterleavedFloats,
//			int outputPosition,
//			int lengthInFrames )
//	{
//		return sampleCachingController.readSamplesForCacheClient( client,
//				outputInterleavedFloats,
//				outputPosition,
//				lengthInFrames );
//	}
//
//	@Override
//	public RealtimeMethodReturnCodeEnum sampleClientFetchFramesResample( ResampledSamplePlaybackDetails resampledSamplePlaybackDetails,
//			BlockResamplingMethod resamplingMethod,
//			double playbackSpeed,
//			float[] outputLeftFloats,
//			float[] outputRightFloats,
//			int outputPos,
//			int numRequired,
//			boolean addToOutput )
//	{
//		return sampleCachingController.sampleClientFetchFramesResample( resampledSamplePlaybackDetails,
//				resamplingMethod,
//				playbackSpeed,
//				outputLeftFloats,
//				outputRightFloats,
//				outputPos,
//				numRequired,
//				addToOutput );
//	}
//
//	@Override
//	public RealtimeMethodReturnCodeEnum sampleClientFetchFramesResample( ResampledSamplePlaybackDetails resampledSamplePlaybackDetails,
//			BlockResamplingMethod resamplingMethod,
//			double playbackSpeed,
//			float[] outputLeftFloats,
//			float[] outputRightFloats,
//			int outputPos,
//			int numRequired,
//			float[] requiredAmps,
//			boolean addToOutput )
//	{
//		return sampleCachingController.sampleClientFetchFramesResample( resampledSamplePlaybackDetails,
//				resamplingMethod,
//				playbackSpeed,
//				outputLeftFloats,
//				outputRightFloats,
//				outputPos,
//				numRequired,
//				requiredAmps,
//				addToOutput );
//	}

	@Override
	public String getSampleSelectionMusicRoot()
	{
		return samplePlayerSelectionRoot;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
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
}
