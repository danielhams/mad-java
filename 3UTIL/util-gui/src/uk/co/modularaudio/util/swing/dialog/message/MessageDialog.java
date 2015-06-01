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

package uk.co.modularaudio.util.swing.dialog.message;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JDialog;

public class MessageDialog extends JDialog
{
	private static final long serialVersionUID = -8625393647843389583L;

	private final MessagePanel messagePanel;

	public static final int DEFAULT_BORDER_WIDTH = 10;

	public MessageDialog()
	{
		messagePanel = new MessagePanel( this );
		this.setModal( true );

		this.setMinimumSize( new Dimension( 300, 150 ) );

		this.add( messagePanel );
		this.pack();
	}

	public void setValues( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final MessageDialogCallback callback )
	{
		this.setTitle( title );
		messagePanel.setValues( message, messageType, callback );
		this.pack();
		this.setLocationRelativeTo( parentComponent );
	}

	public void go()
	{
		setVisible( true );
	}

}
