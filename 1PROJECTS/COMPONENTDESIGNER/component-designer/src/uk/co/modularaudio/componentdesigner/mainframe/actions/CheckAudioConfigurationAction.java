package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameActions;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialogPageEnum;

public class CheckAudioConfigurationAction extends AbstractAction
{
	/**
	 *
	 */
	private final MainFrame mainFrame;
	private final PreferencesDialog preferencesDialog;

	private static final long serialVersionUID = 3850927484100526941L;

	private final ComponentDesignerFrontController fc;

	public CheckAudioConfigurationAction( final ComponentDesignerFrontController fc,
			final PreferencesDialog preferencesDialog,
			final MainFrame mainFrame )
	{
		this.preferencesDialog = preferencesDialog;
		this.mainFrame = mainFrame;
		this.fc = fc;
		this.putValue(NAME,  "Check Audio Configuration" );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		if( !fc.startAudioEngine() )
		{
			preferencesDialog.loadPreferences();
			preferencesDialog.choosePage( PreferencesDialogPageEnum.AUDIO_SYSTEM );
			final Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					preferencesDialog.setLocationRelativeTo( mainFrame );
					preferencesDialog.setVisible( true );
					fc.showMessageDialog( preferencesDialog,
							MainFrameActions.TEXT_AUDIO_RECONFIG_WARNING,
							MainFrameActions.TEXT_AUDIO_RECONFIG_TITLE,
							JOptionPane.WARNING_MESSAGE,
							null );
				}
			};

			SwingUtilities.invokeLater( r );

		}
	}
}