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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialog;

public class TextInputPanel extends JPanel implements ActionListener
{
//	private static Log log = LogFactory.getLog( TextInputPanel.class.getName() );

	private static final long serialVersionUID = -1201231298787690939L;

	private final TextInputDialog parentDialog;

	private TextInputDialogCallback callback;

	private final JLabel textLabel = new JLabel();
	private final JTextField textField = new JTextField();
	private final JButton okButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");

	public TextInputPanel( final TextInputDialog parentDialog )
	{
		this.parentDialog = parentDialog;
//		okButton.setMaximumSize( new Dimension( 80, 40 ) );
		okButton.addActionListener( this );
		cancelButton.addActionListener( this );

		parentDialog.getRootPane().setDefaultButton( okButton );

		final MigLayout migLayout = new MigLayout( "fill, insets " + MessageDialog.DEFAULT_BORDER_WIDTH, "", "[growprio 100][growprio 0]");
		this.setLayout( migLayout );

		this.add( textLabel, "grow, shrink, spanx 3, wrap" );
		this.add( textField, "grow, shrink, spanx 3, wrap" );
		this.add( okButton, "grow 0, align left");
		this.add( cancelButton, "grow 0, spanx 2, align right" );
	}

	public void setValues( final String message,
			final int messageType,
			final String initialValue,
			final TextInputDialogCallback callback )
	{
		textLabel.setText( message );
		if( initialValue != null )
		{
			textField.setText( initialValue );
		}
		else
		{
			textField.setText( "" );
		}
		this.callback = callback;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
//		log.debug("Received action performed: " + e.toString() );
		final Object source = e.getSource();
		if( source == okButton )
		{
			final String value = textField.getText();
			parentDialog.setVisible( false );
			if( callback != null )
			{
				callback.dialogClosedReceiveText( value );
			}
		}
		else if( source == cancelButton )
		{
			parentDialog.setVisible( false );
			callback.dialogClosedReceiveText( null );
		}
	}

	public void doFocusSetting()
	{
		textField.grabFocus();
	}

}
