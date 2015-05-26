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

package uk.co.modularaudio.service.userpreferences.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioproviderregistry.AudioProviderRegistryService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.comboitems.AudioSystemDeviceComboItem;
import uk.co.modularaudio.service.userpreferences.mvc.comboitems.AudioSystemMidiDeviceComboItem;
import uk.co.modularaudio.service.userpreferences.mvc.comboitems.GuiFpsComboItem;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.BufferSizeSliderMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.GuiFpsComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.InputDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.InputMidiDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.OutputDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.OutputMidiDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.RenderingCoresMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemBufferSizeMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemMidiDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.GuiFpsMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.RenderingCoresMVCModel;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.format.SampleBits;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDeviceCriteria;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDeviceCriteria;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.mvc.intslider.ValueOutOfRangeException;

public class UserPreferencesServiceImpl implements ComponentWithLifecycle, UserPreferencesService
{
	private static Log log = LogFactory.getLog( UserPreferencesServiceImpl.class.getName() );

	private ConfigurationService configurationService;

	private final static String CONFIG_KEY_PREFS_FILE = UserPreferencesServiceImpl.class.getSimpleName() + ".UserPreferencesFile";

	private static final String PREFS_FILE_RENDERING_CORES = "RenderingCores";
	private static final String DEFAULT_RENDERING_CORES_STRING = "1";
	private static final String PREFS_FILE_GUI_FPS = "GuiFps";
	private static final String DEFAULT_GUI_FPS_STRING = "30";

	private static final String PREFS_FILE_KEY_INPUT_DEVICE = "InputDeviceId";
	private static final String PREFS_FILE_KEY_OUTPUT_DEVICE = "OutputDeviceId";
	private static final String PREFS_FILE_KEY_BUFFER_SIZE = "BufferSize";
	private static final String PREFS_FILE_KEY_INPUT_MIDI_DEVICE = "InputMidiDeviceId";
	private static final String PREFS_FILE_KEY_OUTPUT_MIDI_DEVICE = "OutputMidiDeviceId";

	private static final String DEFAULT_BUFFER_SIZE_STRING = "1024";

	private String userPreferencesFilename;
	private File userPreferencesFile;
	private Properties userPreferencesProperties;

	private AudioProviderRegistryService audioProviderRegistryService;

	private UserPreferencesMVCController userPreferences;

	private int renderingCores = -1;

	private int guiFps = -1;

