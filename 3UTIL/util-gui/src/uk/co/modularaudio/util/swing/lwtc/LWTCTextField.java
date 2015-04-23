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

package uk.co.modularaudio.util.swing.lwtc;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LWTCTextField extends JTextField
{
	private static final long serialVersionUID = 4565693484660743678L;

	private static Log log = LogFactory.getLog( LWTCTextField.class.getName() );

	public LWTCTextField( final LWTCTextFieldColours colours )
	{
		super();
		LWTCLookAndFeelHelper.getInstance().updateComponentLaf( this );

		this.setBackground( colours.getBackground() );
		this.setForeground( colours.getForeground() );
		this.setFont( LWTCControlConstants.RACK_FONT );
		this.setBorder( BorderFactory.createLineBorder( LWTCControlConstants.CONTROL_OUTLINE, 1 ) );
	}

	public LWTCTextField()
	{
		this( LWTCControlConstants.STD_TEXTFIELD_COLOURS );
	}
}
