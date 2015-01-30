package uk.co.modularaudio.componentdesigner.preferences.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesActions;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;
import uk.co.modularaudio.util.audio.gui.mad.rack.GuiConstants;

public class ApplyPreferencesChangesAction extends AbstractAction
{
	private static final long serialVersionUID = -4903439573172278487L;

	private final ComponentDesignerFrontController fc;
	private final PreferencesDialog preferencesDialog;

	public ApplyPreferencesChangesAction( final ComponentDesignerFrontController fc, final PreferencesDialog pd )
	{
		this.fc = fc;
		this.preferencesDialog = pd;
		this.putValue(NAME, PreferencesActions.APPLY_PREFERENCES_NAME );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		final boolean wasRendering = fc.isRendering();

		if( wasRendering )
		{
			fc.toggleRendering();
		}
		if( fc.testUserPreferencesChanges() )
		{
			fc.applyUserPreferencesChanges();
			preferencesDialog.close();
		}
		else
		{
			// Pop up a warning message, but don't close the dialog.
			fc.showMessageDialog( preferencesDialog,
					PreferencesDialog.AUDIO_PREFS_INVALID_MESSAGE,
					GuiConstants.DIALOG_UNABLE_TO_PERFORM_TITLE,
					JOptionPane.WARNING_MESSAGE, null );
		}
		if( wasRendering )
		{
			fc.toggleRendering();
		}
	}
}