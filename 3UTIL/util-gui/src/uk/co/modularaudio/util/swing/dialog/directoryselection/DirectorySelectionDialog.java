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

package uk.co.modularaudio.util.swing.dialog.directoryselection;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JFileChooser;

public class DirectorySelectionDialog extends JFileChooser
{
	private static final long serialVersionUID = -8625393647843389583L;

	private Component parentComponent;
	private DirectorySelectionDialogCallback callback;

	public static final int DEFAULT_BORDER_WIDTH = 10;

	public DirectorySelectionDialog()
	{
		setMinimumSize( new Dimension( 300, 150 ) );

		setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
	}

	public void setValues( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final DirectorySelectionDialogCallback callback )
	{
		this.parentComponent = parentComponent;
		this.callback = callback;

		setDialogTitle( title );
	}

	public void go()
	{
		final int retVal = showOpenDialog( parentComponent );
		String dirPath = null;
		if( retVal == JFileChooser.APPROVE_OPTION )
		{
			dirPath = getSelectedFile().getPath();
		}

		if( callback != null && dirPath != null )
		{
			callback.receiveDirectorySelectionDialogClosed( dirPath );
		}
	}
}
