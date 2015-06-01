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

package uk.co.modularaudio.util.swing.dialog.textinput;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JDialog;

public class TextInputDialog extends JDialog
{
//	private static Log log = LogFactory.getLog( TextInputDialog.class.getName());

	private static final long serialVersionUID = -8249294046151891200L;

	private final TextInputPanel textInputPanel;

	public TextInputDialog()
	{
		textInputPanel = new TextInputPanel( this );
		this.setModal( true );

		this.setMinimumSize( new Dimension( 300, 150 ) );

		this.add( textInputPanel );
		this.pack();
	}

	public void setValues( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final String initialValue,
			final TextInputDialogCallback callback )
	{
		this.setTitle( title );
		textInputPanel.setValues( message, messageType, initialValue, callback );
		this.pack();

		this.setLocationRelativeTo( parentComponent );
	}

	public void go()
	{
		setVisible( true );
		textInputPanel.doFocusSetting();
	}

}
