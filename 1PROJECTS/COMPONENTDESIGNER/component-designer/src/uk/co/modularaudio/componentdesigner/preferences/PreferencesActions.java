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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.util.audio.gui.mad.rack.GuiConstants;

public class PreferencesActions
{
	private static final String CANCEL_PREFERENCES_NAME = "Cancel Changes";
	private static final String APPLY_PREFERENCES_NAME = "Apply Changes";

	private CancelPreferencesChangesAction cancelPreferencesChangesAction = null;
	private ApplyPreferencesChangesAction applyPreferencesChangesAction = null;

	public PreferencesActions( ComponentDesigner componentDesigner,
			ComponentDesignerFrontController fc,
			PreferencesDialog preferencesDialog,
			ConfigurationService configurationService )
	{
		cancelPreferencesChangesAction = new CancelPreferencesChangesAction(fc, preferencesDialog);
		applyPreferencesChangesAction = new ApplyPreferencesChangesAction(fc, preferencesDialog);
	}
	
	public class CancelPreferencesChangesAction extends AbstractAction
	{
		private static final long serialVersionUID = 5378624881852594498L;

		private ComponentDesignerFrontController fc = null;

		private PreferencesDialog preferencesDialog = null;
		
		public CancelPreferencesChangesAction( ComponentDesignerFrontController fc, PreferencesDialog preferencesDialog )
		{
			this.fc = fc;
			this.preferencesDialog = preferencesDialog;
			this.putValue(NAME, CANCEL_PREFERENCES_NAME );
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			fc.cancelUserPreferencesChanges();
			fc.reloadUserPreferences();
			preferencesDialog.close();
		}
	}
	
	public class ApplyPreferencesChangesAction extends AbstractAction
	{
		private static final long serialVersionUID = -4903439573172278487L;
		
		private ComponentDesignerFrontController fc = null;
		private PreferencesDialog preferencesDialog = null;
		
		public ApplyPreferencesChangesAction( ComponentDesignerFrontController fc, PreferencesDialog pd )
		{
			this.fc = fc;
			this.preferencesDialog = pd;
			this.putValue(NAME, APPLY_PREFERENCES_NAME );
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			boolean wasRendering = fc.isRendering();

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

	public CancelPreferencesChangesAction getCancelAction()
	{
		return cancelPreferencesChangesAction;
	}
	
	public ApplyPreferencesChangesAction getApplyAction()
	{
		return applyPreferencesChangesAction;
	}

}
