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

package uk.co.modularaudio.util.audio.gui.mad.service.util.filesaveextension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CDFileSaveAccessory extends JPanel
{
	private static final long serialVersionUID = -5713965715731161607L;

//	private static Log log = LogFactory.getLog( CDFileSaveAccessory.class.getName() );

	private final String fileName;

	private final JLabel fileNameLabel;
	private final JTextField fileNameTextField;

	public CDFileSaveAccessory( final String defaultName )
	{
		fileName = defaultName;

		fileNameLabel = new JLabel( "Name:");
		this.add( fileNameLabel );
		fileNameTextField = new JTextField( fileName );
		this.add( fileNameTextField );
	}

	public String getFileName()
	{
		return fileNameTextField.getText();
	}

}
