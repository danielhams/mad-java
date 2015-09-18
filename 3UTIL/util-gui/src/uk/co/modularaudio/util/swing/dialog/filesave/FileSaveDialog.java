/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.swing.dialog.filesave;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFileChooser;

public class FileSaveDialog extends JFileChooser
{
	private static final long serialVersionUID = -8625393647843389583L;

	private Component parentComponent;
	private String suggestedDirectory;
	private String suggestedFilename;
	private FileSaveDialogCallback callback;

	public static final int DEFAULT_BORDER_WIDTH = 10;

	public FileSaveDialog()
	{
		setMinimumSize( new Dimension( 300, 150 ) );

		setFileSelectionMode( JFileChooser.FILES_ONLY );
	}

	public void setValues( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final String suggestedDirectory,
			final String suggestedFilename,
			final FileSaveDialogCallback callback )
	{
		this.parentComponent = parentComponent;
		this.suggestedDirectory = suggestedDirectory;
		this.suggestedFilename = suggestedFilename;
		this.callback = callback;

		setDialogTitle( title );
	}

	public void go()
	{
		setCurrentDirectory( new File(suggestedDirectory) );
		setSelectedFile( new File( suggestedDirectory + File.separatorChar + suggestedFilename ) );

		final int retVal = showSaveDialog( parentComponent );
		String filePath = null;
		if( retVal == JFileChooser.APPROVE_OPTION )
		{
			filePath = getSelectedFile().getPath();
		}

		if( callback != null && filePath != null )
		{
			callback.receiveFileSaveDialogClosed( filePath );
		}
	}
}
