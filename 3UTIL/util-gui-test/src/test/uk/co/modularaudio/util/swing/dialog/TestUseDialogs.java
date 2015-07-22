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

package test.uk.co.modularaudio.util.swing.dialog;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.dialog.directoryselection.DirectorySelectionDialog;
import uk.co.modularaudio.util.swing.dialog.directoryselection.DirectorySelectionDialogCallback;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialog;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialog;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialog;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public class TestUseDialogs
{
	private static Log log = LogFactory.getLog( TestUseDialogs.class.getName() );

	public void testUseTextInput() throws Exception
	{
		final TextInputDialog tid = new TextInputDialog();

		final TextInputDialogCallback testCallback = new TextInputDialogCallback()
		{

			@Override
			public void dialogClosedReceiveText( final String textOrNull )
			{
				log.debug("JUnit test received callback with: " + textOrNull );
			}
		};
		tid.setValues( null, "Some message", "Some title", JOptionPane.QUESTION_MESSAGE, "initialValue", testCallback );

		tid.go();
	}

	public void testUseYesNoQuestion() throws Exception
	{
		final YesNoQuestionDialog ynd = new YesNoQuestionDialog();

		final YesNoQuestionDialogCallback testCallback = new YesNoQuestionDialogCallback()
		{
			@Override
			public void receiveDialogResultValue( final int value )
			{
				log.debug("JUnit test received yes no callback with " + value );
			}
		};
		final String[] options = new String[] { "YesOption", "NoOption", "CancelOption" };
		final String defaultChoice = options[0];
		ynd.setValues( null,  "Some message", "Some title", JOptionPane.YES_NO_CANCEL_OPTION, options, defaultChoice, testCallback );

		ynd.go();
	}

	public void testMessage() throws Exception
	{
		final MessageDialog md = new MessageDialog();

		final MessageDialogCallback testCallback = new MessageDialogCallback()
		{

			@Override
			public void receiveMessageDialogClosed()
			{
				log.debug("JUnit test received message callback closed");
			}
		};

		md.setValues( null, "Some message", "Some title", JOptionPane.INFORMATION_MESSAGE, testCallback );
		md.go();
	}

	public void testDirectorySelection() throws Exception
	{
		final DirectorySelectionDialog dsd = new DirectorySelectionDialog();

		final DirectorySelectionDialogCallback testCallback = new DirectorySelectionDialogCallback()
		{
			@Override
			public void receiveDirectorySelectionDialogClosed( final String dirPath )
			{
				log.debug("JUnit test received directory selection callback closed - " + dirPath );
			}
		};

		dsd.setValues( null, "Some message", "Some title", JOptionPane.INFORMATION_MESSAGE, testCallback );
		dsd.go();
	}

	public static void main( final String[] args ) throws Exception
	{
		final TestUseDialogs tud = new TestUseDialogs();
		tud.testUseTextInput();
		tud.testUseYesNoQuestion();
		tud.testMessage();
		tud.testDirectorySelection();
	}
}

