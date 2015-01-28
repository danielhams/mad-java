package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;
import uk.co.modularaudio.util.audio.gui.mad.service.util.filesaveextension.CDFileSaveAccessory;

public class SaveAsFileAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( SaveAsFileAction.class.getName() );

	private static final long serialVersionUID = -4249015082380141979L;

	private final ComponentDesignerFrontController fc;

	private final String defaultDirectory;
	private final MainFrame mainFrame;

	public SaveAsFileAction( final ComponentDesignerFrontController fcin,
			final String defaultDirectory,
			final MainFrame mainFrame )
	{
		this.fc = fcin;
		this.defaultDirectory = defaultDirectory;
		this.mainFrame = mainFrame;

		this.putValue( NAME, "Save File As" );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		log.debug("SaveFileAsAction called");

		try
		{
			final JFileChooser saveFileChooser = new JFileChooser();
			final String rackDataModelName = fc.getRackDataModelName();
			final CDFileSaveAccessory fileSaveAccessory = new CDFileSaveAccessory( rackDataModelName );
			saveFileChooser.setAccessory( fileSaveAccessory );
			saveFileChooser.setCurrentDirectory( new File( defaultDirectory ) );
			final int retVal = saveFileChooser.showSaveDialog( mainFrame );
			if( retVal == JFileChooser.APPROVE_OPTION )
			{
				final File f = saveFileChooser.getSelectedFile();
				if( f != null )
				{
					final String rackName = fileSaveAccessory.getFileName();
					if( log.isDebugEnabled() )
					{
						log.debug("Attempting to save to file as " + f.getAbsolutePath() + " with name " + rackName );
					}

					fc.saveRackToFile( f.getAbsolutePath(), rackName );
				}
			}
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing save file as action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}