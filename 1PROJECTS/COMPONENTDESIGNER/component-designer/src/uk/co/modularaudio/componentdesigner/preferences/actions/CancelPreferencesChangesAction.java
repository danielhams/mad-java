package uk.co.modularaudio.componentdesigner.preferences.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesActions;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;

public class CancelPreferencesChangesAction extends AbstractAction
{
	private static final long serialVersionUID = 5378624881852594498L;

	private final ComponentDesignerFrontController fc;

	private final PreferencesDialog preferencesDialog;

	public CancelPreferencesChangesAction( final ComponentDesignerFrontController fc, final PreferencesDialog preferencesDialog )
	{
		this.fc = fc;
		this.preferencesDialog = preferencesDialog;
		this.putValue(NAME, PreferencesActions.CANCEL_PREFERENCES_NAME );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		fc.cancelUserPreferencesChanges();
		fc.reloadUserPreferences();
		preferencesDialog.close();
	}
}