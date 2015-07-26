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

package uk.co.modularaudio.service.jnajackaudioprovider;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackOptions;
import org.jaudiolibs.jnajack.JackStatus;

import uk.co.modularaudio.service.apprendering.AppRenderingService;
import uk.co.modularaudio.service.apprendering.util.AppRenderingSession;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue;
import uk.co.modularaudio.service.audioproviderregistry.AudioProvider;
import uk.co.modularaudio.service.audioproviderregistry.AudioProviderRegistryService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDeviceCriteria;
import uk.co.modularaudio.util.audio.mad.hardwareio.DeviceDirection;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDeviceCriteria;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class JNAJackAudioProvider extends AudioProvider implements ComponentWithLifecycle
{
	private static Log log = LogFactory.getLog( JNAJackAudioProvider.class.getName() );

	public static final String AUDIO_SOURCE_IDENTIFIER = "The PAC JNA Jack AudioProvider";

	private final static String PROVIDER_ID = "JNA Jack Provider";

	private static final String CONFIG_KEY_DO_CONNECT = JNAJackAudioProvider.class.getSimpleName() + ".DoConnect";

	private static final int NUM_STEREO_JACK_AUDIO_CHANNELS = 2;
	private static final int NUM_QUAD_JACK_AUDIO_CHANNELS = 4;
	private static final int NUM_SURROUND_JACK_AUDIO_CHANNELS = 8;

	private ConfigurationService configurationService;

	private AudioProviderRegistryService audioProviderRegistryService;
	private TimingService timingService;
	private AppRenderingService appRenderingService;

	private boolean doConnect;

	private final ArrayList<AudioHardwareDevice> consumerAudioHardwareDevices;
	private final ArrayList<AudioHardwareDevice> producerAudioHardwareDevices;

	private final ArrayList<MidiHardwareDevice> consumerMidiHardwareDevices;
	private final ArrayList<MidiHardwareDevice> producerMidiHardwareDevices;

	// JavaJack stuff
	private boolean connectedToJack;
	private Jack jack;
	private JackClient client;
	private final String jackClientName = "Component Designer";

	public JNAJackAudioProvider()
	{
		super( PROVIDER_ID );

		consumerAudioHardwareDevices = new ArrayList<AudioHardwareDevice>();

		producerAudioHardwareDevices = new ArrayList<AudioHardwareDevice>();

		consumerMidiHardwareDevices = new ArrayList<MidiHardwareDevice>();

		producerMidiHardwareDevices = new ArrayList<MidiHardwareDevice>();
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				audioProviderRegistryService == null ||
				timingService == null )
		{
			final String msg = "JNAJackAudioProvider is missing service dependencies. Please check configuration";
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}

		try
		{

			final Map<String,String> errors = new HashMap<String,String>();

			doConnect = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_DO_CONNECT, errors );

			ConfigurationServiceHelper.errorCheck( errors );

			consumerAudioHardwareDevices.add( new AudioHardwareDevice( this.getId(),
					"jnajackout8",
					"JNAJack " + NUM_SURROUND_JACK_AUDIO_CHANNELS + " Channel Output",
					DeviceDirection.CONSUMER,
					 NUM_SURROUND_JACK_AUDIO_CHANNELS ) );

			producerAudioHardwareDevices.add( new AudioHardwareDevice( this.getId(),
					"jnajackin8",
					"JNAJack " + NUM_SURROUND_JACK_AUDIO_CHANNELS + " Channel Input",
					DeviceDirection.PRODUCER,
					NUM_SURROUND_JACK_AUDIO_CHANNELS ));

			consumerAudioHardwareDevices.add( new AudioHardwareDevice( this.getId(),
					"jnajackout4",
					"JNAJack " + NUM_QUAD_JACK_AUDIO_CHANNELS + " Channel Output",
					DeviceDirection.CONSUMER,
					 NUM_QUAD_JACK_AUDIO_CHANNELS ) );

			producerAudioHardwareDevices.add( new AudioHardwareDevice( this.getId(),
					"jnajackin4",
					"JNAJack " + NUM_QUAD_JACK_AUDIO_CHANNELS + " Channel Input",
					DeviceDirection.PRODUCER,
					NUM_QUAD_JACK_AUDIO_CHANNELS ));

			consumerAudioHardwareDevices.add( new AudioHardwareDevice( this.getId(),
					"jnajackout2",
					"JNAJack " + NUM_STEREO_JACK_AUDIO_CHANNELS + " Channel Output",
					DeviceDirection.CONSUMER,
					NUM_STEREO_JACK_AUDIO_CHANNELS ) );

			producerAudioHardwareDevices.add( new AudioHardwareDevice( this.getId(),
					"jnajackin2",
					"JNAJack " + NUM_STEREO_JACK_AUDIO_CHANNELS + " Channel Input",
					DeviceDirection.PRODUCER,
					NUM_STEREO_JACK_AUDIO_CHANNELS ));

			consumerMidiHardwareDevices.add( new MidiHardwareDevice( this.getId(),
					"jnajackmidiout",
					"JNAJack Midi Output",
					DeviceDirection.CONSUMER ));

			producerMidiHardwareDevices.add( new MidiHardwareDevice( this.getId(),
					"jnajackmidiin",
					"JNAJack Midi Input",
					DeviceDirection.PRODUCER ) );

			if( doConnect )
			{
				jack = Jack.getInstance();

		        final EnumSet<JackOptions> options = EnumSet.of(JackOptions.JackNoStartServer);
		        final EnumSet<JackStatus> status = EnumSet.noneOf( JackStatus.class );
				client = jack.openClient( jackClientName, options, status, new Object[]{} );

				if( client != null )
				{
					connectedToJack = true;
				}
			}
			try
			{
				audioProviderRegistryService.registerAudioProvider( this );
			}
			catch (final Exception e)
			{
				log.error( e );
			}
		}
		catch(final JackException je )
		{
			final String msg = "JackException caught during JNAJackAudioProvider init()";
			log.error( msg, je );
			throw new ComponentConfigurationException( msg, je );
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			audioProviderRegistryService.unregisterAudioProvider( this );
		}
		catch (final Exception e)
		{
			log.error( e );
		}
		if( doConnect && connectedToJack )
		{
			client.close();
			client = null;
			consumerAudioHardwareDevices.clear();
			producerAudioHardwareDevices.clear();
			consumerMidiHardwareDevices.clear();
			producerMidiHardwareDevices.clear();
			connectedToJack = false;
		}
	}

	public void setAudioProviderRegistryService( final AudioProviderRegistryService audioProviderRegistryService)
	{
		this.audioProviderRegistryService = audioProviderRegistryService;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setTimingService( final TimingService timingService )
	{
		this.timingService = timingService;
	}

	public void setAppRenderingService( final AppRenderingService appRenderingService )
	{
		this.appRenderingService = appRenderingService;
	}

	@Override
	public List<? extends MidiHardwareDevice> getAllConsumerMidiDevices(
			final MidiHardwareDeviceCriteria criteria ) throws DatastoreException
	{
		return consumerMidiHardwareDevices;
	}

	@Override
	public List<? extends MidiHardwareDevice> getAllProducerMidiDevices(
			final MidiHardwareDeviceCriteria criteria ) throws DatastoreException
	{
		return producerMidiHardwareDevices;
	}

	@Override
	public List<? extends AudioHardwareDevice> getAllProducerAudioDevices(
			final AudioHardwareDeviceCriteria criteria ) throws DatastoreException
	{
		return producerAudioHardwareDevices;
	}

	@Override
	public List<? extends AudioHardwareDevice> getAllConsumerAudioDevices(
			final AudioHardwareDeviceCriteria criteria ) throws DatastoreException
	{
		return consumerAudioHardwareDevices;
	}

	@Override
	public AppRenderingSession createAppRenderingSessionForConfiguration( final HardwareIOConfiguration hardwareIOConfiguration,
			final AppRenderingErrorQueue errorQueue,
			final AppRenderingErrorCallback errorCallback )
		throws DatastoreException
	{
		return new JNAJackAppRenderingSession( appRenderingService, timingService, hardwareIOConfiguration, errorQueue, errorCallback, jack, client );
	}
}
