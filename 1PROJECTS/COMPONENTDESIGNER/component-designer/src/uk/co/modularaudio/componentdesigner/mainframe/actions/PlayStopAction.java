package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;

public class PlayStopAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( PlayStopAction.class.getName() );
	/**
	 *
	 */
	private static final long serialVersionUID = 255449800376156959L;

	private final ComponentDesignerFrontController fc;

	private final MainFrame mainFrame;

	private final CheckAudioConfigurationAction checkAudioConfigurationAction;

	public PlayStopAction( final ComponentDesignerFrontController fcin,
			final MainFrame mainFrame,
			final CheckAudioConfigurationAction checkAudioConfigurationAction )
	{
		this.fc = fcin;
		this.mainFrame = mainFrame;
		this.checkAudioConfigurationAction = checkAudioConfigurationAction;
		this.putValue(NAME, "Play/Stop");
		this.putValue(SELECTED_KEY, "PlayStop.selected");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		try
		{
			boolean canToggle = false;
			if( !fc.isRendering() )
			{
				if( !fc.isAudioEngineRunning() )
				{
					mainFrame.getToolbar().getPlayStopToggleButton().setSelected( false );
					// Launch the check audio config action
					// and exit.
					checkAudioConfigurationAction.actionPerformed( e );

					if( fc.isAudioEngineRunning() )
					{
						canToggle = true;
					}
				}
				else
				{
					canToggle = true;
				}
			}
			else
			{
				canToggle = true;
			}

			if( canToggle )
			{
				SwingUtilities.invokeLater( new Runnable()
				{

					@Override
					public void run()
					{
						fc.toggleRendering();
					}
				} );
			}
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing play/stop action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}