	private String outputDeviceId;
	private String inputDeviceId;
	private int bufferSize = -1;
	private String inputMidiDeviceId;
	private String outputMidiDeviceId;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( audioProviderRegistryService == null )
		{
			final String msg = "UserPreferencesServiceImpl has missing service dependencies. Check configuration";
			throw new ComponentConfigurationException( msg );
		}
		final Map<String,String> errors = new HashMap<String, String>();
		userPreferencesFilename = ConfigurationServiceHelper.checkForSingleStringKey(configurationService, CONFIG_KEY_PREFS_FILE, errors);
		ConfigurationServiceHelper.errorCheck( errors );
		try
		{
			readPreferencesFile();
		}
		catch( final IOException ioe )
		{
			final String msg = "IOException caught reading user preferences: " + ioe.toString();
			log.error( msg, ioe );
			throw new ComponentConfigurationException( msg, ioe );
		}
	}

	private void readPreferencesFile() throws IOException
	{
		userPreferencesFile = new File( userPreferencesFilename );
		userPreferencesProperties = new Properties();
		if( userPreferencesFile.exists() && userPreferencesFile.canRead() )
		{
			userPreferencesProperties.load( new FileInputStream( userPreferencesFile ));
		}
		renderingCores = Integer.parseInt( userPreferencesProperties.getProperty( PREFS_FILE_RENDERING_CORES, DEFAULT_RENDERING_CORES_STRING ) );
		guiFps = Integer.parseInt( userPreferencesProperties.getProperty( PREFS_FILE_GUI_FPS, DEFAULT_GUI_FPS_STRING ) );
		bufferSize = Integer.parseInt( userPreferencesProperties.getProperty(PREFS_FILE_KEY_BUFFER_SIZE, DEFAULT_BUFFER_SIZE_STRING ) );
		outputDeviceId = userPreferencesProperties.getProperty(PREFS_FILE_KEY_OUTPUT_DEVICE, "");
		inputDeviceId = userPreferencesProperties.getProperty( PREFS_FILE_KEY_INPUT_DEVICE, "" );
		inputMidiDeviceId = userPreferencesProperties.getProperty( PREFS_FILE_KEY_INPUT_MIDI_DEVICE, "" );
		outputMidiDeviceId = userPreferencesProperties.getProperty( PREFS_FILE_KEY_OUTPUT_MIDI_DEVICE, "" );
	}

	@Override
	public void destroy()
	{
	}

	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public String getSingleUserPreferenceStringByKey( final String key,
			final String defaultValue)
		throws DatastoreException, RecordNotFoundException
	{
		final String retVal = userPreferencesProperties.getProperty( key );
		if( retVal == null && defaultValue == null )
		{
			final String msg = "No such user preference: " + key;
			throw new RecordNotFoundException( msg );
		}
		else if( retVal == null && defaultValue != null )
		{
			return defaultValue;
		}
		else
		{
			return retVal;
		}
	}

	public void setSingleUserPreferenceByKey( final String key, final String value ) throws DatastoreException
	{
	}

	public int getSingleUserPreferenceIntByKey( final String key, final Integer defaultValue ) throws DatastoreException,
			RecordNotFoundException
	{
		final String val = userPreferencesProperties.getProperty( key );
		Integer retVal = null;
		try
		{
			retVal = Integer.parseInt( val );
		}
		catch( final NumberFormatException nfe )
		{
		}

		if( retVal == null && defaultValue == null )
		{
			final String msg = "No such user preference: " + key;
			throw new RecordNotFoundException( msg );
		}
		else if( retVal == null && defaultValue != null )
		{
			return defaultValue;
		}
		else
		{
			return retVal;
		}
	}

	public void setAudioProviderRegistryService( final AudioProviderRegistryService audioProviderRegistryService )
	{
		this.audioProviderRegistryService = audioProviderRegistryService;
	}

	@Override
	public UserPreferencesMVCController getUserPreferencesMVCController() throws DatastoreException
	{
		final UserPreferencesMVCModel model = createUserPreferencesModel();

		userPreferences = new UserPreferencesMVCController( model );

		setupPreferencesSelections();

		return userPreferences;
	}

	@Override
	public void setupPreferencesSelections()
	{
		final RenderingCoresMVCController rcc = userPreferences.getRenderingCoresController();
		try
		{
			rcc.setValue( renderingCores );
		}
		catch( final ValueOutOfRangeException e2 )
		{
			e2.printStackTrace();
		}

		final GuiFpsComboMVCController fcc = userPreferences.getFpsComboController();

		try
		{
			fcc.setSelectedElementById( guiFps + "" );
		}
		catch( final RecordNotFoundException e1 )
		{
			e1.printStackTrace();
		}

		final OutputDeviceComboMVCController odc = userPreferences.getOutputDeviceComboController();
		final InputDeviceComboMVCController idc = userPreferences.getInputDeviceComboController();
		final BufferSizeSliderMVCController bsdc = userPreferences.getBufferSizeSliderController();
		final InputMidiDeviceComboMVCController imdc = userPreferences.getInputMidiDeviceComboController();
		final OutputMidiDeviceComboMVCController omdc = userPreferences.getOutputMidiDeviceComboController();

		if( !inputDeviceId.equals( "" ) )
		{
			try
			{
				idc.setSelectedElementById( inputDeviceId );
			}
			catch (final RecordNotFoundException e)
			{
			}
		}
		if( !outputDeviceId.equals( "" ) )
		{
			try
			{
				odc.setSelectedElementById( outputDeviceId );
			}
			catch(final RecordNotFoundException e )
			{
			}
		}

		try
		{
			// Translate to model index
			final int modelIndex = BufferSizeSliderMVCController.BUF_SIZE_TO_INDEX_MAP.get( bufferSize );
			bsdc.setValue( modelIndex );
		}
		catch (final ValueOutOfRangeException e)
		{
			final String msg = "ValueOutOfRangeException caught setting buffer size: " + e.toString();
			log.error( msg, e );
		}

		if( !inputMidiDeviceId.equals( "" ) )
		{
			try
			{
				imdc.setSelectedElementById( inputMidiDeviceId );
			}
			catch(final RecordNotFoundException rnfe )
			{
			}
		}

		if( !outputMidiDeviceId.equals( "" ) )
		{
			try
			{
				omdc.setSelectedElementById( outputMidiDeviceId );
			}
			catch(final RecordNotFoundException rnfe )
			{
			}
		}
	}

	@Override
	public UserPreferencesMVCModel createUserPreferencesModel()
			throws DatastoreException
	{
		final List<GuiFpsComboItem> guiFpsItems = new ArrayList<GuiFpsComboItem>();

		guiFpsItems.add( new GuiFpsComboItem("1",  "1") );
		guiFpsItems.add( new GuiFpsComboItem("2",  "2") );
		guiFpsItems.add( new GuiFpsComboItem("5",  "5") );
		guiFpsItems.add( new GuiFpsComboItem("10",  "10") );
		guiFpsItems.add( new GuiFpsComboItem("15",  "15") );
		guiFpsItems.add( new GuiFpsComboItem("30",  "30") );
		guiFpsItems.add( new GuiFpsComboItem("60",  "60") );

		final List<AudioSystemDeviceComboItem> inputDeviceItems = new ArrayList<AudioSystemDeviceComboItem>();
		final List<AudioSystemDeviceComboItem> outputDeviceItems = new ArrayList<AudioSystemDeviceComboItem>();
		final List<AudioSystemMidiDeviceComboItem> inputMidiDeviceItems = new ArrayList<AudioSystemMidiDeviceComboItem>();
		final List<AudioSystemMidiDeviceComboItem> outputMidiDeviceItems = new ArrayList<AudioSystemMidiDeviceComboItem>();

		// fill in device combo models with data pulled out from the audio provider registry
		final SampleBits sampleBits = SampleBits.SAMPLE_16LE;
		final DataRate dataRate = DataRate.SR_44100;
//		DataRate dataRate = DataRate.SR_88200;
//		DataRate dataRate = DataRate.SR_192000;

		final AudioHardwareDeviceCriteria criteria = new AudioHardwareDeviceCriteria( sampleBits,
				dataRate,
				bufferSize );

		final List<AudioHardwareDevice> iccs = audioProviderRegistryService.getAllProducerAudioDevices( criteria );
		final AudioSystemDeviceComboItem emptyInputDevice = new AudioSystemDeviceComboItem( "", "", null );
		inputDeviceItems.add( emptyInputDevice );
		for( final AudioHardwareDevice hs : iccs )
		{
			final AudioSystemDeviceComboItem ni = new AudioSystemDeviceComboItem( hs.getId(), hs.getUserVisibleName(), hs );
			inputDeviceItems.add( ni );
		}
		final List<AudioHardwareDevice> occs = audioProviderRegistryService.getAllConsumerAudioDevices( criteria );
		final AudioSystemDeviceComboItem emptyOutputDevice = new AudioSystemDeviceComboItem( "", "", null );
		outputDeviceItems.add( emptyOutputDevice );
		for( final AudioHardwareDevice hs : occs )
		{
			final AudioSystemDeviceComboItem ni = new AudioSystemDeviceComboItem( hs.getId(), hs.getUserVisibleName(), hs );
			outputDeviceItems.add( ni );
		}

		final MidiHardwareDeviceCriteria midiCriteria = new MidiHardwareDeviceCriteria();

		final List<MidiHardwareDevice> imcs = audioProviderRegistryService.getAllConsumerMidiDevices( midiCriteria );
		final AudioSystemMidiDeviceComboItem emptyMidiDevice = new AudioSystemMidiDeviceComboItem( "", "", null );
		inputMidiDeviceItems.add( emptyMidiDevice );
		for( final MidiHardwareDevice mc : imcs )
		{
			final AudioSystemMidiDeviceComboItem mci = new AudioSystemMidiDeviceComboItem( mc.getId(), mc.getUserVisibleName(), mc );
			inputMidiDeviceItems.add( mci );
		}

		final List<MidiHardwareDevice> omcs = audioProviderRegistryService.getAllProducerMidiDevices( midiCriteria );
		outputMidiDeviceItems.add( emptyMidiDevice );
		for( final MidiHardwareDevice mc : omcs )
		{
			final AudioSystemMidiDeviceComboItem mci = new AudioSystemMidiDeviceComboItem( mc.getId(), mc.getUserVisibleName(), mc );
			outputMidiDeviceItems.add( mci );
		}

		final int maxCores = Runtime.getRuntime().availableProcessors();

		final RenderingCoresMVCModel renderingCoresModel = new RenderingCoresMVCModel( maxCores );

		final GuiFpsMVCModel guiFpsComboModel = new GuiFpsMVCModel( guiFpsItems );

		final AudioSystemDeviceMVCModel inputDeviceComboModel = new AudioSystemDeviceMVCModel( inputDeviceItems );
		final AudioSystemDeviceMVCModel outputDeviceComboModel = new AudioSystemDeviceMVCModel( outputDeviceItems );
		final AudioSystemBufferSizeMVCModel bufferSizeModel = new AudioSystemBufferSizeMVCModel();
		final AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel = new AudioSystemMidiDeviceMVCModel( inputMidiDeviceItems );
		final AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel = new AudioSystemMidiDeviceMVCModel( outputMidiDeviceItems );

		final UserPreferencesMVCModel model = new UserPreferencesMVCModel(
				renderingCoresModel,
				guiFpsComboModel,
				inputDeviceComboModel,
				outputDeviceComboModel,
				bufferSizeModel,
				inputMidiDeviceComboModel,
				outputMidiDeviceComboModel );
		return model;
	}

	@Override
	public void applyUserPreferencesChanges( final UserPreferencesMVCController userPreferencesModelController )
			throws DatastoreException
	{
		FileOutputStream os = null;
		try
		{
			final UserPreferencesMVCModel userPrefsMVCModel = userPreferencesModelController.getModel();
			final RenderingCoresMVCModel renderingCoresModel = userPrefsMVCModel.getRenderingCoresModel();
			final GuiFpsMVCModel guiFpsComboModel = userPrefsMVCModel.getFpsComboModel();
			final GuiFpsComboItem guiFpsItem = guiFpsComboModel.getSelectedElement();
			final AudioSystemDeviceMVCModel inputDeviceComboModel = userPrefsMVCModel.getInputDeviceComboModel();
			final AudioSystemDeviceComboItem inputDeviceComboItem = inputDeviceComboModel.getSelectedElement();
			final AudioSystemDeviceMVCModel outputDeviceComboModel = userPrefsMVCModel.getOutputDeviceComboModel();
			final AudioSystemDeviceComboItem outputDeviceComboItem = outputDeviceComboModel.getSelectedElement();
			final AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel = userPrefsMVCModel.getInputMidiDeviceComboModel();
			final AudioSystemMidiDeviceComboItem inputMidiDeviceComboItem = inputMidiDeviceComboModel.getSelectedElement();
			final AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel = userPrefsMVCModel.getOutputMidiDeviceComboModel();
			final AudioSystemMidiDeviceComboItem outputMidiDeviceComboItem = outputMidiDeviceComboModel.getSelectedElement();

			userPreferencesProperties.put( PREFS_FILE_RENDERING_CORES, Integer.toString( renderingCoresModel.getValue() ) );
			userPreferencesProperties.put( PREFS_FILE_GUI_FPS, guiFpsItem.getId() );
			userPreferencesProperties.put( PREFS_FILE_KEY_INPUT_DEVICE,
					(inputDeviceComboItem == null ? "" : inputDeviceComboItem.getId() ));
			userPreferencesProperties.put( PREFS_FILE_KEY_OUTPUT_DEVICE,
					(outputDeviceComboItem == null ? "" : outputDeviceComboItem.getId() ));
			userPreferencesProperties.put( PREFS_FILE_KEY_INPUT_MIDI_DEVICE,
					(inputMidiDeviceComboItem == null ? "" : inputMidiDeviceComboItem.getId() ));
			userPreferencesProperties.put( PREFS_FILE_KEY_OUTPUT_MIDI_DEVICE,
					(outputMidiDeviceComboItem == null ? "" : outputMidiDeviceComboItem.getId() ));

			final AudioSystemBufferSizeMVCModel bufferSizeModel = userPrefsMVCModel.getBufferSizeModel();

			final int bufferSizeModelIndex = bufferSizeModel.getValue();
			final int bufferSize = BufferSizeSliderMVCController.INDEX_TO_BUF_SIZE_MAP.get( bufferSizeModelIndex );
			userPreferencesProperties.put( PREFS_FILE_KEY_BUFFER_SIZE,
					bufferSize + "" );
			os = new FileOutputStream( userPreferencesFile );
			userPreferencesProperties.store( os, "" );
			os.close();
			os = null;
			readPreferencesFile();
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught storing user preferences: " + e.toString();
			log.error( msg, e );
		}
		finally
		{
			if( os != null )
			{
				try
				{
					os.close();
				}
				catch(final Exception e)
				{
					final String msg = "Exception caught closing prefs outputstream: " + e.toString();
					log.error( msg,e );
				}
			}
		}
	}
}
