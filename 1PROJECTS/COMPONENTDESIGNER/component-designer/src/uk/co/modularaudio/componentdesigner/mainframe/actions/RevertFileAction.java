package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;

public class RevertFileAction extends AbstractAction
{
	private static final long serialVersionUID = -4249015082380141979L;

	private final Log log = LogFactory.getLog( RevertFileAction.class.getName() );

	private final ComponentDesignerFrontController fc;

	public RevertFileAction( final ComponentDesignerFrontController fcin )
	{
		this.fc = fcin;
		this.putValue( NAME, "Revert File" );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		log.debug("RevertFileAction called");

		try
		{
			// Check to see if we already have a filename associated with this rack - if not
			if( fc.isRendering() )
			{
				fc.toggleRendering();
			}
			fc.revertRack();
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing revert action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}