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

import uk.co.modularaudio.service.apprenderinggraph.AppRenderingGraphService;
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
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemBufferSizeMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemMidiDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.GuiFpsMVCModel;
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

	private ConfigurationService configurationService = null;

	private final static String CONFIG_KEY_PREFS_FILE = UserPreferencesServiceImpl.class.getSimpleName() + ".UserPreferencesFile";

	private static final String PREFS_FILE_GUI_FPS = "GuiFps";
	private static final String DEFAULT_GUI_FPS_STRING = "30";

	private static final String PREFS_FILE_KEY_INPUT_DEVICE = "InputDeviceId";
	private static final String PREFS_FILE_KEY_OUTPUT_DEVICE = "OutputDeviceId";
	private static final String PREFS_FILE_KEY_BUFFER_SIZE = "BufferSize";
	private static final String PREFS_FILE_KEY_INPUT_MIDI_DEVICE = "InputMidiDeviceId";
	private static final String PREFS_FILE_KEY_OUTPUT_MIDI_DEVICE = "OutputMidiDeviceId";

	private static final String DEFAULT_BUFFER_SIZE_STRING = "1024";

	private String userPreferencesFilename = null;
	private File userPreferencesFile = null;
	private Properties userPreferencesProperties = null;

	private AudioProviderRegistryService audioProviderRegistryService = null;
	private AppRenderingGraphService appRenderingGraphService = null;

	private UserPreferencesMVCController userPreferences = null;

	private int guiFps = -1;

	private String outputDeviceId = null;
	private String inputDeviceId = null;
	private int bufferSize = -1;
	private String inputMidiDeviceId = null;
	private String outputMidiDeviceId = null;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( audioProviderRegistryService == null ||
				appRenderingGraphService == null )
		{
			String msg = "UserPreferencesServiceImpl has missing service dependencies. Check configuration";
			throw new ComponentConfigurationException( msg );
		}
		Map<String,String> errors = new HashMap<String, String>();
		userPreferencesFilename = ConfigurationServiceHelper.checkForSingleStringKey(configurationService, CONFIG_KEY_PREFS_FILE, errors);
		ConfigurationServiceHelper.errorCheck( errors );
		try
		{
			readPreferencesFile();
		}
		catch( IOException ioe )
		{
			String msg = "IOException caught reading user preferences: " + ioe.toString();
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

	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public String getSingleUserPreferenceStringByKey(String key, String defaultValue) throws DatastoreException,
			RecordNotFoundException
	{
		String retVal = userPreferencesProperties.getProperty( key );
		if( retVal == null && defaultValue == null )
		{
			String msg = "No such user preference: " + key;
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

	public void setSingleUserPreferenceByKey( String key, String value ) throws DatastoreException
	{
	}

	public int getSingleUserPreferenceIntByKey( String key, Integer defaultValue ) throws DatastoreException,
			RecordNotFoundException
	{
		String val = userPreferencesProperties.getProperty( key );
		Integer retVal = null;
		try
		{
			retVal = Integer.parseInt( val );
		}
		catch( NumberFormatException nfe )
		{
		}

		if( retVal == null && defaultValue == null )
		{
			String msg = "No such user preference: " + key;
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

	public void setAudioProviderRegistryService(AudioProviderRegistryService audioProviderRegistryService)
	{
		this.audioProviderRegistryService = audioProviderRegistryService;
	}

	@Override
	public UserPreferencesMVCController getUserPreferencesMVCController() throws DatastoreException
	{
		UserPreferencesMVCModel model = createUserPreferencesModel();

		userPreferences = new UserPreferencesMVCController( model );

		setupPreferencesSelections();

		return userPreferences;
	}

	@Override
	public void setupPreferencesSelections()
	{
		GuiFpsComboMVCController fcc = userPreferences.getFpsComboController();

		try
		{
			fcc.setSelectedElementById( guiFps + "" );
		}
		catch( RecordNotFoundException e1 )
		{
		}

		OutputDeviceComboMVCController odc = userPreferences.getOutputDeviceComboController();
		InputDeviceComboMVCController idc = userPreferences.getInputDeviceComboController();
		BufferSizeSliderMVCController bsdc = userPreferences.getBufferSizeSliderController();
		InputMidiDeviceComboMVCController imdc = userPreferences.getInputMidiDeviceComboController();
		OutputMidiDeviceComboMVCController omdc = userPreferences.getOutputMidiDeviceComboController();

		if( !inputDeviceId.equals( "" ) )
		{
			try
			{
				idc.setSelectedElementById( inputDeviceId );
			}
			catch (RecordNotFoundException e)
			{
			}
		}
		if( !outputDeviceId.equals( "" ) )
		{
			try
			{
				odc.setSelectedElementById( outputDeviceId );
			}
			catch(RecordNotFoundException e )
			{
			}
		}

		try
		{
			// Translate to model index
			int modelIndex = BufferSizeSliderMVCController.bufferSizeToModelIndexMap.get( bufferSize );
			bsdc.setValue( modelIndex );
		}
		catch (ValueOutOfRangeException e)
		{
			String msg = "ValueOutOfRangeException caught setting buffer size: " + e.toString();
			log.error( msg, e );
		}

		if( !inputMidiDeviceId.equals( "" ) )
		{
			try
			{
				imdc.setSelectedElementById( inputMidiDeviceId );
			}
			catch(RecordNotFoundException rnfe )
			{
			}
		}

		if( !outputMidiDeviceId.equals( "" ) )
		{
			try
			{
				omdc.setSelectedElementById( outputMidiDeviceId );
			}
			catch(RecordNotFoundException rnfe )
			{
			}
		}
	}

	@Override
	public UserPreferencesMVCModel createUserPreferencesModel()
			throws DatastoreException
	{
		List<GuiFpsComboItem> guiFpsItems = new ArrayList<GuiFpsComboItem>();

		guiFpsItems.add( new GuiFpsComboItem("1",  "1") );
		guiFpsItems.add( new GuiFpsComboItem("2",  "2") );
		guiFpsItems.add( new GuiFpsComboItem("5",  "5") );
		guiFpsItems.add( new GuiFpsComboItem("10",  "10") );
		guiFpsItems.add( new GuiFpsComboItem("15",  "15") );
		guiFpsItems.add( new GuiFpsComboItem("30",  "30") );
		guiFpsItems.add( new GuiFpsComboItem("60",  "60") );

		List<AudioSystemDeviceComboItem> inputDeviceItems = new ArrayList<AudioSystemDeviceComboItem>();
		List<AudioSystemDeviceComboItem> outputDeviceItems = new ArrayList<AudioSystemDeviceComboItem>();
		List<AudioSystemMidiDeviceComboItem> inputMidiDeviceItems = new ArrayList<AudioSystemMidiDeviceComboItem>();
		List<AudioSystemMidiDeviceComboItem> outputMidiDeviceItems = new ArrayList<AudioSystemMidiDeviceComboItem>();

		// fill in device combo models with data pulled out from the audio provider registry
		SampleBits sampleBits = SampleBits.SAMPLE_16LE;
		DataRate dataRate = DataRate.SR_44100;
//		DataRate dataRate = DataRate.SR_88200;
//		DataRate dataRate = DataRate.SR_192000;

		AudioHardwareDeviceCriteria criteria = new AudioHardwareDeviceCriteria( sampleBits,
				dataRate,
				bufferSize );

		List<AudioHardwareDevice> iccs = audioProviderRegistryService.getAllProducerAudioDevices( criteria );
		AudioSystemDeviceComboItem emptyInputDevice = new AudioSystemDeviceComboItem( "", "", null );
		inputDeviceItems.add( emptyInputDevice );
		for( AudioHardwareDevice hs : iccs )
		{
			AudioSystemDeviceComboItem ni = new AudioSystemDeviceComboItem( hs.getId(), hs.getUserVisibleName(), hs );
			inputDeviceItems.add( ni );
		}
		List<AudioHardwareDevice> occs = audioProviderRegistryService.getAllConsumerAudioDevices( criteria );
		AudioSystemDeviceComboItem emptyOutputDevice = new AudioSystemDeviceComboItem( "", "", null );
		outputDeviceItems.add( emptyOutputDevice );
		for( AudioHardwareDevice hs : occs )
		{
			AudioSystemDeviceComboItem ni = new AudioSystemDeviceComboItem( hs.getId(), hs.getUserVisibleName(), hs );
			outputDeviceItems.add( ni );
		}

		MidiHardwareDeviceCriteria midiCriteria = new MidiHardwareDeviceCriteria();

		List<MidiHardwareDevice> imcs = audioProviderRegistryService.getAllConsumerMidiDevices( midiCriteria );
		AudioSystemMidiDeviceComboItem emptyMidiDevice = new AudioSystemMidiDeviceComboItem( "", "", null );
		inputMidiDeviceItems.add( emptyMidiDevice );
		for( MidiHardwareDevice mc : imcs )
		{
			AudioSystemMidiDeviceComboItem mci = new AudioSystemMidiDeviceComboItem( mc.getId(), mc.getUserVisibleName(), mc );
			inputMidiDeviceItems.add( mci );
		}

		List<MidiHardwareDevice> omcs = audioProviderRegistryService.getAllProducerMidiDevices( midiCriteria );
		outputMidiDeviceItems.add( emptyMidiDevice );
		for( MidiHardwareDevice mc : omcs )
		{
			AudioSystemMidiDeviceComboItem mci = new AudioSystemMidiDeviceComboItem( mc.getId(), mc.getUserVisibleName(), mc );
			outputMidiDeviceItems.add( mci );
		}

		GuiFpsMVCModel guiFpsComboModel = new GuiFpsMVCModel( guiFpsItems );

		AudioSystemDeviceMVCModel inputDeviceComboModel = new AudioSystemDeviceMVCModel( inputDeviceItems );
		AudioSystemDeviceMVCModel outputDeviceComboModel = new AudioSystemDeviceMVCModel( outputDeviceItems );
		AudioSystemBufferSizeMVCModel bufferSizeModel = new AudioSystemBufferSizeMVCModel();
		AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel = new AudioSystemMidiDeviceMVCModel( inputMidiDeviceItems );
		AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel = new AudioSystemMidiDeviceMVCModel( outputMidiDeviceItems );

		UserPreferencesMVCModel model = new UserPreferencesMVCModel(
				guiFpsComboModel,
				inputDeviceComboModel,
				outputDeviceComboModel,
				bufferSizeModel,
				inputMidiDeviceComboModel,
				outputMidiDeviceComboModel );
		return model;
	}

	@Override
	public void applyUserPreferencesChanges( UserPreferencesMVCController userPreferencesModelController )
			throws DatastoreException
	{
		FileOutputStream os = null;
		try
		{
			UserPreferencesMVCModel userPrefsMVCModel = userPreferencesModelController.getModel();
			GuiFpsMVCModel guiFpsComboModel = userPrefsMVCModel.getFpsComboModel();
			GuiFpsComboItem guiFpsItem = guiFpsComboModel.getSelectedElement();
			AudioSystemDeviceMVCModel inputDeviceComboModel = userPrefsMVCModel.getInputDeviceComboModel();
			AudioSystemDeviceComboItem inputDeviceComboItem = inputDeviceComboModel.getSelectedElement();
			AudioSystemDeviceMVCModel outputDeviceComboModel = userPrefsMVCModel.getOutputDeviceComboModel();
			AudioSystemDeviceComboItem outputDeviceComboItem = outputDeviceComboModel.getSelectedElement();
			AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel = userPrefsMVCModel.getInputMidiDeviceComboModel();
			AudioSystemMidiDeviceComboItem inputMidiDeviceComboItem = inputMidiDeviceComboModel.getSelectedElement();
			AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel = userPrefsMVCModel.getOutputMidiDeviceComboModel();
			AudioSystemMidiDeviceComboItem outputMidiDeviceComboItem = outputMidiDeviceComboModel.getSelectedElement();

			userPreferencesProperties.put( PREFS_FILE_GUI_FPS, guiFpsItem.getId() );
			userPreferencesProperties.put( PREFS_FILE_KEY_INPUT_DEVICE,
					(inputDeviceComboItem == null ? "" : inputDeviceComboItem.getId() ));
			userPreferencesProperties.put( PREFS_FILE_KEY_OUTPUT_DEVICE,
					(outputDeviceComboItem == null ? "" : outputDeviceComboItem.getId() ));
			userPreferencesProperties.put( PREFS_FILE_KEY_INPUT_MIDI_DEVICE,
					(inputMidiDeviceComboItem == null ? "" : inputMidiDeviceComboItem.getId() ));
			userPreferencesProperties.put( PREFS_FILE_KEY_OUTPUT_MIDI_DEVICE,
					(outputMidiDeviceComboItem == null ? "" : outputMidiDeviceComboItem.getId() ));

			AudioSystemBufferSizeMVCModel bufferSizeModel = userPrefsMVCModel.getBufferSizeModel();

			int bufferSizeModelIndex = bufferSizeModel.getValue();
			int bufferSize = BufferSizeSliderMVCController.modelIndexToBufferSizeMap.get( bufferSizeModelIndex );
			userPreferencesProperties.put( PREFS_FILE_KEY_BUFFER_SIZE,
					bufferSize + "" );
			os = new FileOutputStream( userPreferencesFile );
			userPreferencesProperties.store( os, "" );
			os.close();
			os = null;
			readPreferencesFile();
		}
		catch( Exception e )
		{
			String msg = "Exception caught storing user preferences: " + e.toString();
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
				catch(Exception e)
				{
					String msg = "Exception caught closing prefs outputstream: " + e.toString();
					log.error( msg,e );
				}
			}
		}
	}

	public void setAppRenderingGraphService(
			AppRenderingGraphService appRenderingGraphService )
	{
		this.appRenderingGraphService = appRenderingGraphService;
	}

}
