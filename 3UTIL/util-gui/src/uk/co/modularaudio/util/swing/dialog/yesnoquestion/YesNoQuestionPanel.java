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
	
	private YesNoQuestionDialog parentDialog = null;
	
	private JLabel textLabel = null;
	private JPanel buttonPanel = null;
	
	private YesNoQuestionDialogCallback callback = null;
	
	private HashMap<String,Integer> optionTextToIndexMap = new HashMap<String,Integer>();
	
	public YesNoQuestionPanel( YesNoQuestionDialog parentDialog )
	{
		this.parentDialog = parentDialog;
		
		textLabel = new JLabel();
		
		MigLayout migLayout = new MigLayout( "fill, insets " + MessageDialog.DEFAULT_BORDER_WIDTH, "", "[growprio 100][growprio 0]");
		this.setLayout( migLayout );
		
		this.add( textLabel, "grow, shrink, spanx 3, wrap" );
		buttonPanel = new JPanel();
		MigLayout buttonPanelLayout = new MigLayout( "gap 5, insets 0, fillx, align center");
		buttonPanel.setLayout( buttonPanelLayout );
		this.add( buttonPanel, "growx, align center");
	}

	public void setValues( String message,
			int messageType,
			String[] options,
			String defaultChoice,
			YesNoQuestionDialogCallback callback )
	{
		// Reset the response so window close does the appropriate thing
		textLabel.setText( message );
		this.callback = callback;
		
		parentDialog.getRootPane().setDefaultButton( null );
		buttonPanel.removeAll();
		
		for( int i = 0 ; i < options.length ; i++ )
		{
			String optionText = options[ i ];
			optionTextToIndexMap.put( optionText, i );
			JButton optionButton = new JButton( optionText );
			optionButton.addActionListener( this );
			buttonPanel.add( optionButton, "align center" );
			if( defaultChoice != null && optionText.equals( defaultChoice ) )
			{
				parentDialog.getRootPane().setDefaultButton( optionButton );
			}
		}
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		Object eventSource = e.getSource();
		if( eventSource instanceof JButton )
		{
			JButton button = (JButton)eventSource;
			String optionText = button.getText();
			parentDialog.setVisible( false );
			if( callback != null )
			{
				callback.receiveDialogResultValue( optionTextToIndexMap.get( optionText ) );
			}
		}
	}

}
