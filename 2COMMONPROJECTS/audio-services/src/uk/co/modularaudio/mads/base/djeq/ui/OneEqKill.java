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

package uk.co.modularaudio.mads.base.djeq.ui;

import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public class OneEqKill extends LWTCToggleButton
{
	private static final long serialVersionUID = -5705961814474040293L;

	private ToggleListener toggleListener;

	public interface ToggleListener
	{
		void receiveToggleChange( boolean previousValue, boolean newValue );
	};

	public OneEqKill( )
	{
		super( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS, "Kill", true, false );
	}

	@Override
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		toggleListener.receiveToggleChange( previousValue, newValue );
	}

	public void setToggleListener( final ToggleListener tl )
	{
		this.toggleListener = tl;
	}

}
