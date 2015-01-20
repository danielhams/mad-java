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
import uk.co.modularaudio.componentdesigner.preferences.newhardware.PreferencesHardwarePage;
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
	private static final String TAB_TITLE_HARDWARE_PAGE = "Audio Hardware";

	private ComponentDesignerFrontController fc = null;
//	private ComponentImageFactory cif = null;
//	private ConfigurationService cs = null;
	private PreferencesDialog preferencesDialog = null;
//	private PreferencesActions actions = null;

	private JTabbedPane tabbedPane = null;

	private PreferencesGeneralPage generalPage = null;
	private PreferencesAudioSystemPage audioSystemPage = null;
	private PreferencesHardwarePage hardwarePage = null;

	public PreferencesManipulator( ComponentDesignerFrontController fc,
			ComponentImageFactory cif,
			ConfigurationService cs,
			PreferencesDialog preferencesDialog,
			PreferencesActions actions ) throws DatastoreException
	{
		this.fc = fc;
//		this.cif = cif;
//		this.cs = cs;
		this.preferencesDialog = preferencesDialog;
//		this.actions = actions;

		preferencesDialog.setTitle( PREFERENCES_TITLE );
		preferencesDialog.setSize( GuiConstants.GUI_PREFERENCES_DEFAULT_DIMENSIONS );
		preferencesDialog.setMinimumSize( GuiConstants.GUI_PREFERENCES_MINIMUM_DIMENSIONS );
		preferencesDialog.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );

		Map<PreferencesDialogPageEnum, Component> pageToComponentMap = new HashMap<PreferencesDialogPageEnum, Component>();
		pageToComponentMap.put( PreferencesDialogPageEnum.GENERAL, getGeneralPage() );
		pageToComponentMap.put( PreferencesDialogPageEnum.AUDIO_SYSTEM, getAudioSystemPage());
		pageToComponentMap.put( PreferencesDialogPageEnum.HARDWARE_PAGE, getHardwarePage() );
		preferencesDialog.setPreferencesTabbedFrame( getContentFrame(), pageToComponentMap );

		preferencesDialog.registerCancelAction( actions.getCancelAction() );
		preferencesDialog.registerApplyAction( actions.getApplyAction() );

		// Register our global keys
//		GlobalKeyHelper.setupKeys( menubar, actions );
//		GlobalKeyHelper.setupKeys( contentFrame, actions );
//		GlobalKeyHelper.setupKeys( scrollableDesigner, actions );
	}

	private JTabbedPane getContentFrame() throws DatastoreException
	{
		if( tabbedPane == null )
		{
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab( TAB_TITLE_GENERAL, getGeneralPage() );
			tabbedPane.addTab( TAB_TITLE_AUDIO_SYSTEM, getAudioSystemPage() );
			tabbedPane.addTab( TAB_TITLE_HARDWARE_PAGE, getHardwarePage() );
		}
		return tabbedPane;
	}

	private Component getGeneralPage()
	{
		if( generalPage == null )
		{
			generalPage = new PreferencesGeneralPage();
		}
		return generalPage;
	}

	private Component getAudioSystemPage() throws DatastoreException
	{
		if( audioSystemPage == null )
		{
			audioSystemPage = new PreferencesAudioSystemPage( fc, preferencesDialog );
		}
		return audioSystemPage;
	}

	private Component getHardwarePage() throws DatastoreException
	{
		if( hardwarePage == null )
		{
			hardwarePage = new PreferencesHardwarePage( fc, preferencesDialog );
		}
		return hardwarePage;
	}
}
