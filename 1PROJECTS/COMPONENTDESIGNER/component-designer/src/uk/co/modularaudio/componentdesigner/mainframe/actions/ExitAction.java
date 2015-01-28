package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.ExitSignalReceiver;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameActions;

public class ExitAction extends AbstractAction
{
	private final Log log = LogFactory.getLog( ExitAction.class.getName() );
	/**
	 *
	 */
	private final MainFrameActions mainFrameActions;

	private static final long serialVersionUID = 1303196363358495273L;

	private final ComponentDesignerFrontController fc;

	private final ExitSignalReceiver exitSignalReceiver;
	private final SaveFileAction saveFileAction;

	public ExitAction(final MainFrameActions mainFrameActions,
			final ComponentDesignerFrontController fc,
			final ExitSignalReceiver exitSignalReceiver,
			final SaveFileAction saveFileAction )
	{
		this.mainFrameActions = mainFrameActions;
		this.fc = fc;
		this.exitSignalReceiver = exitSignalReceiver;
		this.saveFileAction = saveFileAction;
		this.putValue(NAME, "Exit");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		if( fc.isRendering() )
		{
			fc.toggleRendering();
		}

		log.debug("ExitAction performed called.");
		int optionPaneResult = mainFrameActions.rackNotDirtyOrUserConfirmed();

		if( optionPaneResult == JOptionPane.YES_OPTION )
		{
			// Need to save it - call the save
			saveFileAction.actionPerformed( e );

			// Simulate the cancel in the save action if the rack is still dirty.
			optionPaneResult = ( fc.isRackDirty() ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION);
		}

		if( optionPaneResult == JOptionPane.NO_OPTION )
		{
			// Stop the engine
			if( fc.isAudioEngineRunning() )
			{
				fc.stopAudioEngine();
			}
			// Give any components in the graph a chance to cleanup first
			try
			{
				fc.ensureRenderingStoppedBeforeExit();
			}
			catch (final Exception e1)
			{
				final String msg = "Exception caught during destruction before exit: " + e1.toString();
				log.error( msg, e1 );
			}
			log.debug("Will signal exit");
			exitSignalReceiver.signalExit();
		}
	}
}