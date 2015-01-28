package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.util.exception.DatastoreException;

public class DumpProfileAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( DumpProfileAction.class.getName() );
	/**
	 *
	 */
	private static final long serialVersionUID = -3756758345674844578L;
	private final ComponentDesignerFrontController fc;

	public DumpProfileAction( final ComponentDesignerFrontController fc )
	{
		this.fc = fc;
		this.putValue(NAME, "Dump Profile To Console");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		try
		{
			fc.dumpProfileResults();
		}
		catch (final DatastoreException e1)
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Error executing dump profile results: " + e1.toString(), e1 );
			}
		}
	}
}