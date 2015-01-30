package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.util.exception.DatastoreException;

public class DumpGraphAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( DumpGraphAction.class.getName() );
	/**
	 *
	 */
	private static final long serialVersionUID = -8447845406158954693L;
	private final ComponentDesignerFrontController fc;

	public DumpGraphAction( final ComponentDesignerFrontController fc )
	{
		this.fc = fc;
		this.putValue(NAME, "Dump Rack/Graph To Console");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		try
		{
			fc.dumpRack();
		}
		catch (final DatastoreException ex)
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Error executing dump graph: " + ex.toString(), ex );
			}
		}
	}
}