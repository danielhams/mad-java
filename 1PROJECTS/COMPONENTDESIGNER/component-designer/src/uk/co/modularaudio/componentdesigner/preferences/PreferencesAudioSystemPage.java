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
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesGuiFpsMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesInputDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesInputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesOutputDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesOutputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesRenderingCoresView;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class PreferencesAudioSystemPage extends JPanel
{
	private static final long serialVersionUID = 3660928151573358001L;

//	private static Log log = LogFactory.getLog( PreferencesAudioSystemPage.class.getName() );

	private final UserPreferencesMVCView userPreferencesView;

	private final UserPreferencesRenderingCoresView renderingCoresView;

	private final UserPreferencesGuiFpsMVCView fpsCombo;

	private final UserPreferencesInputDeviceMVCView inputDeviceCombo;
	private final UserPreferencesOutputDeviceMVCView outputDeviceCombo;

	private final UserPreferencesInputMidiDeviceMVCView inputMidiDeviceCombo;
	private final UserPreferencesOutputMidiDeviceMVCView outputMidiDeviceCombo;

	public PreferencesAudioSystemPage( final ComponentDesignerFrontController fc, final PreferencesDialog preferencesDialog ) throws DatastoreException
	{
		this.userPreferencesView = preferencesDialog.getUserPreferencesView();

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );

		this.setLayout( msh.createMigLayout() );

		final JPanel deviceChoicePanel = new JPanel();

		final String dcLayoutString = "center";
		final String rowLayoutString = "";
		final String colLayoutString = "[][fill,grow,shrink]";
		deviceChoicePanel.setLayout( new MigLayout( dcLayoutString, colLayoutString, rowLayoutString ));

		final JLabel renderingCoresLabel = new JLabel("Rendering Cores:" );
		deviceChoicePanel.add( renderingCoresLabel, "align right");
		renderingCoresView = userPreferencesView.getRenderingCoresView();
		deviceChoicePanel.add( renderingCoresView, "grow 0, wrap" );

		final JLabel fpsLabel = new JLabel("Gui FPS:" );
		deviceChoicePanel.add( fpsLabel, "align right");
		fpsCombo = userPreferencesView.getGuiFpsMVCView();
		deviceChoicePanel.add( fpsCombo, "growx, shrink, wrap" );

		final JLabel inputDeviceLabel = new JLabel( "Input Device:" );
		deviceChoicePanel.add( inputDeviceLabel, "align right" );
		inputDeviceCombo = userPreferencesView.getInputDeviceMVCView();
		deviceChoicePanel.add( inputDeviceCombo, "growx, shrink, wrap" );

		final JLabel outputDeviceLabel = new JLabel( "Output Device:" );
		deviceChoicePanel.add( outputDeviceLabel, "align right" );
		outputDeviceCombo = userPreferencesView.getOutputDeviceMVCView();
		deviceChoicePanel.add( outputDeviceCombo, "growx, shrink, wrap" );

		inputMidiDeviceCombo = userPreferencesView.getInputMidiDeviceMVCView();
		final JLabel inputMidiLabel = new JLabel( "Midi In" );
		deviceChoicePanel.add(  inputMidiLabel, "align right" );
		deviceChoicePanel.add( inputMidiDeviceCombo, "growx, shrink, wrap" );

		outputMidiDeviceCombo = userPreferencesView.getOutputMidiDeviceMVCView();
		final JLabel outputMidiLabel = new JLabel( "Midi Out" );
		deviceChoicePanel.add(  outputMidiLabel, "align right" );
		deviceChoicePanel.add( outputMidiDeviceCombo, "growx, shrink, wrap" );
		this.add( deviceChoicePanel, "grow, shrink" );

		this.validate();
	}
}
