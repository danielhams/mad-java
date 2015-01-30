package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;

public class ShowPreferencesAction extends AbstractAction
{
	private static final long serialVersionUID = -5903263092723112562L;

	private final ComponentDesignerFrontController fc;

	private final MainFrame mainFrame;

	private final PreferencesDialog preferencesDialog;

	public ShowPreferencesAction( final ComponentDesignerFrontController fc,
			final MainFrame mainFrame,
			final PreferencesDialog preferencesDialog )
	{
		this.fc = fc;
		this.mainFrame = mainFrame;
		this.preferencesDialog = preferencesDialog;
		this.putValue( NAME, "Preferences" );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		fc.reloadUserPreferences();
		preferencesDialog.setLocationRelativeTo( mainFrame );
		preferencesDialog.setVisible( true );
	}
}
