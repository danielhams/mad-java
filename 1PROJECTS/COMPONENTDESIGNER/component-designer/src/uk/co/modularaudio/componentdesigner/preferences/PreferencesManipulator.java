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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.rack.GuiConstants;
import uk.co.modularaudio.util.exception.DatastoreException;

public class PreferencesManipulator
{
//	private static Log log = LogFactory.getLog( PreferencesManipulator.class.getName());

	private static final String PREFERENCES_TITLE = "Component Designer Preferences";

	// Titles for the tabs
	private static final String TAB_TITLE_GENERAL = "General";
	private static final String TAB_TITLE_AUDIO_SYSTEM = "Audio System";

	private final JTabbedPane tabbedPane;

	private final PreferencesGeneralPage generalPage;
	private final PreferencesAudioSystemPage audioSystemPage;

	public PreferencesManipulator( final ComponentDesignerFrontController fc,
			final ComponentImageFactory cif,
			final ConfigurationService cs,
			final PreferencesDialog preferencesDialog,
			final PreferencesActions actions ) throws DatastoreException
	{
		preferencesDialog.setTitle( PREFERENCES_TITLE );
		preferencesDialog.setSize( GuiConstants.GUI_PREFERENCES_DEFAULT_DIMENSIONS );
		preferencesDialog.setMinimumSize( GuiConstants.GUI_PREFERENCES_MINIMUM_DIMENSIONS );
		preferencesDialog.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );

		final Map<PreferencesDialogPageEnum, Component> pageToComponentMap = new HashMap<PreferencesDialogPageEnum, Component>();
		generalPage = new PreferencesGeneralPage( fc, preferencesDialog );
		pageToComponentMap.put( PreferencesDialogPageEnum.GENERAL, generalPage );

		audioSystemPage = new PreferencesAudioSystemPage( fc, preferencesDialog );
		pageToComponentMap.put( PreferencesDialogPageEnum.AUDIO_SYSTEM, audioSystemPage );

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab( TAB_TITLE_GENERAL, generalPage );
		tabbedPane.addTab( TAB_TITLE_AUDIO_SYSTEM, audioSystemPage );
		preferencesDialog.setPreferencesTabbedFrame( tabbedPane, pageToComponentMap );

		preferencesDialog.registerCancelAction( actions.getCancelAction() );
		preferencesDialog.registerApplyAction( actions.getApplyAction() );

		// Register our global keys
//		GlobalKeyHelper.setupKeys( menubar, actions );
//		GlobalKeyHelper.setupKeys( contentFrame, actions );
//		GlobalKeyHelper.setupKeys( scrollableDesigner, actions );
	}
}
