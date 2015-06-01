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

package uk.co.modularaudio.util.swing.dialog.yesnoquestion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialog;

public class YesNoQuestionPanel extends JPanel implements ActionListener
{
//	private static Log log = LogFactory.getLog( YesNoQuestionPanel.class.getName() );

	private static final long serialVersionUID = 5562218715267063142L;

	private final YesNoQuestionDialog parentDialog;

	private final JLabel textLabel;
	private final JPanel buttonPanel;

	private YesNoQuestionDialogCallback callback;

	private final HashMap<String,Integer> optionTextToIndexMap = new HashMap<String,Integer>();

	public YesNoQuestionPanel( final YesNoQuestionDialog parentDialog )
	{
		this.parentDialog = parentDialog;

		textLabel = new JLabel();

		final MigLayout migLayout = new MigLayout( "fill, insets " + MessageDialog.DEFAULT_BORDER_WIDTH, "", "[growprio 100][growprio 0]");
		this.setLayout( migLayout );

		this.add( textLabel, "grow, shrink, spanx 3, wrap" );
		buttonPanel = new JPanel();
		final MigLayout buttonPanelLayout = new MigLayout( "gap 5, insets 0, fillx, align center");
		buttonPanel.setLayout( buttonPanelLayout );
		this.add( buttonPanel, "growx, align center");
	}

	public void setValues( final String message,
			final int messageType,
			final String[] options,
			final String defaultChoice,
			final YesNoQuestionDialogCallback callback )
	{
		// Reset the response so window close does the appropriate thing
		textLabel.setText( message );
		this.callback = callback;

		parentDialog.getRootPane().setDefaultButton( null );
		buttonPanel.removeAll();

		for( int i = 0 ; i < options.length ; i++ )
		{
			final String optionText = options[ i ];
			optionTextToIndexMap.put( optionText, i );
			final JButton optionButton = new JButton( optionText );
			optionButton.addActionListener( this );
			buttonPanel.add( optionButton, "align center" );
			if( defaultChoice != null && optionText.equals( defaultChoice ) )
			{
				parentDialog.getRootPane().setDefaultButton( optionButton );
			}
		}
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		final Object eventSource = e.getSource();
		if( eventSource instanceof JButton )
		{
			final JButton button = (JButton)eventSource;
			final String optionText = button.getText();
			parentDialog.setVisible( false );
			if( callback != null )
			{
				callback.receiveDialogResultValue( optionTextToIndexMap.get( optionText ) );
			}
		}
	}

}
