package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameActions;

public class NewFileAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( NewFileAction.class.getName() );

	/**
	 *
	 */
	private final MainFrameActions mainFrameActions;

	private static final long serialVersionUID = 4608404122938289459L;

	private final ComponentDesignerFrontController fc;
	private final SaveFileAction saveFileAction;

	public NewFileAction( final MainFrameActions mainFrameActions , final ComponentDesignerFrontController fcin,
			final SaveFileAction saveFileAction )
	{
		this.mainFrameActions = mainFrameActions;
		this.fc = fcin;
		this.saveFileAction = saveFileAction;
		this.putValue(NAME, "New File");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		log.debug("NewFileAction called.");
		int dirtyCheckVal = mainFrameActions.rackNotDirtyOrUserConfirmed();
		if( dirtyCheckVal == JOptionPane.YES_OPTION )
		{
			// Need to save it - call the save
			saveFileAction.actionPerformed( e );

			// Simulate the cancel in the save action if the rack is still dirty.
			dirtyCheckVal = ( fc.isRackDirty() ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION);
		}

		// We don't check for cancel, as it will just fall through

		if( dirtyCheckVal == JOptionPane.NO_OPTION )
		{
			if( fc.isRendering() )
			{
				fc.toggleRendering();
			}
			try
			{
				fc.newRack();
			}
			catch (final Exception ex)
			{
				final String msg = "Exception caught performing new file action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}
}