package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;

public class SaveFileAction extends AbstractAction
{
	private static final long serialVersionUID = -4249015082380141979L;

	private static Log log = LogFactory.getLog( SaveFileAction.class.getName() );

	private final ComponentDesignerFrontController fc;

	private final SaveAsFileAction saveAsFileAction;

	public SaveFileAction( final ComponentDesignerFrontController fcin,
			final SaveAsFileAction saveAsFileAction )
	{
		this.fc = fcin;
		this.saveAsFileAction = saveAsFileAction;
		this.putValue( NAME, "Save File" );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		log.debug("SaveFileAction called");

		try
		{
			// Check to see if we already have a filename associated with this rack - if not
			// we pop up a file chooser dialog to set the filename
			boolean fileSaved = false;
			try
			{
				fc.saveRack();
				fileSaved = true;
			}
			catch(final FileNotFoundException fnfe)
			{
			}

			if( !fileSaved )
			{
				saveAsFileAction.actionPerformed( e );
			}
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing save action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}