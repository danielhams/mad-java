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

package uk.co.modularaudio.controller.apprendering.impl;

import uk.co.modularaudio.controller.apprendering.AppRenderingController;
import uk.co.modularaudio.service.apprendering.util.AppRenderingSession;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue;
import uk.co.modularaudio.service.audioproviderregistry.AudioProvider;
import uk.co.modularaudio.service.audioproviderregistry.AudioProviderRegistryService;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AppRenderingControllerImpl implements ComponentWithLifecycle, AppRenderingController
{
//	private static Log log = LogFactory.getLog( AppRenderingControllerImpl.class.getName() );

	private AudioProviderRegistryService audioProviderRegistryService;

	private AppRenderingErrorQueue errorQueue;

	public AppRenderingControllerImpl()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( audioProviderRegistryService == null )
		{
			throw new ComponentConfigurationException( "Service is missing dependencies" );
		}

		errorQueue = new AppRenderingErrorQueue();
	}

	@Override
	public void destroy()
	{
		errorQueue.shutdown();
	}

	public void setAudioProviderRegistryService( final AudioProviderRegistryService audioProviderRegistryService )
	{
		this.audioProviderRegistryService = audioProviderRegistryService;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.controller.apprendering.AppRenderingController#createAppRenderingSessionForConfiguration(uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration, uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback)
	 */
	@Override
	public AppRenderingSession createAppRenderingSessionForConfiguration( final HardwareIOConfiguration hardwareIOConfiguration,
			final AppRenderingErrorCallback errorCallback )
			throws DatastoreException, RecordNotFoundException
	{
		// Check all the appropriate things come from the same provider
		String firstFoundId = null;
		final AudioHardwareDevice phs = hardwareIOConfiguration.getProducerAudioDevice();
		if( phs != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = phs.getProviderId();
			}
		}
		final AudioHardwareDevice chs = hardwareIOConfiguration.getConsumerAudioDevice();
		if( chs != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = chs.getProviderId();
			}
			else if( !firstFoundId.equals( chs.getProviderId() ) )
			{
				throw new DatastoreException("All hardware must come from the same provider.");
			}
		}
		final MidiHardwareDevice pmc = hardwareIOConfiguration.getProducerMidiDevice();
		if( pmc != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = pmc.getProviderId();
			}
			else if( !firstFoundId.equals( pmc.getProviderId() ) )
			{
				throw new DatastoreException("All hardware must come from the same provider.");
			}
		}
		final MidiHardwareDevice cmc = hardwareIOConfiguration.getConsumerMidiDevice();
		if( cmc != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = cmc.getProviderId();
			}
			else if( !firstFoundId.equals( cmc.getProviderId() ) )
			{
				throw new DatastoreException("All hardware must come from the same provider.");
			}
		}

		if( phs == null && chs == null )
		{
			throw new DatastoreException("Either an input or output audio device must be used.");
		}

		// Now get the audio provider from the registry
		final AudioProvider provider = audioProviderRegistryService.getProviderById( firstFoundId );

		return provider.createAppRenderingSessionForConfiguration( hardwareIOConfiguration, errorQueue, errorCallback );
	}
}
