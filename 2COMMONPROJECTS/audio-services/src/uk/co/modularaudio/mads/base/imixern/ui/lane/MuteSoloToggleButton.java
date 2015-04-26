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

package uk.co.modularaudio.mads.base.imixern.ui.lane;

import java.awt.Dimension;

import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public abstract class MuteSoloToggleButton extends LWTCToggleButton
{
	private static final long serialVersionUID = -576319205878844394L;

	public MuteSoloToggleButton( final String label )
	{
		super( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS, label, true, false );

		final Dimension tinySize = new Dimension( 36, 15 );
		this.setPreferredSize( tinySize );
		this.setMinimumSize( tinySize );
		this.setMaximumSize( tinySize );
	}

	@Override
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		if( newValue != previousValue )
		{
			receiveToggleEvent( newValue );
		}
	}

	public abstract void receiveToggleEvent( boolean value );

}
