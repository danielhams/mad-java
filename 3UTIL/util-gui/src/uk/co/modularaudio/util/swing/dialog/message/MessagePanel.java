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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class MessagePanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = -6846873692470232534L;

//	private static Log log = LogFactory.getLog( MessagePanel.class.getName() );

	private final MessageDialog parentDialog;

	private MessageDialogCallback callback;

	private final JLabel textLabel = new JLabel();
	private final JButton okButton = new JButton("OK");

	public MessagePanel( final MessageDialog parentDialog )
	{
		this.parentDialog = parentDialog;
//		okButton.setMaximumSize( new Dimension( 80, 40 ) );
		okButton.addActionListener( this );

		parentDialog.getRootPane().setDefaultButton( okButton );

		final MigLayout migLayout = new MigLayout( "fill, insets " + MessageDialog.DEFAULT_BORDER_WIDTH, "", "[growprio 100][growprio 0]");
		this.setLayout( migLayout );

		this.add( textLabel, "grow, shrink, spanx 3, wrap" );
		this.add( okButton, "growx 0, align center");
	}

	public void setValues( final String message,
			final int messageType,
			final MessageDialogCallback callback )
	{
		textLabel.setText( message );
		this.callback = callback;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
//		log.debug("Received action performed: " + e.toString() );
		parentDialog.setVisible( false );
		if( callback != null )
		{
			callback.receiveMessageDialogClosed();
		}
	}

}
