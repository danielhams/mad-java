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
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesActions.ApplyPreferencesChangesAction;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesActions.CancelPreferencesChangesAction;
import uk.co.modularaudio.service.gui.valueobjects.UserPreferencesMVCView;
import uk.co.modularaudio.util.exception.DatastoreException;

public class PreferencesDialog extends JDialog implements WindowListener
{
	private static final long serialVersionUID = 6978499345998012496L;
//	private final static Log log = LogFactory.getLog( PreferencesDialog.class.getName() );
	static final String AUDIO_PREFS_INVALID_MESSAGE = "Your audio sytem configuration must be valid to continue";
	
	private ComponentDesignerFrontController fc = null;
	
	private JTabbedPane tabbedPane = null;
	private Map<PreferencesDialogPageEnum, Component> pageToComponentMap = null;
	
	private JPanel basePanel = null;
	private JPanel tabsPanel = null;
	private JPanel buttonsPanel = null;
	
	private JButton cancelChangesButton = null;
	private JButton applyChangesButton = null;
	
	private UserPreferencesMVCView userPreferencesView = null;
	
	public PreferencesDialog( ComponentDesignerFrontController fc, Frame parentFrame ) throws DatastoreException
	{
		// Make it model based on parent frame
		super( (Frame)null, false );
		this.setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		this.fc = fc;

		this.userPreferencesView = fc.getUserPreferencesMVCView();
		
		basePanel = new JPanel();
		basePanel.setLayout( new MigLayout( "fill, flowy" ) );
		this.add( basePanel );
		
		tabsPanel = new JPanel();
		tabsPanel.setLayout( new MigLayout( "fill" ) );
		basePanel.add( tabsPanel, "growx, shrink" );
		
		getButtonsPanel();
		basePanel.add( buttonsPanel, "grow, shrink" );
		
		this.pack();
		
		this.addWindowListener( this );
	}
	
	public UserPreferencesMVCView getUserPreferencesView()
	{
		return userPreferencesView;
	}

	public JPanel getButtonsPanel()
	{
		if( buttonsPanel == null )
		{
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout( new MigLayout( "fill" ) );
			cancelChangesButton = new JButton( "Cancel Changes" );
			buttonsPanel.add( cancelChangesButton, "align left, aligny bottom" );
			applyChangesButton = new JButton( "Apply Changes" );
			buttonsPanel.add( applyChangesButton, "align right, aligny bottom" );
		}
		return buttonsPanel;
	}

	public void choosePage( PreferencesDialogPageEnum page )
	{
		this.validate();
		this.pack();
		Component c = pageToComponentMap.get( page );
		c.setVisible( true );
		tabbedPane.setSelectedComponent( c );
	}

	public void setPreferencesTabbedFrame( JTabbedPane tabbedPane, Map<PreferencesDialogPageEnum, Component> pageToComponentMap )
	{
		this.pageToComponentMap = pageToComponentMap;
		this.tabbedPane = tabbedPane;
		this.tabsPanel.add( tabbedPane, "grow" );
	}

	public void close()
	{
		this.setVisible( false );
	}

	public void registerCancelAction(CancelPreferencesChangesAction cancelAction)
	{
		cancelChangesButton.setAction( cancelAction );
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		this.close();
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	public void registerApplyAction( ApplyPreferencesChangesAction applyAction )
	{
		applyChangesButton.setAction( applyAction );
	}

	public void loadPreferences()
	{
		fc.reloadUserPreferences();
	}
}
