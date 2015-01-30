package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;

public class ToggleLoggingAction extends AbstractAction
{
	private static final long serialVersionUID = 6567077333146253552L;

	private static Log log = LogFactory.getLog( ToggleLoggingAction.class.getName() );

	private final ComponentDesignerFrontController fc;

	public ToggleLoggingAction( final ComponentDesignerFrontController fcin )
	{
		this.fc = fcin;
		this.putValue(NAME, "Enable Logging");
		this.putValue(SELECTED_KEY, "EnableLogging.selected");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		try
		{
			fc.toggleLogging();
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing enable logging action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}