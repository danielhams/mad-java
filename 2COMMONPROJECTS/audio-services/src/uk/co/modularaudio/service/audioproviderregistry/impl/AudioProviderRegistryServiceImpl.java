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

package uk.co.modularaudio.service.audioproviderregistry.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioproviderregistry.AudioProviderRegistryService;
import uk.co.modularaudio.service.audioproviderregistry.pub.AudioProvider;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDeviceCriteria;
import uk.co.modularaudio.util.audio.mad.hardwareio.DeviceDirection;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDeviceCriteria;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AudioProviderRegistryServiceImpl
	implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, AudioProviderRegistryService
{
	private static Log log = LogFactory.getLog( AudioProviderRegistryServiceImpl.class.getName() );

	private Set<AudioProvider> providers = new HashSet<AudioProvider>();
	private Map<String,AudioProvider> idToProviderMap = new HashMap<String, AudioProvider>();

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
		// Now unregister any providers still registered
		Collection<AudioProvider> providersToShutdown = new ArrayList<AudioProvider>( providers );
		for( AudioProvider p : providersToShutdown )
		{
			try
			{
				unregisterAudioProvider(p);
			}
			catch (Exception e)
			{
				log.error( "Unable to unregister provider: " + e, e );
			}
		}
		providers.clear();
	}

	@Override
	public void postInit() throws DatastoreException
	{
	}

	@Override
	public void preShutdown() throws DatastoreException
	{
	}

	@Override
	public void registerAudioProvider( AudioProvider provider )
			throws DatastoreException, MAConstraintViolationException
	{
		if( providers.contains( provider) )
		{
			throw new MAConstraintViolationException();
		}
		else
		{
			log.info("Registering audio provider: " + provider.getId() );
			providers.add( provider );
			idToProviderMap.put( provider.getId(), provider );
		}
	}

	@Override
	public void unregisterAudioProvider(AudioProvider provider)
			throws DatastoreException, RecordNotFoundException
	{
		if( providers.contains( provider ) )
		{
			log.info("Unregistered audio provider:" + provider.getId() );
			idToProviderMap.remove( provider.getId() );
			providers.remove( provider );
		}
		else
		{
			throw new RecordNotFoundException();
		}
	}

	@Override
	public AudioProvider getProviderById(String providerId ) throws RecordNotFoundException
	{
		if( !idToProviderMap.containsKey( providerId ) )
		{
			throw new RecordNotFoundException("Unable to find audio provider with id: " + providerId );
		}
		else
		{
			return idToProviderMap.get( providerId );
		}
	}

	@Override
	public List<MidiHardwareDevice> getAllProducerMidiDevices( MidiHardwareDeviceCriteria criteria ) throws DatastoreException
	{
		return getMidiConfigurations( criteria, DeviceDirection.CONSUMER );
	}

	@Override
	public List<MidiHardwareDevice> getAllConsumerMidiDevices( MidiHardwareDeviceCriteria criteria ) throws DatastoreException
	{
		return getMidiConfigurations( criteria, DeviceDirection.PRODUCER );
	}

	private List<MidiHardwareDevice> getMidiConfigurations( MidiHardwareDeviceCriteria criteria, DeviceDirection streamType )
					throws DatastoreException
	{
		List<MidiHardwareDevice> retVal = new ArrayList<MidiHardwareDevice>();
		for( AudioProvider p : providers )
		{
			switch( streamType )
			{
				case CONSUMER:
					retVal.addAll( p.getAllConsumerMidiDevices( criteria ) );
					break;
				case PRODUCER:
					retVal.addAll( p.getAllProducerMidiDevices( criteria ) );
					break;
			}
		}
		return retVal;
	}

	@Override
	public List<AudioHardwareDevice> getAllProducerAudioDevices( AudioHardwareDeviceCriteria criteria) throws DatastoreException
	{
		return getAudioHardwareDevices( criteria, DeviceDirection.PRODUCER );
	}

	@Override
	public List<AudioHardwareDevice> getAllConsumerAudioDevices( AudioHardwareDeviceCriteria criteria) throws DatastoreException
	{
		return getAudioHardwareDevices( criteria, DeviceDirection.CONSUMER );
	}

	private List<AudioHardwareDevice> getAudioHardwareDevices( AudioHardwareDeviceCriteria criteria, DeviceDirection streamType )
		throws DatastoreException
	{
		List<AudioHardwareDevice> retVal = new ArrayList<AudioHardwareDevice>();
		for( AudioProvider p : providers )
		{
			switch( streamType )
			{
				case CONSUMER:
					retVal.addAll( p.getAllConsumerAudioDevices( criteria ) );
					break;
				case PRODUCER:
					retVal.addAll( p.getAllProducerAudioDevices( criteria ) );
					break;
			}
		}
		return retVal;
	}
}
