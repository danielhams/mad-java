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

package uk.co.modularaudio.componentdesigner.preferences;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.service.gui.valueobjects.UserPreferencesMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesGuiFpsMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesInputDeviceMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesInputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesOutputDeviceMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesOutputMidiDeviceMVCView;
import uk.co.modularaudio.util.exception.DatastoreException;

public class PreferencesAudioSystemPage extends JPanel
{
	private static final long serialVersionUID = 3660928151573358001L;

//	private static Log log = LogFactory.getLog( PreferencesAudioSystemPage.class.getName() );

//	private GuiFrontController fc = null;
//	private PreferencesDialog preferencesDialog = null;
	private UserPreferencesMVCView userPreferencesView = null;

	private UserPreferencesGuiFpsMVCView fpsCombo = null;

	private UserPreferencesInputDeviceMVCView inputDeviceCombo = null;
	private UserPreferencesOutputDeviceMVCView outputDeviceCombo = null;
//	private UserPreferencesBufferSizeMVCView bufferSizeSlider = null;
	private UserPreferencesInputMidiDeviceMVCView inputMidiDeviceCombo = null;
	private UserPreferencesOutputMidiDeviceMVCView outputMidiDeviceCombo = null;

	public PreferencesAudioSystemPage( ComponentDesignerFrontController fc, PreferencesDialog preferencesDialog ) throws DatastoreException
	{
//		this.fc = fc;
//		this.preferencesDialog = preferencesDialog;
		this.userPreferencesView = preferencesDialog.getUserPreferencesView();

		String migLayoutString = "fill";
		this.setLayout( new MigLayout( migLayoutString ) );

		JPanel deviceChoicePanel = getDeviceChoicePanel();
		this.add( deviceChoicePanel, "grow, shrink" );
		this.userPreferencesView = preferencesDialog.getUserPreferencesView();

		this.validate();
	}

	private JPanel getDeviceChoicePanel()
	{
		JPanel retVal = new JPanel();
//		String migLayoutString = "debug, center";
		String migLayoutString = "center";
		String rowLayoutString = "";
		String colLayoutString = "[][fill,grow,shrink]";
		retVal.setLayout( new MigLayout( migLayoutString, colLayoutString, rowLayoutString ));

		JLabel fpsLabel = new JLabel("Gui FPS:" );
		retVal.add( fpsLabel, "align right");
		fpsCombo = userPreferencesView.getGuiFpsMVCView();
		retVal.add( fpsCombo, "growx, shrink, wrap" );

		JLabel inputDeviceLabel = new JLabel( "Input Device:" );
		retVal.add( inputDeviceLabel, "align right" );
		inputDeviceCombo = userPreferencesView.getInputDeviceMVCView();
		retVal.add( inputDeviceCombo, "growx, shrink, wrap" );

		JLabel outputDeviceLabel = new JLabel( "Output Device:" );
		retVal.add( outputDeviceLabel, "align right" );
		outputDeviceCombo = userPreferencesView.getOutputDeviceMVCView();
		retVal.add( outputDeviceCombo, "growx, shrink, wrap" );

		// Only supporting jack for now, so no issue with buffer size selection
//		JLabel clockLabel = new JLabel( "Buffer Size:" );
//		retVal.add( clockLabel, "align right, wrap" );
//		bufferSizeSlider = userPreferencesView.getBufferSizeMVCView();
//		retVal.add( bufferSizeSlider, "wrap");

		inputMidiDeviceCombo = userPreferencesView.getInputMidiDeviceMVCView();
		JLabel inputMidiLabel = new JLabel( "Midi In" );
		retVal.add(  inputMidiLabel, "align right" );
		retVal.add( inputMidiDeviceCombo, "growx, shrink, wrap" );

		outputMidiDeviceCombo = userPreferencesView.getOutputMidiDeviceMVCView();
		JLabel outputMidiLabel = new JLabel( "Midi Out" );
		retVal.add(  outputMidiLabel, "align right" );
		retVal.add( outputMidiDeviceCombo, "growx, shrink, wrap" );

		return retVal;
	}
}
